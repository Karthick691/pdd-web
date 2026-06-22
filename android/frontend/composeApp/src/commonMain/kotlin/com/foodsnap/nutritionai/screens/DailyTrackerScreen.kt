package com.foodsnap.nutritionai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.model.*
import com.foodsnap.nutritionai.theme.*

@Composable
fun DailyTrackerScreen(
    loggedFoods: List<FoodLogEntry>,
    currentCalories: Int,
    targetCalories: Int,
    waterMl: Int,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit,
    onClearLogs: () -> Unit,
    onMealClick: (FoodLogEntry) -> Unit
) {
    val scrollState = rememberScrollState()
    
    var showWaterDialog by remember { mutableStateOf(false) }
    if (showWaterDialog) {
        WaterDialog(
            onDismiss = { showWaterDialog = false },
            onAddWater = onAddWater
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Daily Meal Tracker",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = LightText,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Energy Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Calories Logged Today", fontSize = 12.sp, color = GrayText)
                        Text("$currentCalories kcal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Target Limit", fontSize = 12.sp, color = GrayText)
                        Text("$targetCalories kcal", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LightText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Water Log Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Water Logged Today", fontSize = 12.sp, color = GrayText)
                            Text("$waterMl ml", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                        }
                        Button(
                            onClick = { showWaterDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Log Water", color = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val progress = (waterMl / 2800f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF60A5FA),
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Target: 2.8 L (2,800 ml)", fontSize = 11.sp, color = GrayText)
                        if (waterMl > 0) {
                            Text(
                                text = "Reset Water",
                                fontSize = 11.sp,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.clickable { onResetWater() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meals Eaten Today",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightText
                )
                if (loggedFoods.isNotEmpty()) {
                    TextButton(onClick = onClearLogs) {
                        Text("Reset Daily Log", color = Color(0xFFEF4444))
                    }
                }
            }

            if (loggedFoods.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No meals logged yet. Go to Food Scan to check your first food!", color = GrayText, fontSize = 14.sp)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    loggedFoods.forEach { food ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GlassSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                                .clickable { onMealClick(food) }
                                .padding(16.dp)
                        ) {
                            val logTime = remember(food.timestamp) {
                                try {
                                    if (food.timestamp.contains("T")) {
                                        val timePart = food.timestamp.substringAfter("T")
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
                                        food.timestamp.take(5)
                                    }
                                } catch (e: Exception) {
                                    ""
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = if (logTime.isNotEmpty()) 12.dp else 0.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(food.foodName, fontWeight = FontWeight.Bold, color = LightText, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "P: ${food.protein}g | C: ${food.carbs}g | F: ${food.fats}g",
                                        fontSize = 12.sp,
                                        color = GrayText
                                    )
                                }
                                Text("+${food.calories} kcal", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
