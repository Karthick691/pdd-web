import logging
import gc
from PIL import Image

# Configure logger
logger = logging.getLogger("foodsnap.detector")

# Global YOLO model reference
_yolo_model = None

# COCO food and food-container classes
FOOD_CLASSES = {
    "banana",
    "apple",
    "sandwich",
    "orange",
    "broccoli",
    "carrot",
    "hot dog",
    "pizza",
    "donut",
    "cake",
    "bowl"
}

def get_yolo_model():
    """Lazy load the YOLO model and its dependencies to save startup memory."""
    global _yolo_model
    if _yolo_model is None:
        try:
            logger.info("Lazy loading YOLOv8n and PyTorch...")
            # Import ultralytics ONLY when get_yolo_model() is called (runtime, not startup)
            from ultralytics import YOLO
            
            # Load yolov8n.pt from local path
            _yolo_model = YOLO("yolov8n.pt")
            logger.info("YOLOv8n model loaded successfully.")
        except Exception as e:
            logger.error(f"Failed to load YOLOv8n model: {e}")
            raise e
    return _yolo_model

def detect_food(image_source) -> dict:
    """
    Detects food objects in the given image.
    Supports file path or PIL Image object.
    
    Optimized for low-memory CPU environments (Render Free Tier):
    1. Lazy loads PyTorch & Ultralytics
    2. Limits PyTorch threads to 1
    3. Runs inference at imgsz=320
    4. Triggers garbage collection immediately after inference
    """
    try:
        # Load model and heavy dependencies on-demand
        model = get_yolo_model()
        
        # Local imports for PyTorch settings to prevent startup RAM allocation
        import torch
        torch.set_num_threads(1)
        
        # Run inference (disable verbose output, run on CPU, use imgsz=320 for speed/RAM efficiency)
        results = model(image_source, verbose=False, device='cpu', imgsz=320)
        
        foods = []
        non_foods = []
        is_food_detected = False
        
        for result in results:
            if result.boxes is None:
                continue
                
            for box in result.boxes:
                cls_id = int(box.cls[0])
                conf = float(box.conf[0])
                label = model.names[cls_id]
                
                # Bounding box coordinates [x1, y1, x2, y2]
                coords = [round(float(c), 2) for c in box.xyxy[0].tolist()]
                
                detection_info = {
                    "food": label,
                    "confidence": round(conf * 100, 2),
                    "box": coords
                }
                
                if label in FOOD_CLASSES:
                    foods.append(detection_info)
                    is_food_detected = True
                else:
                    non_foods.append({
                        "label": label,
                        "confidence": round(conf * 100, 2)
                    })
        
        # Log detections
        if is_food_detected:
            logger.debug(f"YOLO Detections: {foods}")
        else:
            logger.debug("YOLO: No food objects detected.")
            
        return {
            "is_food_detected": is_food_detected,
            "detections": foods,
            "non_food_detections": non_foods
        }
        
    except Exception as e:
        logger.error(f"Error in YOLO inference: {e}")
        return {
            "is_food_detected": False,
            "detections": [],
            "non_food_detections": [],
            "error": str(e)
        }
    finally:
        # Explicit garbage collection to free memory on CPU
        gc.collect()