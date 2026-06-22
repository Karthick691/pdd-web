package com.foodsnap.nutritionai.repository

import com.foodsnap.nutritionai.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseUserRepository : UserRepository {
    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val doc = db.collection("profiles").document(userId).get().awaitTask()
            if (doc.exists()) {
                UserProfile(
                    name = doc.getString("name") ?: "Jane Doe",
                    email = doc.getString("email") ?: "jane@foodsnap.com",
                    goal = doc.getString("goal") ?: "Weight Loss",
                    targetCalories = doc.getLong("targetCalories")?.toInt() ?: 2000,
                    targetProtein = doc.getLong("targetProtein")?.toInt() ?: 130,
                    targetCarbs = doc.getLong("targetCarbs")?.toInt() ?: 220,
                    targetFats = doc.getLong("targetFats")?.toInt() ?: 65,
                    weight = doc.getDouble("weight") ?: 68.0,
                    targetWeight = doc.getDouble("targetWeight") ?: 62.0,
                    height = doc.getDouble("height") ?: 165.0,
                    age = doc.getLong("age")?.toInt() ?: 28,
                    gender = doc.getString("gender") ?: "Female",
                    activityLevel = doc.getString("activityLevel") ?: "Moderate",
                    dietaryPreference = doc.getString("dietaryPreference") ?: "Vegetarian",
                    bmi = doc.getDouble("bmi") ?: 24.97,
                    bmr = doc.getDouble("bmr") ?: 1435.0,
                    tdee = doc.getDouble("tdee") ?: 1973.0,
                    targetWater = doc.getLong("targetWater")?.toInt() ?: 2500,
                    showOnboarding = doc.getBoolean("showOnboarding") ?: true
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveUserProfile(userId: String, profile: UserProfile) {
        try {
            val map = mapOf(
                "name" to profile.name,
                "email" to profile.email,
                "goal" to profile.goal,
                "targetCalories" to profile.targetCalories,
                "targetProtein" to profile.targetProtein,
                "targetCarbs" to profile.targetCarbs,
                "targetFats" to profile.targetFats,
                "weight" to profile.weight,
                "targetWeight" to profile.targetWeight,
                "height" to profile.height,
                "age" to profile.age,
                "gender" to profile.gender,
                "activityLevel" to profile.activityLevel,
                "dietaryPreference" to profile.dietaryPreference,
                "bmi" to profile.bmi,
                "bmr" to profile.bmr,
                "tdee" to profile.tdee,
                "targetWater" to profile.targetWater,
                "showOnboarding" to profile.showOnboarding
            )
            db.collection("profiles").document(userId).set(map).awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class FirebaseFoodLogRepository : FoodLogRepository {
    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    override suspend fun getFoodLogs(userId: String): List<FoodLogEntry> {
        return try {
            val snapshot = db.collection("foodLogs")
                .whereEqualTo("userId", userId)
                .get()
                .awaitTask()
            val list = mutableListOf<FoodLogEntry>()
            for (doc in snapshot.documents) {
                val proteinVal = doc.get("protein")?.let {
                    if (it is Long) it.toInt()
                    else it.toString().replace("g", "").trim().toIntOrNull() ?: 0
                } ?: 0
                val carbsVal = doc.get("carbs")?.let {
                    if (it is Long) it.toInt()
                    else it.toString().replace("g", "").trim().toIntOrNull() ?: 0
                } ?: 0
                val fatsVal = doc.get("fats")?.let {
                    if (it is Long) it.toInt()
                    else it.toString().replace("g", "").trim().toIntOrNull() ?: 0
                } ?: 0

                list.add(
                    FoodLogEntry(
                        id = doc.id,
                        foodName = doc.getString("foodName") ?: "",
                        calories = doc.getLong("calories")?.toInt() ?: 0,
                        protein = proteinVal,
                        carbs = carbsVal,
                        fats = fatsVal,
                        timestamp = doc.getString("timestamp") ?: "",
                        fiber = doc.getLong("fiber")?.toInt() ?: 0,
                        sugar = doc.getLong("sugar")?.toInt() ?: 0,
                        sodium = doc.getLong("sodium")?.toInt() ?: 0,
                        mealType = doc.getString("mealType") ?: "Breakfast",
                        aiAnalysis = doc.getString("aiAnalysis") ?: "",
                        healthScore = doc.getLong("healthScore")?.toInt() ?: 70,
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                )
            }
            list.sortBy { it.timestamp }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addFoodLog(userId: String, entry: FoodLogEntry) {
        try {
            val map = mapOf(
                "userId" to userId,
                "foodName" to entry.foodName,
                "calories" to entry.calories,
                "protein" to entry.protein,
                "carbs" to entry.carbs,
                "fats" to entry.fats,
                "timestamp" to entry.timestamp,
                "fiber" to entry.fiber,
                "sugar" to entry.sugar,
                "sodium" to entry.sodium,
                "mealType" to entry.mealType,
                "aiAnalysis" to entry.aiAnalysis,
                "healthScore" to entry.healthScore,
                "imageUrl" to entry.imageUrl
            )
            db.collection("foodLogs").add(map).awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteFoodLog(userId: String, logId: String) {
        try {
            db.collection("foodLogs").document(logId).delete().awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateFoodLog(userId: String, entry: FoodLogEntry) {
        try {
            val map = mapOf(
                "foodName" to entry.foodName,
                "calories" to entry.calories,
                "protein" to entry.protein,
                "carbs" to entry.carbs,
                "fats" to entry.fats,
                "timestamp" to entry.timestamp,
                "fiber" to entry.fiber,
                "sugar" to entry.sugar,
                "sodium" to entry.sodium,
                "mealType" to entry.mealType,
                "aiAnalysis" to entry.aiAnalysis,
                "healthScore" to entry.healthScore,
                "imageUrl" to entry.imageUrl
            )
            db.collection("foodLogs").document(entry.id).set(map).awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun clearFoodLogs(userId: String) {
        try {
            val todayStr = getTodayDateString()
            val snapshot = db.collection("foodLogs")
                .whereEqualTo("userId", userId)
                .get()
                .awaitTask()
            for (doc in snapshot.documents) {
                val timestamp = doc.getString("timestamp") ?: ""
                if (timestamp.startsWith(todayStr)) {
                    db.collection("foodLogs").document(doc.id).delete().awaitTask()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class FirebaseExerciseRepository : ExerciseRepository {
    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    override suspend fun getExerciseLogs(userId: String): List<ExerciseLogEntry> {
        return try {
            val snapshot = db.collection("exerciseLogs")
                .whereEqualTo("userId", userId)
                .get()
                .awaitTask()
            val list = mutableListOf<ExerciseLogEntry>()
            for (doc in snapshot.documents) {
                list.add(
                    ExerciseLogEntry(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: doc.getLong("duration")?.toInt() ?: 0,
                        caloriesBurned = doc.getLong("caloriesBurned")?.toInt() ?: 0,
                        intensity = doc.getString("intensity") ?: "Moderate",
                        timestamp = doc.getString("timestamp") ?: "",
                        heartRate = doc.getLong("heartRate")?.toInt() ?: 120
                    )
                )
            }
            list.sortBy { it.timestamp }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addExerciseLog(userId: String, entry: ExerciseLogEntry) {
        try {
            val map = mapOf(
                "userId" to userId,
                "name" to entry.name,
                "durationMinutes" to entry.durationMinutes,
                "duration" to entry.durationMinutes,
                "caloriesBurned" to entry.caloriesBurned,
                "intensity" to entry.intensity,
                "heartRate" to entry.heartRate,
                "timestamp" to entry.timestamp
            )
            db.collection("exerciseLogs").document(entry.id.ifBlank { db.collection("exerciseLogs").document().id }).set(map).awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteExerciseLog(userId: String, logId: String) {
        try {
            db.collection("exerciseLogs").document(logId).delete().awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun clearExerciseLogs(userId: String) {
        try {
            val todayStr = getTodayDateString()
            val snapshot = db.collection("exerciseLogs")
                .whereEqualTo("userId", userId)
                .get()
                .awaitTask()
            for (doc in snapshot.documents) {
                val timestamp = doc.getString("timestamp") ?: ""
                if (timestamp.startsWith(todayStr)) {
                    db.collection("exerciseLogs").document(doc.id).delete().awaitTask()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class FirebaseWaterRepository : WaterRepository {
    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    override suspend fun getWaterLogs(userId: String): List<WaterLogEntry> {
        return try {
            val snapshot = db.collection("waterLogs")
                .whereEqualTo("userId", userId)
                .get()
                .awaitTask()
            val list = mutableListOf<WaterLogEntry>()
            for (doc in snapshot.documents) {
                list.add(
                    WaterLogEntry(
                        amount = doc.getLong("amount")?.toInt() ?: 0,
                        timestamp = doc.getString("timestamp") ?: ""
                    )
                )
            }
            list.sortBy { it.timestamp }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addWaterLog(userId: String, entry: WaterLogEntry) {
        try {
            val map = mapOf(
                "userId" to userId,
                "amount" to entry.amount,
                "timestamp" to entry.timestamp
            )
            db.collection("waterLogs").add(map).awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun clearWaterLogs(userId: String) {
        try {
            val todayStr = getTodayDateString()
            val snapshot = db.collection("waterLogs")
                .whereEqualTo("userId", userId)
                .get()
                .awaitTask()
            for (doc in snapshot.documents) {
                val timestamp = doc.getString("timestamp") ?: ""
                if (timestamp.startsWith(todayStr)) {
                    db.collection("waterLogs").document(doc.id).delete().awaitTask()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class FirebaseChatRepository : ChatRepository {
    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    override suspend fun getChatHistory(userId: String): List<ChatMessage> {
        return try {
            val snapshot = db.collection("chatHistory")
                .whereEqualTo("userId", userId)
                .get()
                .awaitTask()
            val list = mutableListOf<ChatMessage>()
            for (doc in snapshot.documents) {
                list.add(
                    ChatMessage(
                        role = doc.getString("role") ?: "",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getString("timestamp") ?: ""
                    )
                )
            }
            list.sortBy { it.timestamp }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addChatMessage(userId: String, message: ChatMessage) {
        try {
            val map = mapOf(
                "userId" to userId,
                "role" to message.role,
                "text" to message.text,
                "timestamp" to message.timestamp
            )
            db.collection("chatHistory").add(map).awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun clearChatHistory(userId: String) {
        try {
            val snapshot = db.collection("chatHistory")
                .whereEqualTo("userId", userId)
                .get()
                .awaitTask()
            for (doc in snapshot.documents) {
                db.collection("chatHistory").document(doc.id).delete().awaitTask()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Task failed"))
        }
    }
}
