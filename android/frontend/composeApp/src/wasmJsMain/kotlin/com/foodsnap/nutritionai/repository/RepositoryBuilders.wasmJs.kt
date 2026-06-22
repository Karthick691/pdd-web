package com.foodsnap.nutritionai.repository

import kotlin.JsFun

@JsFun("() => { const d = new Date(); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'); }")
private external fun jsToday(): String

@JsFun("() => { const d = new Date(); d.setDate(d.getDate() - 1); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'); }")
private external fun jsYesterday(): String

@JsFun("() => new Date().toISOString()")
private external fun jsTimestamp(): String

actual fun getUserRepository(): UserRepository = MockUserRepository()
actual fun getFoodLogRepository(): FoodLogRepository = MockFoodLogRepository()
actual fun getWaterRepository(): WaterRepository = MockWaterRepository()
actual fun getChatRepository(): ChatRepository = MockChatRepository()
actual fun getExerciseRepository(): ExerciseRepository = MockExerciseRepository()
actual fun getCurrentTimestamp(): String = jsTimestamp()
actual fun getTodayDateString(): String = jsToday()
actual fun getYesterdayDateString(): String = jsYesterday()
