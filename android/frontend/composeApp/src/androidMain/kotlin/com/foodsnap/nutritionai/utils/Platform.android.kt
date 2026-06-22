package com.foodsnap.nutritionai.utils

import com.foodsnap.nutritionai.BuildConfig

actual fun isDebugMode(): Boolean {
    return BuildConfig.DEBUG
}
