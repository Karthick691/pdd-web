package com.foodsnap.nutritionai

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.foodsnap.nutritionai.routes.registerRoutes

var firebaseAuthEnabled = false

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun initFirebase() {
    try {
        val credPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        val options = if (credPath != null) {
            val stream = java.io.FileInputStream(credPath)
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build()
        } else {
            val projectId = System.getenv("FIREBASE_PROJECT_ID") ?: "food-snap-87cfb"
            FirebaseOptions.builder()
                .setProjectId(projectId)
                .build()
        }
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
        firebaseAuthEnabled = true
        println("SUCCESS: Firebase Admin SDK initialized in Ktor.")
    } catch (e: Exception) {
        println("WARNING: Firebase Admin init failed in Ktor (${e.message}). Auth verification fallback will be used.")
    }
}

fun Application.module() {
    initFirebase()

    install(CORS) {
        val allowedOrigins = System.getenv("ALLOWED_ORIGINS")
            ?: "http://localhost:5173,http://localhost:3000,https://food-snap-87cfb.web.app,https://food-snap-87cfb.firebaseapp.com,http://localhost,https://localhost,capacitor://localhost"
        allowedOrigins.split(",").forEach { origin ->
            val cleanOrigin = origin.trim()
            if (cleanOrigin.isNotEmpty()) {
                try {
                    val url = java.net.URL(cleanOrigin)
                    val hostWithPort = if (url.port != -1) "${url.host}:${url.port}" else url.host
                    allowHost(hostWithPort, schemes = listOf(url.protocol))
                } catch (e: Exception) {
                    val host = cleanOrigin.removePrefix("http://").removePrefix("https://").removePrefix("capacitor://")
                    allowHost(host, schemes = listOf("http", "https"))
                }
            }
        }
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    install(RateLimit) {
        register(RateLimitName("analyzeLimit")) {
            rateLimiter(limit = 10, refillPeriod = 60.seconds)
        }
        register(RateLimitName("chatLimit")) {
            rateLimiter(limit = 15, refillPeriod = 60.seconds)
        }
    }

    install(ContentNegotiation) {
        json()
    }

    registerRoutes()
}
