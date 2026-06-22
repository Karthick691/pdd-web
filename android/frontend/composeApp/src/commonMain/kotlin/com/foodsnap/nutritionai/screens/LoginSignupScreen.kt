package com.foodsnap.nutritionai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.theme.*
import com.foodsnap.nutritionai.auth.getAuthRepository
import com.foodsnap.nutritionai.auth.AuthResult
import com.foodsnap.nutritionai.repository.UserRepository
import com.foodsnap.nutritionai.model.UserProfile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.text.input.ImeAction
import com.foodsnap.nutritionai.repository.getCurrentTimestamp
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState


@Composable
fun LoginSignupScreen(
    userRepository: UserRepository,
    onLoginSuccess: () -> Unit
) {
    val authRepository = remember { getAuthRepository() }
    val scope = rememberCoroutineScope()

    var isLogin by remember { mutableStateOf(true) }
    var signupStep by remember { mutableStateOf(1) }

    // Step 1 Fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    // Step 2 Fields
    var ageStr by remember { mutableStateOf("28") }
    var genderStr by remember { mutableStateOf("Female") }
    var heightStr by remember { mutableStateOf("165") }
    var weightStr by remember { mutableStateOf("68") }

    // Step 3 Fields
    var goalStr by remember { mutableStateOf("Weight Loss") }
    var targetWeightStr by remember { mutableStateOf("62") }

    // Step 4 Fields
    var activityLevelStr by remember { mutableStateOf("Moderate") }
    var dietaryPreferenceStr by remember { mutableStateOf("Vegetarian") }

    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Validations
    val isEmailValid = remember(email) {
        email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"))
    }
    val hasMinLength = remember(password) { password.length >= 8 }
    val hasUppercase = remember(password) { password.any { it.isUpperCase() } }
    val hasLowercase = remember(password) { password.any { it.isLowerCase() } }
    val hasDigit = remember(password) { password.any { it.isDigit() } }
    val hasSpecialChar = remember(password) { password.any { !it.isLetterOrDigit() } }
    val isPasswordValid = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar

    val isStep1Valid = name.isNotBlank() && isEmailValid && isPasswordValid
    val isStep2Valid = ageStr.isNotBlank() && heightStr.isNotBlank() && weightStr.isNotBlank()
    val isStep3Valid = targetWeightStr.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, DarkSurface)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryGreen.copy(alpha = 0.2f))
                    .border(1.dp, PrimaryGreen, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Restaurant,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isLogin) "Welcome Back" else "Create Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = if (isLogin) "Sign in to track your food and metrics" else "Step $signupStep of 6 • Onboarding Wizard",
                fontSize = 13.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            if (!isLogin) {
                OnboardingTimeline(signupStep)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isLogin) {
                        // Email Input
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = GlassBorder
                            )
                        )

                        // Password Input
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = GlassBorder
                            )
                        )

                        Text(
                            text = "Forgot Password?",
                            color = PrimaryGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { showResetDialog = true }
                        )
                    } else {
                        // SignUp Steps
                        when (signupStep) {
                            1 -> {
                                // Step 1: Credentials
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Username") },
                                    singleLine = true,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = PrimaryGreen,
                                        unfocusedBorderColor = GlassBorder
                                    )
                                )

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email Address") },
                                    singleLine = true,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = PrimaryGreen,
                                        unfocusedBorderColor = GlassBorder
                                    )
                                )

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    singleLine = true,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = PrimaryGreen,
                                        unfocusedBorderColor = GlassBorder
                                    )
                                )

                                // Password checklist
                                if (password.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Validation Checklist", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        listOf(
                                            isEmailValid to "Valid email format",
                                            hasMinLength to "Password: min 8 characters",
                                            hasUppercase to "Password: min one uppercase (A-Z)",
                                            hasLowercase to "Password: min one lowercase (a-z)",
                                            hasDigit to "Password: min one number (0-9)",
                                            hasSpecialChar to "Password: min one special symbol"
                                        ).forEach { (valid, text) ->
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(
                                                    imageVector = if (valid) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                                                    contentDescription = null,
                                                    tint = if (valid) NeonGreen else Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(text, fontSize = 11.sp, color = if (valid) Color.White else GrayText)
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // Step 2: Biometrics
                                OutlinedTextField(
                                    value = ageStr,
                                    onValueChange = { ageStr = it },
                                    label = { Text("Age (years)") },
                                    singleLine = true,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = PrimaryGreen, unfocusedBorderColor = GlassBorder)
                                )

                                OutlinedTextField(
                                    value = heightStr,
                                    onValueChange = { heightStr = it },
                                    label = { Text("Height (cm)") },
                                    singleLine = true,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = PrimaryGreen, unfocusedBorderColor = GlassBorder)
                                )

                                OutlinedTextField(
                                    value = weightStr,
                                    onValueChange = { weightStr = it },
                                    label = { Text("Weight (kg)") },
                                    singleLine = true,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = PrimaryGreen, unfocusedBorderColor = GlassBorder)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("Gender", fontSize = 12.sp, color = GrayText, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        listOf("Female", "Male").forEach { item ->
                                            val selected = genderStr == item
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (selected) Cyan.copy(alpha = 0.15f) else GlassSurface)
                                                    .border(1.dp, if (selected) Cyan else GlassBorder, RoundedCornerShape(12.dp))
                                                    .clickable { genderStr = item },
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
                            3 -> {
                                // Step 3: Goals
                                Text(
                                    text = "What is your main health objective?",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            OnboardingGoalCard(
                                                title = "Weight Loss",
                                                description = "Burn body fat and achieve a lean physique",
                                                icon = Icons.Rounded.LocalFireDepartment,
                                                iconColor = Color(0xFF10B981),
                                                iconBgColor = Color(0xFF10B981).copy(alpha = 0.15f),
                                                selected = goalStr == "Weight Loss",
                                                onClick = { goalStr = "Weight Loss" }
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            OnboardingGoalCard(
                                                title = "Weight Gain",
                                                description = "Increase body mass and caloric intake capacity",
                                                icon = Icons.Rounded.TrendingUp,
                                                iconColor = Color(0xFF3B82F6),
                                                iconBgColor = Color(0xFF3B82F6).copy(alpha = 0.15f),
                                                selected = goalStr == "Weight Gain",
                                                onClick = { goalStr = "Weight Gain" }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            OnboardingGoalCard(
                                                title = "Muscle Building",
                                                description = "Optimize protein synthesis and gain strength",
                                                icon = Icons.Rounded.FitnessCenter,
                                                iconColor = Color(0xFFF59E0B),
                                                iconBgColor = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                                selected = goalStr == "Muscle Building",
                                                onClick = { goalStr = "Muscle Building" }
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            OnboardingGoalCard(
                                                title = "General Fitness",
                                                description = "Maintain weight, increase energy, and build health",
                                                icon = Icons.Rounded.Favorite,
                                                iconColor = Color(0xFFEC4899),
                                                iconBgColor = Color(0xFFEC4899).copy(alpha = 0.15f),
                                                selected = goalStr == "General Fitness",
                                                onClick = { goalStr = "General Fitness" }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = targetWeightStr,
                                    onValueChange = { targetWeightStr = it },
                                    label = { Text("Target Weight (kg)") },
                                    singleLine = true,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = PrimaryGreen, unfocusedBorderColor = GlassBorder)
                                )
                            }
                            4 -> {
                                // Step 4: Activity Level
                                Text(
                                    text = "Select your daily activity level:",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val activities = listOf(
                                        "Sedentary" to ("Very little active movement or office desk profile (1.2x)" to Icons.Rounded.DirectionsWalk),
                                        "Light Activity" to ("Light exercise or active 1-2 times weekly (1.375x)" to Icons.Rounded.DirectionsRun),
                                        "Moderate Activity" to ("Active gym sessions 3-5 times weekly (1.55x)" to Icons.Rounded.FitnessCenter),
                                        "Active" to ("High energy workouts or drills 6-7 times weekly (1.725x)" to Icons.Rounded.Bolt),
                                        "Athlete" to ("Professional intensity double-daily routines (1.9x)" to Icons.Rounded.EmojiEvents)
                                    )
                                    activities.forEach { (title, data) ->
                                        val (desc, icon) = data
                                        val selected = activityLevelStr == title
                                        OnboardingSelectionCard(
                                            title = title,
                                            description = desc,
                                            icon = icon,
                                            iconColor = Cyan,
                                            selected = selected,
                                            onClick = { activityLevelStr = title }
                                        )
                                    }
                                }
                            }
                            5 -> {
                                // Step 5: Dietary Preference
                                Text(
                                    text = "Select your dietary preference:",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val diets = listOf(
                                        "Vegetarian" to ("Excludes poultry and red meats" to Icons.Rounded.Spa),
                                        "Non-Vegetarian" to ("Includes all proteins, poultry, and seafood" to Icons.Rounded.Restaurant),
                                        "Vegan" to ("Excludes dairy, eggs, and all animal products" to Icons.Rounded.Spa),
                                        "Eggetarian" to ("Vegetarian diet but includes eggs" to Icons.Rounded.Egg),
                                        "Balanced Diet" to ("Balanced nutrients with all food groups" to Icons.Rounded.RestaurantMenu)
                                    )
                                    diets.forEach { (title, data) ->
                                        val (desc, icon) = data
                                        val selected = dietaryPreferenceStr == title
                                        OnboardingSelectionCard(
                                            title = title,
                                            description = desc,
                                            icon = icon,
                                            iconColor = Cyan,
                                            selected = selected,
                                            onClick = { dietaryPreferenceStr = title }
                                        )
                                    }
                                }
                            }
                            6 -> {
                                // Step 6: Review Summary
                                Card(
                                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Biological Review", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                        Divider(color = GlassBorder)

                                        ReviewRow("Username", name)
                                        ReviewRow("Email", email)
                                        ReviewRow("Gender / Age", "$genderStr, $ageStr yrs")
                                        ReviewRow("Height / Weight", "${heightStr}cm / ${weightStr}kg")
                                        ReviewRow("Goal Objective", goalStr)
                                        ReviewRow("Target Weight", "${targetWeightStr} kg")
                                        ReviewRow("Activity Level", activityLevelStr)
                                        ReviewRow("Dietary Choice", dietaryPreferenceStr)
                                    }
                                }
                            }
                        }
                    }

                    if (error.isNotEmpty()) {
                        Text(error, color = Color(0xFFEF4444), fontSize = 12.sp)
                    }

                    // Navigation Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isLogin && signupStep > 1) {
                            Button(
                                onClick = { signupStep -= 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                if (isLogin) {
                                    if (email.isBlank() || password.isBlank()) {
                                        error = "Please fill in all credentials."
                                        return@Button
                                    }
                                    error = ""
                                    isLoading = true
                                    scope.launch {
                                        val result = authRepository.login(email, password)
                                        isLoading = false
                                        when (result) {
                                            is AuthResult.Success -> onLoginSuccess()
                                            is AuthResult.Failure -> error = result.message
                                        }
                                    }
                                } else {
                                    // SignUp Steps transitions
                                    if (signupStep < 6) {
                                        error = ""
                                        if (signupStep == 1 && !isStep1Valid) {
                                            error = "Credentials incomplete or password requirements not satisfied."
                                            return@Button
                                        }
                                        if (signupStep == 2 && !isStep2Valid) {
                                            error = "Biometric numbers are required."
                                            return@Button
                                        }
                                        if (signupStep == 3 && !isStep3Valid) {
                                            error = "Goals parameters are required."
                                            return@Button
                                        }
                                        signupStep += 1
                                    } else {
                                        // Step 6: Save & Create Account
                                        error = ""
                                        isLoading = true
                                        scope.launch {
                                            val result = authRepository.signUp(email, password, name)
                                            if (result is AuthResult.Success) {
                                                // Mifflin-St Jeor formulas
                                                val h = heightStr.toDoubleOrNull() ?: 165.0
                                                val w = weightStr.toDoubleOrNull() ?: 68.0
                                                val a = ageStr.toIntOrNull() ?: 28
                                                val targetW = targetWeightStr.toDoubleOrNull() ?: 62.0

                                                val calculatedBmr = if (genderStr == "Male") {
                                                    10.0 * w + 6.25 * h - 5.0 * a + 5.0
                                                } else {
                                                    10.0 * w + 6.25 * h - 5.0 * a - 161.0
                                                }

                                                val activityMultiplier = when (activityLevelStr) {
                                                    "Sedentary" -> 1.2
                                                    "Light Activity", "Light" -> 1.375
                                                    "Moderate Activity", "Moderate" -> 1.55
                                                    "Active" -> 1.725
                                                    "Athlete" -> 1.9
                                                    else -> 1.55
                                                }

                                                val calculatedTdee = calculatedBmr * activityMultiplier
                                                val targetCalories = when (goalStr) {
                                                    "Weight Loss" -> calculatedTdee - 500
                                                    "Weight Gain" -> calculatedTdee + 500
                                                    "Muscle Building" -> calculatedTdee + 300
                                                    else -> calculatedTdee
                                                }.coerceIn(1200.0, 5000.0).toInt()

                                                val bmi = w / ((h / 100.0) * (h / 100.0))

                                                val proteinMultiplier = when (goalStr) {
                                                    "Weight Loss" -> 2.0
                                                    "Weight Gain" -> 1.8
                                                    "Muscle Building" -> 2.2
                                                    else -> 1.6
                                                }

                                                val proteinTarget = (w * proteinMultiplier).toInt()
                                                val fatTarget = ((targetCalories * 0.25) / 9).toInt()
                                                val carbTarget = ((targetCalories - (proteinTarget * 4) - (fatTarget * 9)) / 4).toInt()
                                                val waterTarget = (w * 35.0).toInt()

                                                val syncTime = getCurrentTimestamp()

                                                val initialProfile = UserProfile(
                                                    name = name,
                                                    email = email,
                                                    goal = goalStr,
                                                    targetCalories = targetCalories,
                                                    targetProtein = proteinTarget,
                                                    targetCarbs = carbTarget,
                                                    targetFats = fatTarget,
                                                    weight = w,
                                                    targetWeight = targetW,
                                                    height = h,
                                                    age = a,
                                                    gender = genderStr,
                                                    activityLevel = activityLevelStr,
                                                    dietaryPreference = dietaryPreferenceStr,
                                                    bmi = bmi,
                                                    bmr = calculatedBmr,
                                                    tdee = calculatedTdee,
                                                    targetWater = waterTarget,
                                                    showOnboarding = false, // completed wizard here
                                                    lastSynced = syncTime
                                                )
                                                userRepository.saveUserProfile(result.uid, initialProfile)
                                                isLoading = false
                                                onLoginSuccess()
                                            } else if (result is AuthResult.Failure) {
                                                isLoading = false
                                                error = result.message
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            modifier = Modifier.weight(2f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = if (isLogin) "Sign In" else if (signupStep == 6) "Create Account" else "Continue",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.clickable { 
                    isLogin = !isLogin
                    signupStep = 1
                    error = ""
                }
            ) {
                Text(
                    text = if (isLogin) "Don't have an account? " else "Already have an account? ",
                    fontSize = 14.sp,
                    color = GrayText
                )
                Text(
                    text = if (isLogin) "Sign Up" else "Sign In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }

        // Forgot Password Dialog
        if (showResetDialog) {
            var resetEmail by remember { mutableStateOf(email) }
            var resetMessage by remember { mutableStateOf("") }
            var isResetting by remember { mutableStateOf(false) }
            
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Password", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter your email address and we will send you a password reset link.", color = GrayText, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = GlassBorder
                            )
                        )
                        if (resetMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(resetMessage, color = if (resetMessage.startsWith("Success")) PrimaryGreen else Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (resetEmail.isBlank()) {
                                resetMessage = "Please enter an email address."
                                return@Button
                            }
                            resetMessage = ""
                            isResetting = true
                            scope.launch {
                                val res = authRepository.sendPasswordResetEmail(resetEmail)
                                isResetting = false
                                if (res is AuthResult.Success) {
                                    resetMessage = "Success! Check your inbox for a reset email."
                                } else if (res is AuthResult.Failure) {
                                    resetMessage = res.message
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        enabled = !isResetting
                    ) {
                        if (isResetting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Send Link", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showResetDialog = false }
                    ) {
                        Text("Cancel", color = PrimaryGreen)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = GrayText)
        Text(value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

enum class TimelineStatus {
    Completed,
    Active,
    Pending
}

@Composable
fun OnboardingTimeline(signupStep: Int) {
    val steps = listOf("Account", "Body Metrics", "Goal", "Lifestyle", "Complete")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        // Connecting Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.08f))
                .align(Alignment.TopCenter)
                .padding(horizontal = 28.dp)
                .offset(y = 13.dp) // align vertically in the middle of circles
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            steps.forEachIndexed { index, title ->
                val stepNum = index + 1
                val status = when (stepNum) {
                    1 -> TimelineStatus.Completed // Account is always complete when user is doing onboarding steps
                    2 -> when {
                        signupStep > 2 -> TimelineStatus.Completed
                        signupStep == 2 -> TimelineStatus.Active
                        else -> TimelineStatus.Pending
                    }
                    3 -> when {
                        signupStep > 3 -> TimelineStatus.Completed
                        signupStep == 3 -> TimelineStatus.Active
                        else -> TimelineStatus.Pending
                    }
                    4 -> when {
                        signupStep > 5 -> TimelineStatus.Completed
                        signupStep == 4 || signupStep == 5 -> TimelineStatus.Active
                        else -> TimelineStatus.Pending
                    }
                    5 -> when {
                        signupStep == 6 -> TimelineStatus.Active
                        else -> TimelineStatus.Pending
                    }
                    else -> TimelineStatus.Pending
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                when (status) {
                                    TimelineStatus.Completed -> PrimaryGreen
                                    else -> DarkBackground // masks the background line
                                }
                            )
                            .border(
                                1.dp,
                                when (status) {
                                    TimelineStatus.Completed -> PrimaryGreen
                                    TimelineStatus.Active -> Cyan
                                    TimelineStatus.Pending -> Color.White.copy(alpha = 0.15f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (status == TimelineStatus.Completed) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = stepNum.toString(),
                                color = if (status == TimelineStatus.Active) Color.White else GrayText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = title,
                        color = if (status == TimelineStatus.Pending) GrayText else Color.White,
                        fontSize = 9.sp,
                        fontWeight = if (status == TimelineStatus.Active) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingGoalCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (selected) 1.03f else 1.0f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                1.dp,
                if (selected) Cyan else GlassBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Cyan.copy(alpha = 0.08f) else GlassSurface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Cyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, color = GrayText, fontSize = 10.sp, lineHeight = 13.sp, minLines = 2)
        }
    }
}

@Composable
fun OnboardingSelectionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (selected) 1.02f else 1.0f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                1.dp,
                if (selected) Cyan else GlassBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Cyan.copy(alpha = 0.08f) else GlassSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (selected) Cyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Cyan else GrayText,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(description, color = GrayText, fontSize = 11.sp, lineHeight = 13.sp)
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
