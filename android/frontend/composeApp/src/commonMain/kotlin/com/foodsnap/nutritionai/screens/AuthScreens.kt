package com.foodsnap.nutritionai.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.model.UserProfile
import com.foodsnap.nutritionai.theme.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.RestaurantMenu

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DarkBackground, DarkSurface)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.15f))
                    .border(2.dp, PrimaryPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🥗", fontSize = 54.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "FoodSnap AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = LightText,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Next-Gen Health Tracker",
                fontSize = 14.sp,
                color = PrimaryPurple,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    userProfile: UserProfile,
    onComplete: (UserProfile) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    
    // Step 1: Biometrics
    var height by remember { mutableStateOf(170f) }
    var weight by remember { mutableStateOf(70f) }
    var age by remember { mutableStateOf(25f) }
    var gender by remember { mutableStateOf("Female") }
    
    // Step 2: Goal
    var goal by remember { mutableStateOf("Weight Loss") }
    
    // Step 3: Activity Level
    var activityLevel by remember { mutableStateOf("Moderate") }
    
    // Step 4: Dietary Preference
    var dietaryPreference by remember { mutableStateOf("Vegetarian") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Progress header
        Text(
            text = "AI PROFILE CALIBRATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Cyan,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Step $step of 5",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Step bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) { index ->
                val active = index + 1 <= step
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (active) NeonGreen else Color.White.copy(alpha = 0.08f))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))

        when (step) {
            1 -> {
                // Biometrics Input
                Text(
                    text = "Configure Biometrics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Provide your basic physiological details to establish metabolic baseline formulas.",
                    fontSize = 12.sp,
                    color = GrayText,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
                )

                // Height Slider
                BiometricSlider(
                    label = "Height",
                    value = height.toInt(),
                    unit = "cm",
                    range = 100..250,
                    onValueChange = { height = it.toFloat() }
                )

                // Weight Slider
                BiometricSlider(
                    label = "Weight",
                    value = weight.toInt(),
                    unit = "kg",
                    range = 30..200,
                    onValueChange = { weight = it.toFloat() }
                )

                // Age Slider
                BiometricSlider(
                    label = "Age",
                    value = age.toInt(),
                    unit = "years",
                    range = 10..100,
                    onValueChange = { age = it.toFloat() }
                )

                // Gender Selection
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Biological Gender", fontSize = 13.sp, color = GrayText, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Female", "Male").forEach { item ->
                            val selected = gender == item
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) NeonGreen.copy(alpha = 0.15f) else GlassSurface)
                                    .border(1.dp, if (selected) NeonGreen else GlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { gender = item },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    color = if (selected) Color.White else GrayText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            
            2 -> {
                // Goal selection
                Text(
                    text = "Select Fitness Goal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Specify your target calorie focus. AI recalculates metabolic outputs accordingly.",
                    fontSize = 12.sp,
                    color = GrayText,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val goals = listOf(
                        "Weight Loss" to "🔥 Burn body fat and maintain lean muscle",
                        "Weight Gain" to "📈 Increase overall mass & caloric capacity",
                        "Muscle Gain" to "💪 Optimize muscle protein synthesis & strength",
                        "Maintenance" to "⚖️ Maintain stable weight & physical vitality",
                        "Athletic Performance" to "⚡ Optimize cellular glycogen stores & endurance"
                    )
                    goals.forEach { (title, subtitle) ->
                        val selected = goal == title
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (selected) Cyan else GlassBorder, RoundedCornerShape(16.dp))
                                .clickable { goal = title },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) Cyan.copy(alpha = 0.08f) else GlassSurface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) Cyan else Color.White.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (selected) "✓" else "",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text(subtitle, color = GrayText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            3 -> {
                // Activity Level
                Text(
                    text = "Determine Activity Level",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "This adjusts the baseline TDEE multiplier index used by the AI engine.",
                    fontSize = 12.sp,
                    color = GrayText,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val activities = listOf(
                        "Sedentary" to "🚶 Very little active movement or office desk profile (1.2x)",
                        "Light" to "🏃 Short walks or active 1-2 times weekly (1.375x)",
                        "Moderate" to "🏋️ Active gym sessions 3-5 times weekly (1.55x)",
                        "Active" to "🚴 High energy drills or drills 6-7 times weekly (1.725x)",
                        "Athlete" to "🏆 Professional intensity double-daily routines (1.9x)"
                    )
                    activities.forEach { (title, subtitle) ->
                        val selected = activityLevel == title
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (selected) NeonGreen else GlassBorder, RoundedCornerShape(16.dp))
                                .clickable { activityLevel = title },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) NeonGreen.copy(alpha = 0.08f) else GlassSurface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) NeonGreen else Color.White.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (selected) "✓" else "",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text(subtitle, color = GrayText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Dietary Preferences
                Text(
                    text = "Dietary Preferences",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Filters out incompatible recipes or products inside recommendations feed.",
                    fontSize = 12.sp,
                    color = GrayText,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val preferences = listOf(
                        "Vegetarian" to "🌱 Excludes poultry and red meats",
                        "Vegan" to "🥑 Excludes dairy, eggs, and all animal products",
                        "Non-Veg" to "🍗 Include all lean proteins and poultry",
                        "Keto" to "🥩 High lipids and protein with minimum carb indexes",
                        "Diabetic Friendly" to "🥗 Low glycemic indexes to preserve insulin indexes"
                    )
                    preferences.forEach { (title, subtitle) ->
                        val selected = dietaryPreference == title
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (selected) Cyan else GlassBorder, RoundedCornerShape(16.dp))
                                .clickable { dietaryPreference = title },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) Cyan.copy(alpha = 0.08f) else GlassSurface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) Cyan else Color.White.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (selected) "✓" else "",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text(subtitle, color = GrayText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // Summary & Calculation Engine
                val calculatedBmr = if (gender == "Male") {
                    10.0 * weight + 6.25 * height - 5.0 * age + 5.0
                } else {
                    10.0 * weight + 6.25 * height - 5.0 * age - 161.0
                }

                val activityFactor = when (activityLevel) {
                    "Sedentary" -> 1.2
                    "Light" -> 1.375
                    "Moderate" -> 1.55
                    "Active" -> 1.725
                    "Athlete" -> 1.9
                    else -> 1.55
                }

                val calculatedTdee = calculatedBmr * activityFactor

                val targetCalories = when (goal) {
                    "Weight Loss" -> calculatedTdee - 500
                    "Weight Gain" -> calculatedTdee + 500
                    "Muscle Gain" -> calculatedTdee + 300
                    "Maintenance" -> calculatedTdee
                    "Athletic Performance" -> calculatedTdee
                    else -> calculatedTdee
                }.coerceIn(1200.0, 5000.0).toInt()

                val bmi = weight / ((height / 100f) * (height / 100f))

                val proteinMultiplier = when (goal) {
                    "Weight Loss" -> 2.2
                    "Muscle Gain" -> 2.2
                    "Weight Gain" -> 1.8
                    "Maintenance" -> 1.8
                    "Athletic Performance" -> 2.0
                    else -> 1.8
                }
                val proteinTarget = (weight * proteinMultiplier).coerceIn(40.0, 250.0).toInt()
                val fatTarget = ((targetCalories * 0.25) / 9).toInt()
                val carbTarget = ((targetCalories - (proteinTarget * 4) - (fatTarget * 9)) / 4).toInt()

                val waterTarget = (weight * 35.0 + when (activityLevel) {
                    "Active" -> 500.0
                    "Athlete" -> 1000.0
                    else -> 0.0
                }).toInt()

                Text(
                    text = "AI Summary Matrix",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Your physiological profile has been processed by our neural calculation nodes.",
                    fontSize = 12.sp,
                    color = GrayText,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(NeonGreen.copy(alpha = 0.5f), Cyan.copy(alpha = 0.5f))),
                            RoundedCornerShape(20.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("METABOLIC DATA SUMMARY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Cyan)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("BMI Index", fontSize = 11.sp, color = GrayText)
                                Text("${(bmi * 100).toInt() / 100.0}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Column {
                                Text("BMR Baseline", fontSize = 11.sp, color = GrayText)
                                Text("${calculatedBmr.toInt()} kcal", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Column {
                                Text("TDEE Energy", fontSize = 11.sp, color = GrayText)
                                Text("${calculatedTdee.toInt()} kcal", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                        
                        Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 16.dp))
                        
                        Text("DAILY NUTRITIONAL BUDGETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DetailRow("Daily Calorie Target", "$targetCalories kcal")
                        DetailRow("Daily Protein Budget", "${proteinTarget}g")
                        DetailRow("Daily Carb Budget", "${carbTarget}g")
                        DetailRow("Daily Fat Budget", "${fatTarget}g")
                        DetailRow("Daily Water Target", "${waterTarget} ml")
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                Button(
                    onClick = {
                        val finalProfile = userProfile.copy(
                            height = height.toDouble(),
                            weight = weight.toDouble(),
                            age = age.toInt(),
                            gender = gender,
                            goal = goal,
                            activityLevel = activityLevel,
                            dietaryPreference = dietaryPreference,
                            bmi = (bmi * 100).toInt() / 100.0,
                            bmr = calculatedBmr,
                            tdee = calculatedTdee,
                            targetCalories = targetCalories,
                            targetProtein = proteinTarget,
                            targetCarbs = carbTarget,
                            targetFats = fatTarget,
                            targetWater = waterTarget,
                            showOnboarding = false
                        )
                        onComplete(finalProfile)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Enter App Dashboard & Save ✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (step < 5) {
            Spacer(modifier = Modifier.height(30.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .background(DarkBackground.copy(alpha = 0.9f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step -= 1 },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("← Back", color = GrayText)
                    }
                }
                Button(
                    onClick = { step += 1 },
                    modifier = Modifier.weight(2f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Continue →", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BiometricSlider(
    label: String,
    value: Int,
    unit: String,
    range: ClosedRange<Int>,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, color = GrayText, fontWeight = FontWeight.SemiBold)
            Text("$value $unit", fontSize = 18.sp, fontWeight = FontWeight.Black, color = NeonGreen)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = NeonGreen,
                activeTrackColor = NeonGreen,
                inactiveTrackColor = Color.White.copy(alpha = 0.05f)
            )
        )
    }
}
