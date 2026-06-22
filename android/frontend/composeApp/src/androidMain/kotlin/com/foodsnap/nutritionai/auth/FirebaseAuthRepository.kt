package com.foodsnap.nutritionai.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthRepository : AuthRepository {
    private val firebaseAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).awaitTask()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user.uid, user.email ?: "")
            } else {
                AuthResult.Failure("User is null after successful login")
            }
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Login failed")
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).awaitTask()
            val user = result.user
            if (user != null) {
                // Update profile display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).awaitTask()
                AuthResult.Success(user.uid, user.email ?: "")
            } else {
                AuthResult.Failure("User is null after successful signup")
            }
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Signup failed")
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).awaitTask()
            AuthResult.Success("", email)
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Password reset failed")
        }
    }

    override suspend fun getCurrentUserToken(forceRefresh: Boolean): String? {
        val user = firebaseAuth.currentUser ?: return null
        return try {
            val result = user.getIdToken(forceRefresh).awaitTask()
            result.token
        } catch (e: Exception) {
            null
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun getUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    override fun getUserDisplayName(): String? {
        return firebaseAuth.currentUser?.displayName
    }
}

// Helper to await Play Services Task using suspendCancellableCoroutine
private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Task failed"))
        }
    }
}
