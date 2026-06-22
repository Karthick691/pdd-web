package com.foodsnap.nutritionai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.theme.*
import com.foodsnap.nutritionai.model.format
import com.foodsnap.nutritionai.model.ExerciseLogEntry

@Composable
fun DashboardScreen(
    currentCalories: Int,
    targetCalories: Int,
    caloriesBurned: Int,
    currentProtein: Int,
    targetProtein: Int,
    currentCarbs: Int,
    targetCarbs: Int,
    currentFats: Int,
    targetFats: Int,
    waterMl: Int,
    currentWeight: Double,
    exerciseLogs: List<ExerciseLogEntry> = emptyList(),
    onAddWater: (Int) -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Water dialog state
    var showWaterDialog by remember { mutableStateOf(false) }
    if (showWaterDialog) {
        WaterDialog(
            onDismiss = { showWaterDialog = false },
            onAddWater = onAddWater
        )
    }

    // Analytics center tabs states
    var activeCategoryTab by remember { mutableStateOf(0) } // 0: Calories, 1: Macros, 2: Hydration
    var activePeriodTab by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly, 2: Monthly

    // Calorie status
    val calPct = if (targetCalories > 0) (currentCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f) else 0f
    
    // Nutrition Score Calculation
    val macroRatio = if (currentProtein + currentCarbs + currentFats > 0) {
        val pPct = currentProtein.toFloat() / targetProtein.coerceAtLeast(1)
        val cPct = currentCarbs.toFloat() / targetCarbs.coerceAtLeast(1)
        val fPct = currentFats.toFloat() / targetFats.coerceAtLeast(1)
        (pPct + cPct + fPct) / 3f
    } else 0f
    val hydrationRatio = (waterMl.toFloat() / 2500f).coerceIn(0f, 1f)
    val score = ((macroRatio * 40f) + (hydrationRatio * 30f) + (calPct * 30f)).coerceIn(0f, 100f).toInt()
    
    // AI tips calculation
    val proteinTip = if (currentProtein < targetProtein * 0.6) {
        "Your protein synthesis is suboptimal today. Add chicken, tofu, or lean egg whites to reach your target of ${targetProtein}g."
    } else {
        "Excellent protein synthesis! You have successfully achieved your amino acid saturation benchmarks."
    }

    val waterTip = if (waterMl < 2000) {
        "Cellular hydration is deficient. Your body requires at least ${(2500 - waterMl).coerceAtLeast(0)} ml more water to maintain optimal lipid metabolism."
    } else {
        "Optimal hydration index! Cellular energy transport and waste processing channels are highly active."
    }

    val macroTip = if (currentCalories > targetCalories) {
        "Caloric budget exceeded by ${currentCalories - targetCalories} kcal. We recommend scheduling a high-intensity cardio block to optimize glycogen consumption."
    } else {
        "Pacing perfectly towards your target calorie budget. Excellent metabolic regulation."
    }

    val latestWorkout = exerciseLogs.lastOrNull()
    val workoutTip = if (latestWorkout != null) {
        val duration = latestWorkout.durationMinutes
        val hr = latestWorkout.heartRate
        val cals = latestWorkout.caloriesBurned
        val intensity = latestWorkout.intensity
        
        "Workout Detected: ${latestWorkout.name} ($duration mins, $cals kcal burned). " +
            "Your average heart rate reached $hr bpm (Intensity: $intensity). " +
            if (hr > 140) {
                "High heart rate zone detected! Focus on hydration and consume at least 300ml of electrolyte water for cardiovascular recovery."
            } else {
                "Heart rate zone stayed aerobic. Good pacing for fat oxidation and endurance development."
            }
    } else {
        "No workouts logged today. To optimize cardiovascular output and accelerate fat oxidation, schedule a 30-minute cardio session."
    }

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
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANALYTICS ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Dashboard Matrix",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonGreen.copy(alpha = 0.12f))
                        .border(1.dp, NeonGreen, RoundedCornerShape(12.dp))
                        .clickable { onNavigateToChat() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.SmartToy, contentDescription = "AI Coach", tint = NeonGreen, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main KPI Cards Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Calories Consumed
                DashboardKpiRow(
                    title = "Calories Consumed",
                    value = "$currentCalories",
                    subtitle = "/ $targetCalories kcal",
                    icon = Icons.Rounded.LocalFireDepartment,
                    progress = calPct,
                    color = NeonGreen
                )

                // Card 2: Hydration Level
                DashboardKpiRow(
                    title = "Hydration Level",
                    value = "${(waterMl / 1000.0).format(2)}",
                    subtitle = "/ 2.50 Liters",
                    icon = Icons.Rounded.WaterDrop,
                    progress = hydrationRatio,
                    color = Cyan,
                    onClick = { showWaterDialog = true }
                )

                // Card 3: Nutrition Target Score
                DashboardKpiRow(
                    title = "Nutrition Target Score",
                    value = "$score",
                    subtitle = "/ 100 Health Score",
                    icon = Icons.Rounded.GpsFixed,
                    progress = score.toFloat() / 100f,
                    color = AccentOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macro details subsection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Macronutrient Calibration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MacroBar(
                        name = "Protein",
                        current = currentProtein,
                        target = targetProtein,
                        color = NeonGreen,
                        unit = "g"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MacroBar(
                        name = "Carbohydrates",
                        current = currentCarbs,
                        target = targetCarbs,
                        color = Cyan,
                        unit = "g"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MacroBar(
                        name = "Fats",
                        current = currentFats,
                        target = targetFats,
                        color = Color(0xFFF43F5E),
                        unit = "g"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Analytics Center with interactive Charts
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Analytics Center",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("LIVE MODEL DATA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Switcher Tabs
                    TabRow(
                        selectedTabIndex = activeCategoryTab,
                        containerColor = Color.Transparent,
                        contentColor = NeonGreen,
                        indicator = { TabRowDefaults.SecondaryIndicator(color = NeonGreen) }
                    ) {
                        listOf("Calories", "Macros", "Hydration").forEachIndexed { idx, tabTitle ->
                            Tab(
                                selected = activeCategoryTab == idx,
                                onClick = { activeCategoryTab = idx },
                                text = { Text(tabTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Period Switcher Tab Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Daily", "Weekly", "Monthly").forEachIndexed { idx, label ->
                            val active = activePeriodTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) NeonGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, if (active) NeonGreen else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { activePeriodTab = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (active) Color.White else GrayText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Interactive Custom Graph View depending on selections
                    val graphTitle = when (activeCategoryTab) {
                        0 -> "Calories Energy Chart (kcal)"
                        1 -> "Macros Distribution Ratio"
                        else -> "Water Hydration Intake (ml)"
                    }
                    val dataPoints = when (activePeriodTab) {
                        0 -> listOf(25, 45, 60, 40, 80, 50, 95) // Daily
                        1 -> listOf(65, 75, 55, 90, 70, 85, 99) // Weekly
                        else -> listOf(45, 55, 65, 50, 85, 90, 95) // Monthly
                    }

                    Text(graphTitle, fontSize = 12.sp, color = GrayText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Drawing simulated graph bar lines
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dataPoints.forEachIndexed { i, pt ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(pt / 100f)
                                        .width(12.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(NeonGreen, Cyan)
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val label = when (activePeriodTab) {
                                    0 -> listOf("9am", "11am", "1pm", "3pm", "5pm", "7pm", "9pm")[i]
                                    1 -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[i]
                                    else -> listOf("Wk1", "Wk2", "Wk3", "Wk4", "Wk5", "Wk6", "Wk7")[i]
                                }
                                Text(label, fontSize = 9.sp, color = GrayText)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Health Tips Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Cyan.copy(alpha = 0.3f), Color.Transparent)),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0x0C06B6D4)), // Glass cyan card
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.SmartToy, contentDescription = "AI", tint = Cyan, modifier = Modifier.size(22.dp))
                        Text(
                            text = "AI HEALTH TIPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cyan,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardTipItem(title = "Macro Balance Optimization", tip = macroTip, color = NeonGreen)
                        DashboardTipItem(title = "Protein Synthesis Status", tip = proteinTip, color = AccentOrange)
                        DashboardTipItem(title = "Hydration Index Audit", tip = waterTip, color = Cyan)
                        DashboardTipItem(title = "Cardio Recovery Audit", tip = workoutTip, color = SecondaryPurple)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
        }
    }
}

@Composable
fun DashboardKpiRow(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    progress: Float,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .then(clickMod),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 12.sp, color = GrayText)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(subtitle, fontSize = 12.sp, color = GrayText, modifier = Modifier.padding(bottom = 2.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape),
                    color = color,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
            }
        }
    }
}

@Composable
fun DashboardTipItem(
    title: String,
    tip: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .background(color, CircleShape)
        )
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(tip, fontSize = 11.sp, color = GrayText, lineHeight = 16.sp)
        }
    }
}

@Composable
fun MacroBar(
    name: String,
    current: Int,
    target: Int,
    color: Color,
    unit: String
) {
    val progress = if (target > 0) current.toFloat() / target.toFloat() else 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("$current / $target $unit", fontSize = 12.sp, color = GrayText)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.05f)
        )
    }
}

