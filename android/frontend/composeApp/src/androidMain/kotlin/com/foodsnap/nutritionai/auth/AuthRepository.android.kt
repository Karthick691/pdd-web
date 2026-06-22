package com.foodsnap.nutritionai.auth

actual fun getAuthRepository(): AuthRepository = FirebaseAuthRepository()
