package com.foodsnap.nutritionai.repository

import com.foodsnap.nutritionai.model.UserProfile

interface UserRepository {
    suspend fun getUserProfile(userId: String): UserProfile?
    suspend fun saveUserProfile(userId: String, profile: UserProfile)
}
