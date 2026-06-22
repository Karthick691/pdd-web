package com.foodsnap.nutritionai.auth

class MockAuthRepository : AuthRepository {
    private var loggedInUserEmail: String? = null
    private var loggedInUserUid: String? = null
    
    override suspend fun login(email: String, password: String): AuthResult {
        loggedInUserEmail = email
        loggedInUserUid = "mock-wasm-uid-123"
        return AuthResult.Success(loggedInUserUid!!, email)
    }

    override suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        loggedInUserEmail = email
        loggedInUserUid = "mock-wasm-uid-123"
        return AuthResult.Success(loggedInUserUid!!, email)
    }

    override fun logout() {
        loggedInUserEmail = null
        loggedInUserUid = null
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return AuthResult.Success("", email)
    }

    override suspend fun getCurrentUserToken(forceRefresh: Boolean): String? {
        val isLocalhost = try {
            val hostname = kotlinx.browser.window.location.hostname
            hostname == "localhost" || hostname == "127.0.0.1"
        } catch (e: Exception) {
            false
        }
        return if (isLocalhost) "test-token" else null
    }

    override fun isUserLoggedIn(): Boolean = loggedInUserEmail != null
    override fun getUserId(): String? = loggedInUserUid
    override fun getUserEmail(): String? = loggedInUserEmail
    override fun getUserDisplayName(): String? = "Wasm User"
}

actual fun getAuthRepository(): AuthRepository = MockAuthRepository()
