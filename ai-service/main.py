import os
import json
import base64
import io
import logging
from PIL import Image

# Protect against image decompression bomb vulnerabilities (V-11)
Image.MAX_IMAGE_PIXELS = 10000000
from fastapi import FastAPI, UploadFile, File, HTTPException, Request, Depends
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
from pydantic import BaseModel, Field
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

# External service and helper imports
import httpx
from food_detector import detect_food

# Load environment variables
load_dotenv()

# --- Logging Configuration ---
VALID_ENVIRONMENTS = {"development", "staging", "production"}
ENVIRONMENT = os.getenv("ENVIRONMENT", "development").lower().strip()
if ENVIRONMENT not in VALID_ENVIRONMENTS:
    raise SystemExit(f"Invalid ENVIRONMENT: '{ENVIRONMENT}'. Must be one of {VALID_ENVIRONMENTS}")

log_level = logging.DEBUG if ENVIRONMENT == "development" else logging.INFO
logging.basicConfig(level=log_level, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("foodsnap")

# --- AI Provider Configuration ---
# Priority: Gemini (free) -> OpenAI -> Ollama (local) -> Simulation fallback
AI_PROVIDER = os.getenv("AI_PROVIDER", "gemini")  # "gemini", "openai", "ollama"

# --- Gemini Configuration ---
gemini_model = None
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
GEMINI_MODEL_NAME = os.getenv("GEMINI_MODEL", "gemini-2.0-flash")

if AI_PROVIDER == "gemini" and GEMINI_API_KEY:
    try:
        import google.generativeai as genai
        genai.configure(api_key=GEMINI_API_KEY)
        gemini_model = genai.GenerativeModel(GEMINI_MODEL_NAME)
        logger.info(f"Gemini API configured with model '{GEMINI_MODEL_NAME}'")
    except Exception as e:
        logger.warning(f"Gemini API init failed ({e}). Will try fallback providers.")

# --- OpenAI Configuration ---
openai_client = None
OPENAI_KEY = os.getenv("OPENAI_API_KEY", "")

if (AI_PROVIDER == "openai" or (AI_PROVIDER == "gemini" and not gemini_model)) and OPENAI_KEY:
    try:
        from openai import OpenAI
        openai_client = OpenAI(api_key=OPENAI_KEY)
        logger.info("OpenAI API Key loaded and configured.")
    except Exception as e:
        logger.warning(f"OpenAI client init failed ({e}).")

# --- Ollama Configuration (local only) ---
ollama_client = None
USE_OLLAMA = os.getenv("USE_OLLAMA", "false").lower() == "true"
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434/v1")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "gemma3")

if USE_OLLAMA:
    try:
        from openai import OpenAI as OllamaOpenAI
        api_key = os.getenv("OLLAMA_API_KEY") or "ollama"
        ollama_client = OllamaOpenAI(base_url=OLLAMA_BASE_URL, api_key=api_key)
        logger.info(f"Ollama client configured for '{OLLAMA_BASE_URL}' using model '{OLLAMA_MODEL}'")
    except Exception as e:
        logger.warning(f"Ollama client init failed ({e}).")

# Determine the active LLM client for logging
if gemini_model:
    logger.info(f"ACTIVE AI PROVIDER: Gemini ({GEMINI_MODEL_NAME})")
elif openai_client:
    logger.info("ACTIVE AI PROVIDER: OpenAI")
elif ollama_client:
    logger.info(f"ACTIVE AI PROVIDER: Ollama ({OLLAMA_MODEL})")
else:
    logger.warning("No AI provider available. Will use local classifier + simulation fallback.")


# --- Firebase Admin for Token Verification (C-4) ---
FIREBASE_AUTH_ENABLED = False
try:
    import firebase_admin
    from firebase_admin import credentials, auth as firebase_auth

    cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    if cred_path:
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
    else:
        firebase_admin.initialize_app(options={
            'projectId': os.getenv('FIREBASE_PROJECT_ID', 'food-snap-87cfb')
        })
    FIREBASE_AUTH_ENABLED = True
    logger.info("Firebase Admin initialized for token verification.")
except Exception as e:
    logger.warning(f"Firebase Admin not available ({e}). Auth verification disabled.")


# --- Rate Limiter (H-1) ---
limiter = Limiter(key_func=get_remote_address)

# --- USDA FoodData Central API & Fallback Nutrition Database ---
USDA_API_KEY = os.getenv("USDA_API_KEY", "")

def calculate_health_score(calories: float, protein: float, carbs: float, fat: float) -> int:
    """
    Calculate a simple health score (0-100) based on macronutrient composition.
    Highly custom but simple and reproducible.
    """
    if calories <= 0:
        return 0
        
    score = 70
    
    # Protein/Kcal ratio (higher is better for score)
    protein_kcal = protein * 4
    protein_ratio = protein_kcal / calories
    score += min(int(protein_ratio * 60), 15)
    
    # Fat/Kcal ratio (high fat decreases score)
    fat_kcal = fat * 9
    fat_ratio = fat_kcal / calories
    if fat_ratio > 0.40:
        score -= min(int((fat_ratio - 0.40) * 50), 20)
        
    # Calorie penalty for extremely high-calorie items
    if calories > 700:
        score -= min(int((calories - 700) / 20), 15)
        
    return max(min(score, 95), 10)

async def query_usda_nutrition(query: str) -> dict:
    """
    Fetch macronutrient information for a food item using USDA FoodData Central API.
    Returns standard format if successful, otherwise None.
    """
    if not USDA_API_KEY or USDA_API_KEY == "DEMO_KEY":
        logger.warning("USDA API key not configured or using default DEMO_KEY. Skipping USDA API lookup.")
        return None
        
    try:
        url = "https://api.nal.usda.gov/fdc/v1/foods/search"
        params = {
            "api_key": USDA_API_KEY,
            "query": query,
            "pageSize": 1,
            "dataType": "Survey (FNDDS),Branded,SR Legacy"
        }
        logger.info(f"Querying USDA API for: {query}")
        
        async with httpx.AsyncClient(timeout=4.0) as client:
            response = await client.get(url, params=params)
            if response.status_code == 200:
                data = response.json()
                foods = data.get("foods", [])
                if foods:
                    food_item = foods[0]
                    nutrients = food_item.get("foodNutrients", [])
                    
                    calories = 0
                    protein = 0.0
                    carbs = 0.0
                    fat = 0.0
                    
                    for nut in nutrients:
                        name = nut.get("nutrientName", "").lower()
                        unit = nut.get("unitName", "").lower()
                        val = float(nut.get("value", 0))
                        
                        if "energy" in name and ("kcal" in unit or "calories" in unit):
                            calories = int(val)
                        elif "energy" in name and calories == 0 and "kj" in unit:
                            calories = int(val / 4.184)
                        elif "protein" in name:
                            protein = val
                        elif "carbohydrate" in name:
                            carbs = val
                        elif "total lipid" in name or name == "fat" or "total fat" in name:
                            fat = val
                            
                    health_score = calculate_health_score(calories, protein, carbs, fat)
                    
                    return {
                        "calories": str(calories),
                        "macros": {
                            "protein": int(protein),
                            "carbs": int(carbs),
                            "fats": int(fat)
                        },
                        "health_score": str(health_score),
                        "description": f"USDA API Match: {food_item.get('description', '')}"
                    }
    except Exception as e:
        logger.error(f"Failed to fetch nutrition from USDA API for '{query}': {e}")
        
    return None

# Run initialization on import - Disabled for MobileNetV2, YOLO lazy-loaded on demand
if ENVIRONMENT in ("production", "staging"):
    app = FastAPI(title="FoodSnap AI Service", docs_url=None, redoc_url=None)
else:
    app = FastAPI(title="FoodSnap AI Service (Cloud Edition)")
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# --- CORS Configuration — Restricted origins (C-5) ---
ALLOWED_ORIGINS = os.getenv(
    "ALLOWED_ORIGINS",
    "http://localhost:5173,http://localhost:3000,https://food-snap-87cfb.web.app,https://food-snap-87cfb.firebaseapp.com,http://localhost,https://localhost,capacitor://localhost"
).split(",")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[origin.strip() for origin in ALLOWED_ORIGINS],
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["Authorization", "Content-Type"],
)

# --- Security Headers Middleware (L-2, H-5 FIX) ---
@app.middleware("http")
async def add_security_headers(request: Request, call_next):
    response = await call_next(request)
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
    response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
    response.headers["Content-Security-Policy"] = "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; connect-src 'self' https://*.googleapis.com https://*.firebaseapp.com; frame-ancestors 'none'"
    return response

# --- Authentication Dependency (C-4, C-3 FIX) ---
async def verify_token(request: Request):
    """Verify Firebase ID token from Authorization header."""
    auth_header = request.headers.get("Authorization", "")
    
    # Enable local development bypass for testing API endpoints
    if ENVIRONMENT == "development" and auth_header == "Bearer test-token":
        logger.info("Using local test identity bypass (dev mode)")
        return {"uid": "local-test-uid", "email": "test@user.com"}

    # Only allow unauthenticated fallback in development mode
    if not FIREBASE_AUTH_ENABLED:
        if ENVIRONMENT in ("production", "staging"):
            logger.error(f"Firebase Auth is not available in {ENVIRONMENT}. Rejecting request.")
            raise HTTPException(status_code=503, detail="Authentication service unavailable")
        logger.warning("Firebase Auth disabled — using local test identity (dev mode only)")
        return {"uid": "local-test-uid", "email": "test@user.com"}

    if not auth_header.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Authentication required")

    token = auth_header.split("Bearer ", 1)[1]
    try:
        decoded_token = firebase_auth.verify_id_token(token)
        return decoded_token
    except Exception as e:
        logger.warning(f"Token verification failed: {type(e).__name__}")
        raise HTTPException(status_code=401, detail="Invalid or expired token")

# --- Request Models (H-3, L-4) ---
class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=2000)

# --- File Upload Validation Constants (H-2) ---
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5 MB
ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp", "image/gif"}
IMAGE_MAGIC_BYTES = [
    (b'\xff\xd8\xff', 'image/jpeg'),
    (b'\x89PNG',      'image/png'),
    (b'RIFF',         'image/webp'),
    (b'GIF8',         'image/gif'),
]

# --- Shared Food Database ---
ALLOWED_FOODS = {
    "Cheeseburger with French Fries": {
        "calories": "850",
        "protein": 32,
        "carbs": 72,
        "fats": 42,
        "health_score": "55",
        "description": "Trained Model Match: A classic American beef cheeseburger served with golden crispy french fries."
    },
    "Paneer Butter Masala": {
        "calories": "420",
        "protein": 15,
        "carbs": 20,
        "fats": 30,
        "health_score": "70",
        "description": "Trained Model Match: Rich and creamy Indian cottage cheese curry in a spiced tomato, butter, and cashew sauce."
    },
    "Masala Dosa": {
        "calories": "380",
        "protein": 10,
        "carbs": 52,
        "fats": 14,
        "health_score": "78",
        "description": "Trained Model Match: A crispy South Indian rice crepe stuffed with a savory, spiced potato filling."
    },
    "Chicken Biryani": {
        "calories": "650",
        "protein": 28,
        "carbs": 70,
        "fats": 22,
        "health_score": "65",
        "description": "Trained Model Match: A flavorful, aromatic basmati rice dish cooked with tender chicken pieces, yogurt, and warm Indian spices."
    },
    "Raspberry Cake": {
        "calories": "350",
        "protein": 5,
        "carbs": 45,
        "fats": 16,
        "health_score": "50",
        "description": "Trained Model Match: A moist, sweet layer cake filled with fresh red raspberries and whipped vanilla frosting."
    },
    "Idli with Sambar": {
        "calories": "300",
        "protein": 12,
        "carbs": 50,
        "fats": 6,
        "health_score": "85",
        "description": "Trained Model Match: Steamed savory rice cakes served with a lentil-based vegetable stew."
    },
    "Chole Bhature": {
        "calories": "550",
        "protein": 14,
        "carbs": 65,
        "fats": 25,
        "health_score": "45",
        "description": "Trained Model Match: Spicy chickpeas accompanied by deep-fried bread made from maida flour."
    },
    "Fried Rice": {
        "calories": "450",
        "protein": 10,
        "carbs": 70,
        "fats": 15,
        "health_score": "60",
        "description": "Trained Model Match: Wok-tossed rice with vegetables, soy sauce, and aromatic Asian spices."
    },
    "Pasta Alfredo": {
        "calories": "650",
        "protein": 18,
        "carbs": 75,
        "fats": 30,
        "health_score": "40",
        "description": "Trained Model Match: Classic Italian pasta smothered in a rich, creamy parmesan cheese sauce."
    },
    "Fruit Smoothie Bowl": {
        "calories": "350",
        "protein": 8,
        "carbs": 60,
        "fats": 10,
        "health_score": "90",
        "description": "Trained Model Match: Blended frozen fruits topped with fresh berries, nuts, and crunchy granola."
    }
}

def build_food_response(food_name, confidence):
    """Build a standardized response for a recognized food item."""
    food_info = ALLOWED_FOODS[food_name]
    return {
        "food_name": food_name,
        "confidence": str(confidence),
        "calories": food_info["calories"],
        "macros": {
            "protein": food_info["protein"],
            "carbs": food_info["carbs"],
            "fats": food_info["fats"]
        },
        "vitamins": ["Vitamin A: 10%", "Vitamin C: 15%", "Iron: 8%"],
        "health_score": food_info["health_score"],
        "description": food_info["description"],
        "alternatives": "Try pairing with nutrient-dense sides to keep your diet balanced."
    }

def build_unknown_response(confidence):
    """Build a standardized response for an unrecognized food item."""
    return {
        "food_name": "Unknown Food Item",
        "confidence": str(confidence),
        "calories": "0",
        "macros": {
            "protein": 0,
            "carbs": 0,
            "fats": 0
        },
        "vitamins": [],
        "health_score": "0",
        "description": "The uploaded food item could not be recognized as any of the trained foods with high confidence.",
        "alternatives": "Please try scanning a Cheeseburger, Paneer Butter Masala, Masala Dosa, Chicken Biryani, or Raspberry Cake."
    }


def validate_image_content(file_bytes: bytes) -> bool:
    """Check file magic bytes to verify it is actually an image."""
    for magic, _ in IMAGE_MAGIC_BYTES:
        if file_bytes[:len(magic)] == magic:
            return True
    return False

def encode_image(image_bytes):
    try:
        # Resize image to max 256x256 pixels to speed up multimodal inference
        img = Image.open(io.BytesIO(image_bytes))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")
        img.thumbnail((256, 256))
        
        output_buffer = io.BytesIO()
        img.save(output_buffer, format="JPEG", quality=70)
        optimized_bytes = output_buffer.getvalue()
        return base64.b64encode(optimized_bytes).decode('utf-8')
    except Exception as e:
        logger.warning(f"Image compression failed: {e}. Using raw bytes.")
        return base64.b64encode(image_bytes).decode('utf-8')


# --- LLM Analysis Functions ---

FOOD_ANALYSIS_PROMPT = """
You are FoodSnap AI Recognition Engine.

IMPORTANT:
I have already uploaded and trained the following food images:
1. Cheeseburger with French Fries
2. Paneer Butter Masala
3. Masala Dosa
4. Chicken Biryani
5. Raspberry Cake
6. Idli with Sambar
7. Chole Bhature
8. Fried Rice
9. Pasta Alfredo
10. Fruit Smoothie Bowl

Your task is to analyze the uploaded image and recognize the food.

STRICT RULES:
* First check if the food in the image matches one of the 10 trained reference foods above.
* If it matches one of the 10 trained reference foods above, you MUST classify it as that food, set confidence >= 75%, and return the exact predefined nutritional values for that food from the DATABASE below.
* If the food in the image is NOT one of the 10 trained reference foods (e.g. Pizza, Apple, Salad, Soup, etc.), you MUST try to predict what food it is, estimate its nutritional values using your AI capabilities, and set confidence between 50% and 74%.
* If the image does not contain any food or is completely unrecognizable, return "Unknown Food Item" with confidence < 50%.
* Never return generic placeholder foods like Avocado Salmon Poke Bowl or generic meal if the image is recognizable.

DATABASE FOR TRAINED REFERENCE FOODS:
Cheeseburger with French Fries:
Calories: 850
Protein: 32g
Carbs: 72g
Fat: 42g
Health Score: 55

Paneer Butter Masala:
Calories: 420
Protein: 15g
Carbs: 20g
Fat: 30g
Health Score: 70

Masala Dosa:
Calories: 380
Protein: 10g
Carbs: 52g
Fat: 14g
Health Score: 78

Chicken Biryani:
Calories: 650
Protein: 28g
Carbs: 70g
Fat: 22g
Health Score: 65

Raspberry Cake:
Calories: 350
Protein: 5g
Carbs: 45g
Fat: 16g
Health Score: 50

Idli with Sambar:
Calories: 300
Protein: 12g
Carbs: 50g
Fat: 6g
Health Score: 85

Chole Bhature:
Calories: 550
Protein: 14g
Carbs: 65g
Fat: 25g
Health Score: 45

Fried Rice:
Calories: 450
Protein: 10g
Carbs: 70g
Fat: 15g
Health Score: 60

Pasta Alfredo:
Calories: 650
Protein: 18g
Carbs: 75g
Fat: 30g
Health Score: 40

Fruit Smoothie Bowl:
Calories: 350
Protein: 8g
Carbs: 60g
Fat: 10g
Health Score: 90

RETURN ONLY VALID JSON:
{
  "dish_name": "Name of the dish (string)",
  "confidence": 0, (integer between 0 and 100)
  "calories": 0, (integer calories)
  "protein_g": 0, (integer protein)
  "carbs_g": 0, (integer carbs)
  "fat_g": 0, (integer fat)
  "health_score": 0, (integer health score between 0 and 100)
  "description": "Short description of the food item (string)"
}
Ensure the response is valid JSON and matches this structure exactly. Do not add markdown backticks outside.
"""

CHAT_SYSTEM_PROMPT = (
    "You are FoodSnap AI Coach, an intelligent nutrition, fitness, hydration, and wellness assistant.\n\n"
    "Your goal is to provide personalized health guidance by first understanding the user's needs through interactive questions.\n\n"
    "When a user starts a conversation:\n"
    "1. Greet them warmly.\n"
    "2. Identify their primary goal (Weight Loss, Weight Gain, Muscle Gain, Muscle Recovery, Healthy Eating, Hydration Improvement, Sports Performance, General Wellness).\n\n"
    "Ask 3-4 relevant qualifying questions depending on their goal to customize your response (e.g., current/target weight, daily activity level, diet, workouts, sleep, hydration intake).\n"
    "After collecting their answers, provide personalized food, meal plans, target macros, and hydration advice. Encourage sustainable habits.\n"
    "Always be supportive and motivating, use simple language, and end responses with a follow-up question to continue the conversation."
)


async def analyze_with_gemini(image_bytes, prompt=None):
    """Analyze food image using Google Gemini API."""
    if not gemini_model:
        return None
    try:
        img = Image.open(io.BytesIO(image_bytes))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")
        img.thumbnail((512, 512))

        active_prompt = prompt or FOOD_ANALYSIS_PROMPT
        response = gemini_model.generate_content(
            [active_prompt, img],
            generation_config={"response_mime_type": "application/json", "max_output_tokens": 250}
        )
        result_text = response.text.strip()
        logger.debug(f"Raw Gemini Response: {result_text}")
        return json.loads(result_text)
    except Exception as e:
        logger.error(f"Gemini API Error: {e}")
        return None


async def analyze_with_openai(image_bytes, prompt=None):
    """Analyze food image using OpenAI API."""
    if not openai_client:
        return None
    try:
        base64_image = encode_image(image_bytes)
        active_prompt = prompt or FOOD_ANALYSIS_PROMPT
        response = openai_client.chat.completions.create(
            model="gpt-4o-mini",
            response_format={"type": "json_object"},
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": active_prompt},
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{base64_image}"
                            }
                        }
                    ]
                }
            ],
            max_tokens=250
        )
        result_text = response.choices[0].message.content
        logger.debug(f"Raw OpenAI Response: {result_text}")
        return json.loads(result_text)
    except Exception as e:
        print(f"OpenAI API Error: {e}")
        return None


async def analyze_with_ollama(image_bytes, prompt=None):
    """Analyze food image using Ollama (local) API."""
    if not ollama_client:
        return None
    try:
        base64_image = encode_image(image_bytes)
        active_prompt = prompt or FOOD_ANALYSIS_PROMPT
        response = ollama_client.chat.completions.create(
            model=OLLAMA_MODEL,
            response_format={"type": "json_object"},
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": active_prompt},
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{base64_image}"
                            }
                        }
                    ]
                }
            ],
            max_tokens=250
        )
        result_text = response.choices[0].message.content
        logger.debug(f"Raw Ollama Response: {result_text}")
        return json.loads(result_text)
    except Exception as e:
        print(f"Ollama API Error: {e}")
        return None


def process_llm_result(parsed_result):
    """Process raw LLM JSON result into a standardized food response."""
    dish_name = parsed_result.get("dish_name", "Unknown Food Item").strip()
    confidence = int(parsed_result.get("confidence", 0))

    matched_key = None
    for key in ALLOWED_FOODS:
        if key.lower() in dish_name.lower() or dish_name.lower() in key.lower():
            matched_key = key
            break

    if matched_key and confidence >= 75:
        return build_food_response(matched_key, confidence)
    else:
        # Predict food which is not included in reference foods with the help of AI
        if dish_name != "Unknown Food Item" and dish_name != "" and confidence >= 50:
            return {
                "food_name": dish_name,
                "confidence": str(confidence),
                "calories": str(parsed_result.get("calories", 0)),
                "macros": {
                    "protein": int(parsed_result.get("protein_g", 0)),
                    "carbs": int(parsed_result.get("carbs_g", 0)),
                    "fats": int(parsed_result.get("fat_g", 0))
                },
                "vitamins": ["Vitamin A: 10%", "Vitamin C: 12%", "Calcium: 8%"],
                "health_score": str(parsed_result.get("health_score", 50)),
                "description": parsed_result.get("description", "Estimated by AI Vision."),
                "alternatives": "Try pairing with nutrient-dense sides to keep your diet balanced.",
                "source": "ai_prediction"
            }
        else:
            return build_unknown_response(confidence)


async def chat_with_gemini(message):
    """Chat using Gemini API."""
    if not gemini_model:
        return None
    try:
        response = gemini_model.generate_content(
            f"System: {CHAT_SYSTEM_PROMPT}\n\nUser: {message}",
            generation_config={"max_output_tokens": 300}
        )
        return response.text.strip()
    except Exception as e:
        print(f"Gemini Chat Error: {e}")
        return None


async def chat_with_openai(message):
    """Chat using OpenAI API."""
    if not openai_client:
        return None
    try:
        response = openai_client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": CHAT_SYSTEM_PROMPT},
                {"role": "user", "content": message}
            ],
            max_tokens=300
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        print(f"OpenAI Chat Error: {e}")
        return None


async def chat_with_ollama(message):
    """Chat using Ollama (local) API."""
    if not ollama_client:
        return None
    try:
        response = ollama_client.chat.completions.create(
            model=OLLAMA_MODEL,
            messages=[
                {"role": "system", "content": CHAT_SYSTEM_PROMPT},
                {"role": "user", "content": message}
            ],
            max_tokens=300
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        print(f"Ollama Chat Error: {e}")
        return None


def chat_simulation_fallback(message):
    """Rule-based chat fallback when no AI provider is available."""
    try:
        import re
        msg_lower = message.lower()
        
        # Extract actual user question if context headers are present
        if "user question:" in msg_lower:
            question_part = msg_lower.split("user question:")[-1].strip()
        elif "── user question ──" in msg_lower:
            question_part = msg_lower.split("── user question ──")[-1].strip()
        elif "user's message:" in msg_lower:
            question_part = msg_lower.split("user's message:")[-1].strip()
        else:
            question_part = msg_lower

        # Parse profile and stats from context if present
        goal = "General Fitness"
        weight = "N/A"
        water_ml = 0
        food_cals = 0
        exercise_mins = 0
        exercise_cals = 0

        # Parse goal
        goal_match = re.search(r"(?:-|•)\s*goal:\s*([^\n\r]+)", msg_lower)
        if goal_match:
            goal = goal_match.group(1).strip()
            
        # Parse weight
        weight_match = re.search(r"(?:-|•)\s*weight:\s*([^\n\r\s]+)", msg_lower)
        if weight_match:
            weight = weight_match.group(1).strip()
            
        # Parse water (in L or ml)
        water_match = re.search(r"(?:-|•|water:)\s*([0-9.]+)\s*l", msg_lower)
        if water_match:
            try:
                water_ml = int(float(water_match.group(1).strip()) * 1000)
            except:
                pass
        else:
            water_match_ml = re.search(r"water:\s*(\d+)\s*ml", msg_lower)
            if water_match_ml:
                try:
                    water_ml = int(water_match_ml.group(1).strip())
                except:
                    pass
                
        # Parse food calories
        food_match = re.search(r"(?:-|calories:)\s*(\d+)\s*/\s*\d+", msg_lower)
        if food_match:
            food_cals = int(food_match.group(1).strip())
            
        # Parse exercise mins and calories
        ex_match = re.search(r"exercise:\s*(\d+)\s*mins\s*\|\s*(\d+)\s*kcal", msg_lower)
        if ex_match:
            exercise_mins = int(ex_match.group(1).strip())
            exercise_cals = int(ex_match.group(2).strip())
        else:
            ex_match_old = re.search(r"-\s*exercise:\s*(\d+)\s*minutes\s*\((\d+)\s*kcal", msg_lower)
            if ex_match_old:
                exercise_mins = int(ex_match_old.group(1).strip())
                exercise_cals = int(ex_match_old.group(2).strip())

        # Generate custom context-based fallback response
        if any(x in question_part for x in ["gym", "muscle", "workout", "protein", "exercise", "active"]):
            if exercise_mins > 0:
                return f"You've logged {exercise_mins} minutes of exercise today, burning {exercise_cals} kcal. Since your goal is {goal}, consume 25-30g of protein within 45 minutes of training to optimize muscle recovery!"
            else:
                return f"For your goal to {goal}, consistent active output is key. Let me know when you log a workout so we can adjust recovery macros. Focus on chicken, fish, tofu, or whey!"
        elif any(x in question_part for x in ["diet", "weight", "fat", "loss", "calories", "calorie", "eat", "food"]):
            return f"With your goal of {goal} (Weight: {weight}kg), you have consumed {food_cals} kcal today (Net: {food_cals - exercise_cals} kcal). Focus on high-protein, nutrient-dense foods to support your goal."
        elif any(x in question_part for x in ["water", "hydrate", "drink", "hydration"]):
            remaining_ml = max(0, 2800 - water_ml)
            if remaining_ml > 0:
                return f"You have logged {(water_ml / 1000.0):.2f} L of water today. You need {remaining_ml} ml more to hit your 2.8 L target. Keep hydrating!"
            else:
                return f"Awesome job! You've logged {(water_ml / 1000.0):.2f} L of water today, successfully meeting your daily target."
        elif "hello" in question_part or "hi" in question_part:
            return f"Hello! I see your goal is {goal} (Weight: {weight}kg). How can I assist you on your health and nutrition journey today?"
        else:
            return f"Based on your profile (Goal: {goal}, Weight: {weight}kg), keep tracking meals and workouts. Today you logged {food_cals} kcal and {(water_ml/1000.0):.2f} L of water. Let me know what specific tips you need!"
    except Exception as e:
        print(f"Error in fallback: {e}")
        return "I'm your FoodSnap AI Coach. I'm currently operating in offline mode. Focus on balanced meals, drinking plenty of water, and keeping up your activity streak today!"


def run_filename_simulation(filename):
    """Filename-based food matching simulation fallback."""
    import random
    filename = filename.lower() if filename else ""

    if "burger" in filename or "cheeseburger" in filename or "fries" in filename:
        matched_key = "Cheeseburger with French Fries"
    elif "paneer" in filename or ("masala" in filename and "dosa" not in filename) or "butter" in filename:
        matched_key = "Paneer Butter Masala"
    elif "dosa" in filename:
        matched_key = "Masala Dosa"
    elif "biryani" in filename or ("chicken" in filename and "dosa" not in filename and "paneer" not in filename):
        matched_key = "Chicken Biryani"
    elif "cake" in filename or "raspberry" in filename:
        matched_key = "Raspberry Cake"
    elif "idli" in filename or "sambar" in filename:
        matched_key = "Idli with Sambar"
    elif "chole" in filename or "bhature" in filename:
        matched_key = "Chole Bhature"
    elif "rice" in filename and "fried" in filename:
        matched_key = "Fried Rice"
    elif "pasta" in filename or "alfredo" in filename:
        matched_key = "Pasta Alfredo"
    elif "smoothie" in filename or "bowl" in filename or "fruit" in filename:
        matched_key = "Fruit Smoothie Bowl"
    else:
        matched_key = None

    if matched_key:
        return build_food_response(matched_key, random.randint(85, 98))
    else:
        return build_unknown_response(random.randint(40, 60))


@app.get("/")
async def root():
    return {"message": "FoodSnap AI Service is running"}


@app.get("/health")
async def health():
    """Health check endpoint for Cloud Run (M-3 FIX: no internal details exposed)."""
    return {"status": "healthy"}


@app.post("/analyze")
@limiter.limit("10/minute")
async def analyze_food(
    request: Request,
    file: UploadFile = File(...),
    user: dict = Depends(verify_token)
):
    try:
        # --- File Validation (H-2) ---
        if file.content_type and file.content_type not in ALLOWED_IMAGE_TYPES:
            raise HTTPException(
                status_code=400,
                detail="Invalid file type. Please upload a JPEG, PNG, or WebP image."
            )

        image_bytes = await file.read()

        if len(image_bytes) == 0:
            raise HTTPException(status_code=400, detail="Empty file uploaded.")

        if len(image_bytes) > MAX_FILE_SIZE:
            raise HTTPException(status_code=413, detail="File too large. Maximum size is 5MB.")

        if not validate_image_content(image_bytes):
            raise HTTPException(
                status_code=400,
                detail="File content does not match a valid image format."
            )

        # --- Step 1: Local YOLOv8 Object Detection ---
        yolo_res = {"is_food_detected": False, "detections": [], "non_food_detections": []}
        try:
            img = Image.open(io.BytesIO(image_bytes)).convert('RGB')
            yolo_res = detect_food(img)
        except Exception as e:
            logger.error(f"Local YOLOv8 detection failed: {e}. Proceeding directly to LLM.")

        # --- Step 2: Dynamic LLM Prompt construction based on YOLO hints ---
        yolo_detections = yolo_res.get("detections", [])
        non_food_detections = yolo_res.get("non_food_detections", [])
        
        if yolo_detections:
            hints = ", ".join([f"{d['food']} (conf: {d['confidence']}%)" for d in yolo_detections])
            logger.info(f"YOLO food hints found: {hints}. Generating minimal dynamic prompt.")
            gemini_prompt = f"""You are FoodSnap AI Recognition Engine.

YOLO pre-detector found potential food objects in the image: {hints}.

Your task:
1. Verify the exact dish name (if it matches one of our reference foods, classify it as that food: Cheeseburger with French Fries, Paneer Butter Masala, Masala Dosa, Chicken Biryani, Raspberry Cake, Idli with Sambar, Chole Bhature, Fried Rice, Pasta Alfredo, Fruit Smoothie Bowl).
2. Estimate serving size and nutritional values (calories, protein, carbs, fat, health score) based on the image content.
3. Return ONLY a valid JSON matching this schema:
{{
  "dish_name": "Name of the dish (string)",
  "confidence": 0, (integer between 0 and 100)
  "calories": 0, (integer calories)
  "protein_g": 0, (integer protein)
  "carbs_g": 0, (integer carbs)
  "fat_g": 0, (integer fat)
  "health_score": 0, (integer health score between 0 and 100)
  "description": "Short description of the food item (string)"
}}
Do NOT add markdown backticks outside. Ensure the response is valid JSON."""
        else:
            logger.info("YOLO detected no food. Generating full classification fallback prompt.")
            if non_food_detections and any(d["confidence"] > 80.0 for d in non_food_detections):
                non_food_list = ", ".join([f"{d['label']} ({d['confidence']}%)" for d in non_food_detections if d["confidence"] > 80.0])
                logger.info(f"YOLO non-food objects found: {non_food_list}")
                gemini_prompt = FOOD_ANALYSIS_PROMPT + f"\n[Object Detection Warning: The pre-detector found non-food objects ({non_food_list}) and no food. Check carefully if this is a non-food image. If there is no food in this image, you MUST return 'Unknown Food Item'.]"
            else:
                gemini_prompt = FOOD_ANALYSIS_PROMPT + "\n[Object Detection Info: YOLO pre-detector did not find standard COCO food classes. The image may contain a regional dish (like Biryani, Dosa, curry, idli, etc.). Perform full image analysis to recognize any food present.]"

        # --- Step 3: Try LLM providers in priority order ---
        parsed_result = None

        # Try Gemini first
        parsed_result = await analyze_with_gemini(image_bytes, prompt=gemini_prompt)
        if parsed_result:
            final_response = process_llm_result(parsed_result)
        else:
            # Try OpenAI second
            parsed_result = await analyze_with_openai(image_bytes, prompt=gemini_prompt)
            if parsed_result:
                final_response = process_llm_result(parsed_result)
            else:
                # Try Ollama third (local)
                parsed_result = await analyze_with_ollama(image_bytes, prompt=gemini_prompt)
                if parsed_result:
                    final_response = process_llm_result(parsed_result)
                else:
                    if ENVIRONMENT == "development":
                        # Fallback to filename simulation
                        logger.warning("All AI providers unavailable. Running filename simulation fallback.")
                        final_response = run_filename_simulation(getattr(file, "filename", ""))
                    else:
                        logger.error("All AI providers unavailable in production/staging. Failing request.")
                        raise HTTPException(status_code=503, detail="AI analysis service currently unavailable")

        # --- Step 4: Multi-tier Nutrition Lookup (Trained DB -> USDA API -> Fallback) ---
        food_name = final_response.get("food_name", "Unknown Food Item")
        
        if food_name != "Unknown Food Item":
            matched_key = None
            for key in ALLOWED_FOODS:
                if key.lower() in food_name.lower() or food_name.lower() in key.lower():
                    matched_key = key
                    break
            
            if matched_key:
                logger.info(f"Matched reference database food: {matched_key}")
                ref_vals = ALLOWED_FOODS[matched_key]
                final_response["food_name"] = matched_key
                final_response["calories"] = ref_vals["calories"]
                final_response["macros"] = {
                    "protein": ref_vals["protein"],
                    "carbs": ref_vals["carbs"],
                    "fats": ref_vals["fats"]
                }
                final_response["health_score"] = ref_vals["health_score"]
                final_response["description"] = ref_vals["description"]
                final_response["source"] = "reference_db"
            else:
                # Query USDA FoodData Central API
                usda_nutrition = await query_usda_nutrition(food_name)
                if usda_nutrition:
                    logger.info(f"USDA API lookup successful for: {food_name}")
                    final_response["calories"] = usda_nutrition["calories"]
                    final_response["macros"] = usda_nutrition["macros"]
                    final_response["health_score"] = usda_nutrition["health_score"]
                    final_response["description"] = f"{usda_nutrition['description']}. {final_response.get('description', '')}"
                    final_response["source"] = "usda_api"
                else:
                    logger.info(f"Using LLM estimated nutrition for: {food_name}")
                    final_response["source"] = final_response.get("source", "llm_estimation")

        return final_response
    except HTTPException:
        raise  # Re-raise validation errors as-is
    except Exception as e:
        # (H-6) Sanitized error — log details server-side, return generic message to client
        logger.error(f"Error during image analysis: {e}.")
        if ENVIRONMENT == "development":
            logger.warning("Running filename simulation fallback (dev mode only)")
            try:
                return run_filename_simulation(getattr(file, "filename", ""))
            except Exception:
                return {"error": "Analysis temporarily unavailable. Please try again later."}
        else:
            raise HTTPException(status_code=500, detail="An error occurred during image analysis.")


@app.post("/chat")
@limiter.limit("15/minute")
async def ai_assistant(
    request: Request,
    data: ChatRequest,
    user: dict = Depends(verify_token)
):
    msg = data.message
    try:
        # Try Gemini first
        reply = await chat_with_gemini(msg)
        if reply:
            return {"response": reply}

        # Try OpenAI second
        reply = await chat_with_openai(msg)
        if reply:
            return {"response": reply}

        # Try Ollama third (local)
        reply = await chat_with_ollama(msg)
        if reply:
            return {"response": reply}

    except Exception as e:
        # (H-6) Log details server-side only
        logger.error(f"AI Chat Error: {e}. Falling back to rule-based responses.")

    # Expert Simulation Logic fallback
    try:
        return {"response": chat_simulation_fallback(msg)}
    except Exception as exc:
        logger.error(f"Fallback generation failed: {exc}")
        return {"response": "I'm your FoodSnap AI Coach. I'm currently offline but here to support you. Let's focus on hitting your calorie targets and drinking water today!"}


if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", "8002"))
    uvicorn.run(app, host="0.0.0.0", port=port)
