package com.foodsnap.nutritionai.repository

import com.foodsnap.nutritionai.model.ExerciseLogEntry

interface ExerciseRepository {
    suspend fun getExerciseLogs(userId: String): List<ExerciseLogEntry>
    suspend fun addExerciseLog(userId: String, entry: ExerciseLogEntry)
    suspend fun deleteExerciseLog(userId: String, logId: String)
    suspend fun clearExerciseLogs(userId: String)
}
