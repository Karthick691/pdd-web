package com.foodsnap.nutritionai.repository

import com.foodsnap.nutritionai.model.WaterLogEntry

interface WaterRepository {
    suspend fun getWaterLogs(userId: String): List<WaterLogEntry>
    suspend fun addWaterLog(userId: String, entry: WaterLogEntry)
    suspend fun clearWaterLogs(userId: String)
}
