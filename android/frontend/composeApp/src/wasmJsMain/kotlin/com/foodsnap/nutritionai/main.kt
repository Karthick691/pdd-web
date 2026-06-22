package com.foodsnap.nutritionai

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        val loading = kotlinx.browser.document.getElementById("loading")
        loading?.setAttribute("style", "display: none;")

        CanvasBasedWindow(title = "FoodSnap AI", canvasElementId = "compose-canvas") {
            App()
        }
    } catch (e: Throwable) {
        println("Wasm startup crash:")
        println(e.message)
        
        // Render error overlay to the DOM so it is visible in the browser viewport
        val errorDiv = kotlinx.browser.document.createElement("div")
        errorDiv.setAttribute("style", "color: #ff3333; padding: 20px; font-family: monospace; white-space: pre-wrap; z-index: 9999; position: absolute; top: 0; left: 0; background: #110000; width: 100vw; height: 100vh; box-sizing: border-box; overflow: auto;")
        errorDiv.textContent = "Exception during Kotlin/Wasm startup:\n\n" + e.toString() + "\n\nStacktrace:\n" + e.stackTraceToString()
        kotlinx.browser.document.body?.appendChild(errorDiv)
        
        throw e
    }
}
