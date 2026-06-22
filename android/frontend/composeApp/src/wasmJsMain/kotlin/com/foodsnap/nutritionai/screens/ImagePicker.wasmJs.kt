package com.foodsnap.nutritionai.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.FileList
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray, String) -> Unit): ImagePickerLauncher {
    return remember {
        object : ImagePickerLauncher {
            override fun pickImage() {
                val input = document.createElement("input") as HTMLInputElement
                input.type = "file"
                input.accept = "image/*"
                input.onchange = {
                    val files: FileList? = input.files
                    if (files != null && files.length > 0) {
                        val file = files.item(0)
                        if (file != null) {
                            val reader = FileReader()
                            reader.onload = {
                                val result = reader.result
                                if (result is ArrayBuffer) {
                                    val array = Int8Array(result)
                                    val bytes = ByteArray(array.length) { i -> array[i] }
                                    onImagePicked(bytes, file.name)
                                }
                            }
                            reader.readAsArrayBuffer(file)
                        }
                    }
                }
                input.click()
            }

            override fun takePhoto() {
                // For desktop browsers, we delegate to file selection which can trigger camera choice on mobile browsers.
                pickImage()
            }
        }
    }
}
