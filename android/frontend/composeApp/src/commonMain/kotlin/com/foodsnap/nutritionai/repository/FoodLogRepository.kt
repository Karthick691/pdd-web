package com.foodsnap.nutritionai.repository

import com.foodsnap.nutritionai.model.FoodLogEntry

interface FoodLogRepository {
    suspend fun getFoodLogs(userId: String): List<FoodLogEntry>
    suspend fun addFoodLog(userId: String, entry: FoodLogEntry)
    suspend fun deleteFoodLog(userId: String, logId: String)
    suspend fun updateFoodLog(userId: String, entry: FoodLogEntry)
    suspend fun clearFoodLogs(userId: String)
}
