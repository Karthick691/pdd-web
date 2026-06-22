package com.foodsnap.nutritionai.utils

import com.foodsnap.nutritionai.repository.getTodayDateString
import com.foodsnap.nutritionai.repository.getYesterdayDateString

fun formatLogTimestamp(isoString: String): String {
    if (isoString.isBlank()) return ""
    return try {
        // Handles standard ISO formats like "2026-06-18T20:45:00+05:30"
        val datePart = isoString.substringBefore('T')
        val timePart = isoString.substringAfter('T').substring(0, 5)
        
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        
        val time12h = convertTo12Hour(timePart)
        
        when (datePart) {
            today -> "Today $time12h"
            yesterday -> "Yesterday $time12h"
            else -> {
                val parts = datePart.split("-")
                if (parts.size == 3) {
                    val month = parts[1].toIntOrNull() ?: 1
                    val day = parts[2].toIntOrNull() ?: 1
                    "${getMonthName(month)} $day, $time12h"
                } else {
                    "$datePart $time12h"
                }
            }
        }
    } catch (e: Exception) {
        isoString
    }
}

private fun convertTo12Hour(time24: String): String {
    val parts = time24.split(":")
    if (parts.isEmpty()) return time24
    val hour24 = parts[0].toIntOrNull() ?: return time24
    val minute = if (parts.size > 1) parts[1] else "00"
    val suffix = if (hour24 >= 12) "PM" else "AM"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return "$hour12:$minute $suffix"
}

private fun getMonthName(monthNumber: Int): String {
    return when (monthNumber) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
}
