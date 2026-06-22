package com.foodsnap.nutritionai.screens

import androidx.compose.runtime.Composable

interface ImagePickerLauncher {
    fun pickImage()
    fun takePhoto()
}

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray, String) -> Unit): ImagePickerLauncher
