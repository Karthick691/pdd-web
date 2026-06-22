package com.foodsnap.nutritionai.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.foodsnap.nutritionai.utils.ImageCompressor
import java.io.File

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray, String) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    
    // Remember the photo state (URI and File)
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }

    // Launcher for selecting an image from the gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Compress and resize the image
                val compressedBytes = ImageCompressor.compressImage(context, uri)
                val fileName = getFileName(context, uri) ?: "gallery_image.jpg"
                if (compressedBytes != null) {
                    onImagePicked(compressedBytes, fileName)
                } else {
                    Toast.makeText(context, "Failed to load/compress image.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error processing gallery image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher for capturing a photo via camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val uri = currentPhotoUri
            val file = currentPhotoFile
            if (uri != null && file != null) {
                try {
                    val compressedBytes = ImageCompressor.compressImage(context, uri)
                    if (compressedBytes != null) {
                        onImagePicked(compressedBytes, file.name)
                    } else {
                        Toast.makeText(context, "Failed to process captured image.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error processing camera image: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    // Clean up temporary files from cache
                    try {
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (ignored: Exception) {}
                }
            }
        }
    }

    // Launcher for gallery permission requests
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Gallery access permission denied.", Toast.LENGTH_LONG).show()
        }
    }

    // Launcher for camera permission requests
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera(context, cameraLauncher) { uri, file ->
                currentPhotoUri = uri
                currentPhotoFile = file
            }
        } else {
            Toast.makeText(context, "Camera permission denied.", Toast.LENGTH_LONG).show()
        }
    }

    return remember {
        object : ImagePickerLauncher {
            override fun pickImage() {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    galleryLauncher.launch("image/*")
                } else {
                    galleryPermissionLauncher.launch(permission)
                }
            }

            override fun takePhoto() {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    launchCamera(context, cameraLauncher) { uri, file ->
                        currentPhotoUri = uri
                        currentPhotoFile = file
                    }
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}

private fun launchCamera(
    context: Context,
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    onStateUpdate: (Uri, File) -> Unit
) {
    try {
        val cacheDir = context.cacheDir
        val tempFile = File.createTempFile("captured_food_", ".jpg", cacheDir)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, tempFile)
        onStateUpdate(uri, tempFile)
        cameraLauncher.launch(uri)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to initialize camera: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}
