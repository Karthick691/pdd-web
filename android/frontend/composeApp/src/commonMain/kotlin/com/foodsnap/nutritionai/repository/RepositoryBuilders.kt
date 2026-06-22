package com.foodsnap.nutritionai.repository

expect fun getUserRepository(): UserRepository
expect fun getFoodLogRepository(): FoodLogRepository
expect fun getWaterRepository(): WaterRepository
expect fun getChatRepository(): ChatRepository
expect fun getExerciseRepository(): ExerciseRepository
expect fun getCurrentTimestamp(): String
expect fun getTodayDateString(): String
expect fun getYesterdayDateString(): String
