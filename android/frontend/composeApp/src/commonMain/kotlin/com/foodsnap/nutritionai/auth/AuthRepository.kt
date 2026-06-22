package com.foodsnap.nutritionai.auth

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun signUp(email: String, password: String, displayName: String): AuthResult
    fun logout()
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun getCurrentUserToken(forceRefresh: Boolean = false): String?
    fun isUserLoggedIn(): Boolean
    fun getUserId(): String?
    fun getUserEmail(): String?
    fun getUserDisplayName(): String?
}

sealed class AuthResult {
    data class Success(val uid: String, val email: String) : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

expect fun getAuthRepository(): AuthRepository
