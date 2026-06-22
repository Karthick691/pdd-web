package com.foodsnap.nutritionai.model

import kotlinx.serialization.Serializable

@Serializable
data class Macros(
    val protein: Int = 0,
    val carbs: Int = 0,
    val fats: Int = 0
)

@Serializable
data class FoodItem(
    val food_name: String,
    val confidence: String = "100",
    val calories: String = "0",
    val macros: Macros = Macros(),
    val vitamins: List<String> = emptyList(),
    val health_score: String = "0",
    val description: String = "",
    val alternatives: String = "",
    val source: String = "server"
)

@Serializable
data class ChatRequest(
    val message: String
)

@Serializable
data class ChatResponse(
    val response: String
)

@Serializable
data class FoodLogEntry(
    val id: String = "",
    val foodName: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val timestamp: String,
    val fiber: Int = 0,
    val sugar: Int = 0,
    val sodium: Int = 0,
    val mealType: String = "Breakfast",
    val aiAnalysis: String = "",
    val healthScore: Int = 70,
    val imageUrl: String = ""
)

@Serializable
data class UserProfile(
    val name: String = "Jane Doe",
    val email: String = "jane@foodsnap.com",
    val goal: String = "Weight Loss",
    val targetCalories: Int = 2000,
    val targetProtein: Int = 130,
    val targetCarbs: Int = 220,
    val targetFats: Int = 65,
    val weight: Double = 68.0,
    val targetWeight: Double = 62.0,
    val height: Double = 165.0,
    val age: Int = 28,
    val gender: String = "Female",
    val activityLevel: String = "Moderate",
    val dietaryPreference: String = "Vegetarian",
    val bmi: Double = 24.97,
    val bmr: Double = 1435.0,
    val tdee: Double = 1973.0,
    val targetWater: Int = 2500,
    val showOnboarding: Boolean = true,
    val lastSynced: String = ""
)

@Serializable
data class ExerciseLogEntry(
    val id: String,
    val name: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val intensity: String,
    val timestamp: String,
    val heartRate: Int = 120
)

fun Double.format(decimals: Int): String {
    val multiplier = when (decimals) {
        1 -> 10.0
        2 -> 100.0
        3 -> 1000.0
        else -> 100.0
    }
    val rounded = (this * multiplier).toLong() / multiplier
    val str = rounded.toString()
    val dotIndex = str.indexOf('.')
    if (dotIndex == -1) {
        return str + "." + "0".repeat(decimals)
    }
    val existingDecimals = str.length - dotIndex - 1
    return if (existingDecimals < decimals) {
        str + "0".repeat(decimals - existingDecimals)
    } else if (existingDecimals > decimals) {
        str.substring(0, dotIndex + 1 + decimals)
    } else {
        str
    }
}

@Serializable
data class WaterLogEntry(
    val amount: Int,
    val timestamp: String
)

@Serializable
data class ChatMessage(
    val role: String, // "user" or "assistant"
    val text: String,
    val timestamp: String
)

