package com.foodsnap.nutritionai.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.theme.*
import com.foodsnap.nutritionai.model.UserProfile

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    currentCalories: Int,
    waterMl: Int,
    currentProtein: Int,
    onNavigateToScan: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToDiet: () -> Unit,
    onNavigateToExercise: () -> Unit,
    onAddWater: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Calculated values
    val caloriesRemaining = (userProfile.targetCalories - currentCalories).coerceAtLeast(0)
    val calorieProgress = if (userProfile.targetCalories > 0) currentCalories.toFloat() / userProfile.targetCalories else 0f
    
    val waterTarget = userProfile.targetWater.coerceAtLeast(1000)
    val waterProgress = waterMl.toFloat() / waterTarget
    
    val proteinTarget = userProfile.targetProtein.coerceAtLeast(1)
    val proteinProgress = currentProtein.toFloat() / proteinTarget
    
    // Dynamic AI Recommendation message based on goals
    val aiRecommendation = remember(userProfile.goal) {
        when (userProfile.goal) {
            "Weight Loss" -> "Your metabolic rate is optimized! To keep burning fat, focus on high-fiber items and try a 20-minute Cardio session today."
            "Weight Gain" -> "Calorie surplus target is active. Ensure you log your mid-afternoon meal to stay on track for your mass goals."
            "Muscle Gain" -> "Protein synthesis is key. Add a serving of eggs or plant protein, and schedule your Legs/Shoulders workout session."
            "Maintenance" -> "You are doing great maintaining balance. Continue keeping hydration levels up and complete daily walking goals."
            else -> "Athletic performance engine active. Focus on complex carbohydrates pre-workout to maintain cellular glycogen stores."
        }
    }
    
    // Motivation Quote list
    val quotes = listOf(
        "Your body is a temple, but keep it as a futuristic engine optimized by AI.",
        "Every meal logged is a step closer to complete metabolic calibration.",
        "Small daily calibrations compound into massive health milestones over time.",
        "Hydrate, refuel, rebuild. The optimal health formula is fully automated."
    )
    val currentQuote = remember { quotes.random() }

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
            // Header section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WELCOME,",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = userProfile.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NeonGreen, Cyan)
                            )
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(CardBackgroundDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Website Hero Section: Motivational Card & AI Nutrition Score
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
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = "DAILY MOTIVATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"$currentQuote\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 18.sp
                        )
                    }
                    
                    // Circular AI Nutrition Score Dial
                    Column(
                        modifier = Modifier.weight(0.7f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(70.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color.White.copy(alpha = 0.08f),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                drawArc(
                                    brush = Brush.linearGradient(listOf(NeonGreen, Cyan)),
                                    startAngle = -90f,
                                    sweepAngle = 0.88f * 360f, // 88 score
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "88",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "SCORE",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrayText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI Health Score",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Progress Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calories Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Remaining", fontSize = 11.sp, color = GrayText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$caloriesRemaining",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen
                        )
                        Text("kcal left", fontSize = 11.sp, color = GrayText)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { calorieProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = NeonGreen,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                    }
                }

                // Water Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Hydration", fontSize = 11.sp, color = GrayText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$waterMl",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Cyan
                        )
                        Text("/ $waterTarget ml", fontSize = 11.sp, color = GrayText)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { waterProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = Cyan,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Protein & Goals Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Protein Progress", fontSize = 12.sp, color = GrayText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currentProtein / $proteinTarget g",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { proteinProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = NeonGreen,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Egg, contentDescription = "Protein", tint = NeonGreen, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Recommendation Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Cyan.copy(alpha = 0.2f), Color.Transparent)),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0x0C06B6D4)), // glass cyan tint
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.SmartToy, contentDescription = "AI", tint = Cyan, modifier = Modifier.size(22.dp))
                        Text(
                            text = "AI RECOMMENDATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cyan,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = aiRecommendation,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Actions Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions List/Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionRowItem(
                    icon = Icons.Rounded.CameraAlt,
                    title = "Scan Food",
                    desc = "Detect meal nutrition in real-time",
                    accent = NeonGreen,
                    onClick = onNavigateToScan
                )
                ActionRowItem(
                    icon = Icons.Rounded.RestaurantMenu,
                    title = "Log Meal",
                    desc = "Log custom dish to daily tracker",
                    accent = Cyan,
                    onClick = onNavigateToTracker
                )
                ActionRowItem(
                    icon = Icons.Rounded.WaterDrop,
                    title = "Add Water",
                    desc = "Log 250ml hydration intake",
                    accent = Cyan,
                    onClick = { onAddWater(250) }
                )
                ActionRowItem(
                    icon = Icons.Rounded.FitnessCenter,
                    title = "Start Workout",
                    desc = "Log active calories and exercises",
                    accent = AccentOrange,
                    onClick = onNavigateToExercise
                )
                ActionRowItem(
                    icon = Icons.Rounded.ListAlt,
                    title = "View Diet Plan",
                    desc = "Activate schedules and menus",
                    accent = NeonGreen,
                    onClick = onNavigateToDiet
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // bottom padding
        }
    }
}

@Composable
fun ActionRowItem(
    icon: ImageVector,
    title: String,
    desc: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = desc,
                        fontSize = 11.sp,
                        color = GrayText
                    )
                }
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        }
    }
}
