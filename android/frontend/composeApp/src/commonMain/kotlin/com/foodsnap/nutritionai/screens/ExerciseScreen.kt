package com.foodsnap.nutritionai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.model.*
import com.foodsnap.nutritionai.theme.*

@Composable
fun ExerciseScreen(
    userProfile: UserProfile,
    exerciseLogs: List<ExerciseLogEntry>,
    onLogExercise: (String, Int, Int, String, Int) -> Unit,
    onClearLogs: () -> Unit
) {
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf("Moderate") }
    var avgHeartRate by remember { mutableStateOf("") }
    
    // Auto Calorie Estimation state
    val weightKg = userProfile.weight
    val estimatedCalories: Int by remember(name, duration, intensity) {
        derivedStateOf {
            val dur = duration.toIntOrNull() ?: 0
            if (dur <= 0 || name.isBlank()) return@derivedStateOf 0

            val met = when (name.lowercase().trim()) {
                "running", "run", "cardio" -> when (intensity) {
                    "Low" -> 7.0
                    "High" -> 12.5
                    else -> 10.0
                }
                "hiit" -> when (intensity) {
                    "Low" -> 8.0
                    "High" -> 14.0
                    else -> 11.0
                }
                "yoga", "stretch" -> when (intensity) {
                    "Low" -> 2.0
                    "High" -> 4.0
                    else -> 3.0
                }
                "chest", "back", "legs", "shoulders", "arms", "weights", "strength" -> when (intensity) {
                    "Low" -> 3.5
                    "High" -> 8.5
                    else -> 5.5
                }
                else -> when (intensity) {
                    "Low" -> 2.5
                    "High" -> 4.5
                    else -> 3.5
                }
            }
            (met * weightKg * (dur / 60.0)).toInt()
        }
    }

    val totalBurned = exerciseLogs.sumOf { it.caloriesBurned }
    val workoutLibrary = listOf(
        "Cardio" to Icons.Rounded.MonitorHeart,
        "HIIT" to Icons.Rounded.Bolt,
        "Yoga" to Icons.Rounded.Spa,
        "Chest" to Icons.Rounded.FitnessCenter,
        "Back" to Icons.Rounded.FitnessCenter,
        "Legs" to Icons.Rounded.DirectionsRun,
        "Shoulders" to Icons.Rounded.FitnessCenter,
        "Arms" to Icons.Rounded.FitnessCenter
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Workout Tracker",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "Log workout routines, select target categories, and track energy expenditure indexes.",
                fontSize = 13.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Activity Streak & Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(NeonGreen.copy(alpha = 0.5f), Cyan.copy(alpha = 0.5f))),
                        RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ACTIVE STREAK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                            Text("5 Days Active", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Today's burn: $totalBurned kcal", fontSize = 12.sp, color = GrayText)
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Cyan.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Cyan, modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Workout Library Categories Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Workout Library", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until 2) {
                            val index = row * 2 + col
                            val (catName, icon) = workoutLibrary[index]
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .clickable { name = "$catName Workout" },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(icon, contentDescription = catName, tint = NeonGreen, modifier = Modifier.size(22.dp))
                                    Text(catName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Log New Exercise Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Log Workout Activity",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Exercise Name") },
                        placeholder = { Text("e.g. HIIT, Running, Legs Workout") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GlassBorder
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (mins)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = GlassBorder
                            )
                        )

                        OutlinedTextField(
                            value = avgHeartRate,
                            onValueChange = { avgHeartRate = it },
                            label = { Text("Avg HR (bpm)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = GlassBorder
                            )
                        )
                    }

                    // Intensity Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Intensity Level", fontSize = 11.sp, color = GrayText, modifier = Modifier.padding(bottom = 6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                        ) {
                            listOf("Low", "Moderate", "High").forEach { item ->
                                val isSelected = intensity == item
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (isSelected) NeonGreen else Color.Transparent)
                                        .clickable { intensity = item }
                                        .wrapContentSize(Alignment.Center)
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else GrayText
                                    )
                                }
                            }
                        }
                    }

                    // Dynamically Calculated Calorie Detection
                    if (estimatedCalories > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonGreen.copy(alpha = 0.1f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                                    Text("AI Calorie Burn Estimate", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                                }
                                Text("~$estimatedCalories kcal", fontSize = 14.sp, color = NeonGreen, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val durVal = duration.toIntOrNull() ?: 0
                            if (name.isNotBlank() && durVal > 0) {
                                // Clean heart rate or default
                                val hr = avgHeartRate.toIntOrNull() ?: 120
                                onLogExercise(name, durVal, estimatedCalories, intensity, hr)
                                name = ""
                                duration = ""
                                avgHeartRate = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Workout", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logs list
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workout Logs Today",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (exerciseLogs.isNotEmpty()) {
                    TextButton(onClick = onClearLogs) {
                        Text("Reset Logs", color = Color(0xFFEF4444))
                    }
                }
            }

            if (exerciseLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No exercise sessions logged today. Select library item to start!", color = GrayText, fontSize = 13.sp)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    exerciseLogs.forEach { log ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            val logTime = remember(log.timestamp) {
                                try {
                                    if (log.timestamp.contains("T")) {
                                        val timePart = log.timestamp.substringAfter("T")
                                        if (timePart.length >= 5) {
                                            val hhmm = timePart.substring(0, 5)
                                            val parts = hhmm.split(":")
                                            val hr = parts[0].toIntOrNull() ?: 12
                                            val min = parts[1]
                                            val suffix = if (hr >= 12) "PM" else "AM"
                                            val hr12 = if (hr == 0) 12 else if (hr > 12) hr - 12 else hr
                                            "$hr12:$min $suffix"
                                        } else {
                                            timePart
                                        }
                                    } else {
                                        log.timestamp.take(5)
                                    }
                                } catch (e: Exception) {
                                    ""
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = if (logTime.isNotEmpty()) 12.dp else 0.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(log.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Duration: ${log.durationMinutes} mins | Intensity: ${log.intensity} | HR: ${log.heartRate} bpm",
                                            fontSize = 12.sp,
                                            color = GrayText
                                        )
                                    }
                                    Text("-${log.caloriesBurned} kcal", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                if (logTime.isNotEmpty()) {
                                    Text(
                                        text = logTime,
                                        fontSize = 9.sp,
                                        color = GrayText,
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
