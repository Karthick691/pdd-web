package com.foodsnap.nutritionai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageCompressor {
    
    /**
     * Compresses and resizes an image from a given Uri.
     * Downscales the image so its maximum dimension is [maxDimension] to save memory and network payload.
     * Compresses it to JPEG format with progressive quality reduction targeting [targetSizeMb].
     */
    fun compressImage(context: Context, uri: Uri, targetSizeMb: Double = 1.0, maxDimension: Int = 1200): ByteArray? {
        val contentResolver = context.contentResolver
        var inputStream: InputStream? = null
        try {
            // 1. Decode bounds to get original dimensions (OOM prevention)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            if (originalWidth <= 0 || originalHeight <= 0) return null

            // 2. Calculate sample size for downscaling
            options.inSampleSize = calculateInSampleSize(originalWidth, originalHeight, maxDimension)
            options.inJustDecodeBounds = false

            // 3. Decode bitmap with inSampleSize
            inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            if (bitmap == null) return null

            // 4. Scale precisely if needed to match maxDimension
            val scaledBitmap = scaleBitmapIfNeeded(bitmap, maxDimension)

            // 5. Compress to byte array targeting < targetSizeMb (default 1MB)
            var quality = 85
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            
            // If image is still larger than target size, compress further (quality down to 45)
            val targetBytes = (targetSizeMb * 1024 * 1024).toLong()
            while (outputStream.toByteArray().size > targetBytes && quality > 45) {
                outputStream.reset()
                quality -= 10
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            // Clean up bitmaps
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            return outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                inputStream?.close()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Compresses and resizes an image from a pre-loaded ByteArray.
     * Downscales the image so its maximum dimension is [maxDimension].
     * Compresses to JPEG format with progressive quality reduction targeting [targetSizeMb].
     */
    fun compressImage(data: ByteArray, targetSizeMb: Double = 1.0, maxDimension: Int = 1200): ByteArray? {
        try {
            // 1. Decode bounds
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(data, 0, data.size, options)

            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            if (originalWidth <= 0 || originalHeight <= 0) return null

            // 2. Calculate sample size
            options.inSampleSize = calculateInSampleSize(originalWidth, originalHeight, maxDimension)
            options.inJustDecodeBounds = false

            // 3. Decode bitmap
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options) ?: return null
            val scaledBitmap = scaleBitmapIfNeeded(bitmap, maxDimension)

            // 4. Compress targeting < targetSizeMb
            var quality = 85
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            
            val targetBytes = (targetSizeMb * 1024 * 1024).toLong()
            while (outputStream.toByteArray().size > targetBytes && quality > 45) {
                outputStream.reset()
                quality -= 10
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            return outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        if (width > maxDim || height > maxDim) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val longest = max(width, height)
        if (longest <= maxDim) {
            return bitmap
        }

        val scale = maxDim.toFloat() / longest
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
