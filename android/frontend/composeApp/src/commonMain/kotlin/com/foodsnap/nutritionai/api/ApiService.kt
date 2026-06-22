package com.foodsnap.nutritionai.api

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import com.foodsnap.nutritionai.model.*
import com.foodsnap.nutritionai.auth.getAuthRepository

class UnauthorizedException(message: String) : Exception(message)

class ApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private var baseUrl = "https://foodsnap-ai-47398940550.us-central1.run.app"
    private val authRepository = getAuthRepository()
    
    var onAuthExpired: (() -> Unit)? = null

    fun setBaseUrl(url: String) {
        baseUrl = url
    }

    fun getBaseUrl(): String {
        return baseUrl
    }

    suspend fun analyzeFoodImage(imageBytes: ByteArray, filename: String): FoodItem {
        val token = authRepository.getCurrentUserToken(forceRefresh = false)
        if (token == null) {
            onAuthExpired?.invoke()
            throw UnauthorizedException("Session has expired. Please log in again.")
        }
        
        val response = client.post("$baseUrl/analyze") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                    })
                }
            ))
        }
        
        if (response.status == HttpStatusCode.Unauthorized) {
            onAuthExpired?.invoke()
            throw UnauthorizedException("Session has expired. Please log in again.")
        }
        
        if (response.status.isSuccess()) {
            val text = response.bodyAsText()
            return Json.decodeFromString<FoodItem>(text)
        } else {
            throw Exception("Recognition failed with status code ${response.status.value}")
        }
    }

    suspend fun chatWithAI(
        message: String,
        goal: String? = null,
        height: Double? = null,
        weight: Double? = null,
        waterMl: Int = 0,
        foodCals: Int = 0,
        exerciseMins: Int = 0,
        exerciseCals: Int = 0,
        targetCalories: Int = 2000,
        currentProtein: Int = 0,
        currentCarbs: Int = 0,
        currentFats: Int = 0,
        targetProtein: Int = 150,
        targetCarbs: Int = 200,
        targetFats: Int = 60,
        bmr: Double = 0.0,
        tdee: Double = 0.0,
        bmi: Double = 0.0,
        age: Int = 25,
        gender: String = "Unknown",
        activityLevel: String = "Moderate",
        dietaryPreference: String = "None",
        targetWater: Int = 2800,
        latestWorkoutName: String? = null,
        latestWorkoutDuration: Int = 0,
        latestWorkoutCalories: Int = 0,
        latestWorkoutHeartRate: Int = 0
    ): String {
        val msgLower = message.lowercase()

        fun generatePersonalizedResponse(rawMsg: String): String {
            val qLower = rawMsg.lowercase()
            
            // Check if progress or how doing
            val isProgress = qLower.contains("progress") || 
                             qLower.contains("how am i doing") || 
                             qLower.contains("my stats") || 
                             qLower.contains("how is my day") ||
                             qLower.contains("status") ||
                             qLower.contains("intake")
                             
            if (isProgress) {
                val calRemaining = targetCalories - foodCals
                val waterRemaining = (targetWater - waterMl).coerceAtLeast(0)
                val calProgressPct = if (targetCalories > 0) ((foodCals.toDouble() / targetCalories) * 100).toInt() else 0
                val waterProgressPct = if (targetWater > 0) ((waterMl.toDouble() / targetWater) * 100).toInt() else 0
                
                val calSuggestion = if (foodCals > targetCalories) {
                    "You've slightly exceeded your daily calorie target. Focus on light activity and high-volume, low-calorie foods for the rest of today."
                } else {
                    "You're currently within your calorie budget, with $calRemaining kcal remaining. Excellent pacing!"
                }
                
                val waterSuggestion = if (waterRemaining > 0) {
                    "To reach your hydration target, you need $waterRemaining ml more. Keep a water bottle close and try to sip regularly."
                } else {
                    "Fantastic job hitting your hydration goals today!"
                }
                
                return """
                    Here is your real-time progress update:
                    • Calorie Intake: $foodCals / $targetCalories kcal ($calProgressPct% achieved, $calRemaining kcal remaining)
                    • Hydration: ${(waterMl / 1000.0).format(2)}L / ${(targetWater / 1000.0).format(1)}L ($waterProgressPct% achieved, ${waterRemaining}ml remaining)
                    • Macros Logged: Protein: ${currentProtein}g / ${targetProtein}g | Carbs: ${currentCarbs}g / ${targetCarbs}g | Fats: ${currentFats}g / ${targetFats}g
                    • Active Output: $exerciseMins mins active, burning $exerciseCals kcal today.
                    
                    Suggestions for improvement:
                    1. $calSuggestion
                    2. $waterSuggestion
                    3. Ensure your meals are balanced with lean protein to help muscle recovery.
                """.trimIndent()
            }
            
            if (listOf("gym", "muscle", "workout", "protein", "exercise", "active", "heart rate", "heartrate", "cardio").any { it in qLower }) {
                return if (exerciseMins > 0) {
                    var reply = "Hey there! You've logged $exerciseMins minutes of exercise today, burning $exerciseCals kcal. "
                    if (latestWorkoutDuration > 0) {
                        reply += "Your latest session was $latestWorkoutName for $latestWorkoutDuration minutes, reaching an average heart rate of $latestWorkoutHeartRate bpm and burning $latestWorkoutCalories kcal. "
                        if (latestWorkoutHeartRate > 140) {
                            reply += "With a peak cardiovascular intensity detected at $latestWorkoutHeartRate bpm, make sure to drink at least 300ml of water now and consume 25-30g of protein within 45 minutes to optimize muscle recovery!"
                        } else {
                            reply += "Since your heart rate stayed in the fat-burning/aerobic zone ($latestWorkoutHeartRate bpm), it was a great session for endurance development and cardiovascular health. Keep it up!"
                        }
                    } else {
                        reply += "Since your goal is ${goal ?: "General Fitness"}, make sure to consume high-quality proteins within 45 minutes of training to optimize muscle recovery!"
                    }
                    reply
                } else {
                    "For your ${goal ?: "fitness"} goal, consistent training is key. Log a workout today, and I'll help you calculate exact recovery macros. Chicken, tofu, fish, or whey are ideal recovery foods!"
                }
            }
            
            if (listOf("diet", "weight", "fat", "loss", "calories", "calorie", "eat", "food").any { it in qLower }) {
                return "With your profile (${height?.toInt() ?: 170}cm, ${weight?.toInt() ?: 70}kg) aiming for ${goal ?: "General Health"}, you have consumed $foodCals kcal today (Net: ${foodCals - exerciseCals} kcal against target $targetCalories kcal). Keep focused on nutrient-dense, high-protein foods to support your goal!"
            }
            
            if (listOf("water", "hydrate", "drink", "hydration").any { it in qLower }) {
                val remaining = (targetWater - waterMl).coerceAtLeast(0)
                return "You have drunk ${(waterMl / 1000.0).format(2)} L of water today. " +
                    if (remaining > 0) "You need $remaining ml more to hit your ${(targetWater / 1000.0).format(1)} L target. Keep hydrating!"
                    else "Awesome job hitting your daily hydration target!"
            }
            
            if (qLower.contains("hello") || qLower.contains("hi")) {
                return "Hello! I'm your FoodSnap AI. I see your goal is ${goal ?: "General Health"} (${weight ?: "N/A"} kg). How can I assist you on your health journey today?"
            }
            
            return "Based on your goal to ${goal ?: "General Health"} (Weight: ${weight ?: "N/A"}kg), keep checking your daily tracker. Today you logged $foodCals kcal and ${(waterMl / 1000.0).format(2)} L water. Let me know how I can help!"
        }

        var enrichedMessage = message
        if (goal != null) {
            val dailyTarget = if (goal == "Weight Loss") "Calorie Deficit" else if (goal == "Muscle Gain") "Calorie Surplus" else "Maintenance"
            val calRemaining = targetCalories - foodCals
            val netCalories = foodCals - exerciseCals
            val proteinPct = if (foodCals > 0) ((currentProtein * 4.0 / foodCals) * 100).toInt() else 0
            val carbPct = if (foodCals > 0) ((currentCarbs * 4.0 / foodCals) * 100).toInt() else 0
            val fatPct = if (foodCals > 0) ((currentFats * 9.0 / foodCals) * 100).toInt() else 0
            val waterGap = (targetWater - waterMl).coerceAtLeast(0)
            val bmiStatus = if (bmi < 18.5) "Underweight" else if (bmi < 25.0) "Normal" else if (bmi < 30.0) "Overweight" else "Obese"

            val workoutSection = if (latestWorkoutName != null) {
                """
                
                ── RECENT WORKOUT DETAILS ──
                • Latest Workout: $latestWorkoutName ($latestWorkoutDuration mins)
                • Calorie Burned: $latestWorkoutCalories kcal
                • Avg Heart Rate: $latestWorkoutHeartRate bpm
                """.trimIndent()
            } else ""

            enrichedMessage = """
                [SYSTEM INSTRUCTION: You are FoodSnap AI Coach — an elite, context-aware nutrition and fitness advisor. You MUST personalize every response using the real-time data below. Reference specific numbers from the user's data when answering. Never give generic advice. Keep responses concise (under 180 words), actionable, and motivating. Use the user's goal to tailor your coaching tone.]$workoutSection
                
                ── USER BIOMETRIC PROFILE ──
                • Goal: $goal ($dailyTarget strategy)
                • Age: $age | Gender: $gender
                • Height: ${height ?: 170.0} cm | Weight: ${weight ?: 70.0} kg
                • BMI: ${(bmi * 10).toInt() / 10.0} ($bmiStatus)
                • BMR: ${bmr.toInt()} kcal | TDEE: ${tdee.toInt()} kcal
                • Activity Level: $activityLevel
                • Dietary Preference: $dietaryPreference
                
                ── TODAY'S LIVE DASHBOARD ──
                Calories: $foodCals / $targetCalories kcal consumed ($calRemaining remaining)
                Protein: ${currentProtein}g / ${targetProtein}g ($proteinPct% of intake)
                Carbs: ${currentCarbs}g / ${targetCarbs}g ($carbPct% of intake)
                Fats: ${currentFats}g / ${targetFats}g ($fatPct% of intake)
                Water: ${(waterMl / 1000.0).format(2)}L / ${(targetWater / 1000.0).format(1)}L (${waterGap}ml remaining)
                Exercise: $exerciseMins mins | $exerciseCals kcal burned
                Net Calorie Balance: $netCalories kcal
                
                ── USER QUESTION ──
                $message
            """.trimIndent()
        }

        val replyText = try {
            val token = authRepository.getCurrentUserToken(forceRefresh = false)
            if (token == null) {
                onAuthExpired?.invoke()
                throw UnauthorizedException("Session has expired. Please log in again.")
            }
            
            val response = client.post("$baseUrl/chat") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
                setBody(ChatRequest(enrichedMessage))
            }
            
            if (response.status == HttpStatusCode.Unauthorized) {
                onAuthExpired?.invoke()
                throw UnauthorizedException("Session has expired. Please log in again.")
            }
            
            if (response.status.isSuccess()) {
                val res = response.bodyAsText()
                val parsed = Json.decodeFromString<ChatResponse>(res)
                parsed.response
            } else {
                generatePersonalizedResponse(message)
            }
        } catch (e: Exception) {
            if (e is UnauthorizedException) {
                throw e
            }
            generatePersonalizedResponse(message)
        }

        val mockTemplates = listOf(
            "For muscle recovery after your workout",
            "For sustainable weight management",
            "Managing sugar spikes is crucial",
            "Hydration is the foundation of health",
            "Hello! I'm your FoodSnap AI nutritionist",
            "That's an interesting health query"
        )
        
        val isMock = mockTemplates.any { replyText.contains(it) }
        val isProgressQuery = msgLower.contains("progress") || 
                msgLower.contains("how am i doing") || 
                msgLower.contains("my stats") || 
                msgLower.contains("how is my day") ||
                msgLower.contains("status") ||
                msgLower.contains("intake")

        if (isMock || isProgressQuery) {
            return generatePersonalizedResponse(message)
        }
        
        return replyText
    }
}
