package com.foodsnap.nutritionai.model

import kotlinx.serialization.Serializable

@Serializable
data class Macros(
    val protein: Int,
    val carbs: Int,
    val fats: Int
)

@Serializable
data class FoodItem(
    val food_name: String,
    val confidence: String,
    val calories: String,
    val macros: Macros,
    val vitamins: List<String>,
    val health_score: String,
    val description: String,
    val alternatives: String,
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
data class ExerciseLogEntry(
    val id: String,
    val name: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val intensity: String,
    val timestamp: String
)

