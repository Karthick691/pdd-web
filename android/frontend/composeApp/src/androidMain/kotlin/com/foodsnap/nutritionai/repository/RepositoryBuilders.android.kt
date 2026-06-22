package com.foodsnap.nutritionai.repository

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun getUserRepository(): UserRepository = FirebaseUserRepository()
actual fun getFoodLogRepository(): FoodLogRepository = FirebaseFoodLogRepository()
actual fun getWaterRepository(): WaterRepository = FirebaseWaterRepository()
actual fun getChatRepository(): ChatRepository = FirebaseChatRepository()
actual fun getExerciseRepository(): ExerciseRepository = FirebaseExerciseRepository()
actual fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    return sdf.format(Date())
}

actual fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

actual fun getYesterdayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
}
