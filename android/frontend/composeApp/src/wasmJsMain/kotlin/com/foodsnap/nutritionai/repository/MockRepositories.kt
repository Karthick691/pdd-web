package com.foodsnap.nutritionai.repository

import com.foodsnap.nutritionai.model.*

class MockUserRepository : UserRepository {
    private var profile = UserProfile()

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return profile
    }

    override suspend fun saveUserProfile(userId: String, profile: UserProfile) {
        this.profile = profile
    }
}

class MockFoodLogRepository : FoodLogRepository {
    private val logs = mutableListOf<FoodLogEntry>()

    override suspend fun getFoodLogs(userId: String): List<FoodLogEntry> {
        return logs
    }

    override suspend fun addFoodLog(userId: String, entry: FoodLogEntry) {
        logs.add(entry)
    }

    override suspend fun deleteFoodLog(userId: String, logId: String) {
        logs.removeAll { it.id == logId }
    }

    override suspend fun updateFoodLog(userId: String, entry: FoodLogEntry) {
        val index = logs.indexOfFirst { it.id == entry.id }
        if (index != -1) {
            logs[index] = entry
        }
    }

    override suspend fun clearFoodLogs(userId: String) {
        logs.clear()
    }
}

class MockWaterRepository : WaterRepository {
    private val logs = mutableListOf<WaterLogEntry>()

    override suspend fun getWaterLogs(userId: String): List<WaterLogEntry> {
        return logs
    }

    override suspend fun addWaterLog(userId: String, entry: WaterLogEntry) {
        logs.add(entry)
    }

    override suspend fun clearWaterLogs(userId: String) {
        logs.clear()
    }
}

class MockChatRepository : ChatRepository {
    private val logs = mutableListOf<ChatMessage>()

    override suspend fun getChatHistory(userId: String): List<ChatMessage> {
        return logs
    }

    override suspend fun addChatMessage(userId: String, message: ChatMessage) {
        logs.add(message)
    }

    override suspend fun clearChatHistory(userId: String) {
        logs.clear()
    }
}

class MockExerciseRepository : ExerciseRepository {
    private val logs = mutableListOf<ExerciseLogEntry>()

    override suspend fun getExerciseLogs(userId: String): List<ExerciseLogEntry> {
        return logs
    }

    override suspend fun addExerciseLog(userId: String, entry: ExerciseLogEntry) {
        logs.add(entry)
    }

    override suspend fun deleteExerciseLog(userId: String, logId: String) {
        logs.removeAll { it.id == logId }
    }

    override suspend fun clearExerciseLogs(userId: String) {
        logs.clear()
    }
}
