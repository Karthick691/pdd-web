package com.foodsnap.nutritionai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.api.ApiService
import com.foodsnap.nutritionai.model.*
import com.foodsnap.nutritionai.screens.*
import com.foodsnap.nutritionai.theme.*
import com.foodsnap.nutritionai.auth.getAuthRepository
import com.foodsnap.nutritionai.repository.*
import kotlinx.coroutines.launch

@Composable
fun App() {
    val authRepository = remember { getAuthRepository() }
    var isLoggedIn by remember { mutableStateOf(authRepository.isUserLoggedIn()) }
    
    val apiService = remember { 
        ApiService().apply {
            onAuthExpired = { isLoggedIn = false }
        }
    }
    val userRepo = remember { getUserRepository() }
    val foodLogRepo = remember { getFoodLogRepository() }
    val waterRepo = remember { getWaterRepository() }
    val chatRepo = remember { getChatRepository() }
    val scope = rememberCoroutineScope()
    
    var userProfile by remember { mutableStateOf(UserProfile()) }
    val loggedFoods = remember { mutableStateListOf<FoodLogEntry>() }
    val exerciseLogs = remember { mutableStateListOf<ExerciseLogEntry>() }
    var waterIntake by remember { mutableStateOf(0) }
    
    var isDarkTheme by remember { mutableStateOf(true) }
    var showChatbotOverlay by remember { mutableStateOf(false) }

    // Custom backstack router
    val initialScreen = if (isLoggedIn) Screen.Home else Screen.Splash
    val screenBackStack = remember { mutableStateOf<List<Screen>>(listOf(initialScreen)) }
    
    fun navigateTo(screen: Screen) {
        val finalScreen = when (screen) {
            is Screen.ExportReports,
            is Screen.ProgressAnalytics,
            is Screen.ConnectedDevices -> Screen.Subscription
            else -> screen
        }
        screenBackStack.value = screenBackStack.value + finalScreen
    }
    
    fun popBack() {
        if (screenBackStack.value.size > 1) {
            screenBackStack.value = screenBackStack.value.dropLast(1)
        }
    }
    
    fun resetTo(screen: Screen) {
        screenBackStack.value = listOf(screen)
    }
 
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val uid = authRepository.getUserId()
            if (uid != null) {
                var profile = userRepo.getUserProfile(uid)
                if (profile != null) {
                    profile = profile.copy(lastSynced = getCurrentTimestamp())
                    userProfile = profile
                    if (profile.showOnboarding) {
                        resetTo(Screen.Onboarding(1))
                    } else {
                        resetTo(Screen.Home)
                    }
                } else {
                    val defaultProfile = UserProfile(
                        email = authRepository.getUserEmail() ?: "",
                        showOnboarding = true,
                        lastSynced = getCurrentTimestamp()
                    )
                    userProfile = defaultProfile
                    userRepo.saveUserProfile(uid, defaultProfile)
                    resetTo(Screen.Onboarding(1))
                }
                
                val foodLogs = foodLogRepo.getFoodLogs(uid)
                loggedFoods.clear()
                loggedFoods.addAll(foodLogs)
                
                val waterLogs = waterRepo.getWaterLogs(uid)
                val waterSum = waterLogs.sumOf { it.amount }
                waterIntake = waterSum

                val exRepo = getExerciseRepository()
                val exLogs = exRepo.getExerciseLogs(uid)
                exerciseLogs.clear()
                exerciseLogs.addAll(exLogs)
            }
        } else {
            resetTo(Screen.Splash)
        }
    }

    val currentCalories = loggedFoods.sumOf { it.calories }
    val caloriesBurned = exerciseLogs.sumOf { it.caloriesBurned }
    val currentProtein = loggedFoods.sumOf { it.protein }
    val currentCarbs = loggedFoods.sumOf { it.carbs }
    val currentFats = loggedFoods.sumOf { it.fats }

    val currentScreen = screenBackStack.value.lastOrNull() ?: Screen.Splash
    
    // Determine whether to show top app bar and bottom bar
    val showTopAndBottomBars = isLoggedIn && currentScreen !is Screen.Onboarding && currentScreen !is Screen.Splash

    FoodSnapTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                if (showTopAndBottomBars) {
                    CustomTopAppBar(
                        currentScreen = currentScreen,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme },
                        onNavigateToScan = { navigateTo(Screen.FoodScanner) },
                        onNavigateToProfile = { navigateTo(Screen.Profile) }
                    )
                }
            },
            bottomBar = {
                if (showTopAndBottomBars) {
                    CustomFloatingBottomBar(
                        currentScreen = currentScreen,
                        onNavigate = { screen -> resetTo(screen) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(if (showTopAndBottomBars) innerPadding else PaddingValues(0.dp))
            ) {
                when (currentScreen) {
                    // Auth
                    is Screen.Splash -> SplashScreen(onFinished = {
                        if (isLoggedIn) {
                            if (userProfile.showOnboarding) resetTo(Screen.Onboarding(1))
                            else resetTo(Screen.Home)
                        } else {
                            resetTo(Screen.Welcome)
                        }
                    })
                    is Screen.Onboarding -> OnboardingScreen(
                        userProfile = userProfile,
                        onComplete = { completedProfile ->
                            userProfile = completedProfile
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    userRepo.saveUserProfile(uid, completedProfile)
                                }
                            }
                            resetTo(Screen.Home)
                        }
                    )
                    is Screen.Welcome -> Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.Restaurant, contentDescription = "FoodSnap", tint = NeonGreen, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Welcome to FoodSnap AI", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Premium nutrition and meal tracking", fontSize = 14.sp, color = GrayText)
                            Spacer(modifier = Modifier.height(48.dp))
                            Button(
                                onClick = { navigateTo(Screen.Login) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                            ) {
                                Text("Sign In", color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { navigateTo(Screen.Register) },
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("Create Account", color = NeonGreen)
                            }
                        }
                    }
                    is Screen.Login -> LoginSignupScreen(
                        userRepository = userRepo,
                        onLoginSuccess = { isLoggedIn = true }
                    )
                    is Screen.Register -> Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Register Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Please use the Sign In screen register portal link to sign up.", color = GrayText, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(30.dp))
                            Button(onClick = { resetTo(Screen.Login) }) {
                                Text("Go to Sign In Portal")
                            }
                        }
                    }
                    is Screen.ForgotPassword -> Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Reset Password", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(30.dp))
                            Button(onClick = { popBack() }) {
                                Text("← Back")
                            }
                        }
                    }
                    is Screen.VerifyEmail -> Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Verify Email", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(30.dp))
                            Button(onClick = { resetTo(Screen.Login) }) {
                                Text("Go to Login")
                            }
                        }
                    }

                    // Home Screen
                    is Screen.Home -> HomeScreen(
                        userProfile = userProfile,
                        currentCalories = currentCalories,
                        waterMl = waterIntake,
                        currentProtein = currentProtein,
                        onNavigateToScan = { navigateTo(Screen.FoodScanner) },
                        onNavigateToTracker = { navigateTo(Screen.DailySummary) },
                        onNavigateToDiet = { navigateTo(Screen.GoalPlanner) },
                        onNavigateToExercise = { navigateTo(Screen.WorkoutDashboard) },
                        onAddWater = { amount ->
                            waterIntake += amount
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    waterRepo.addWaterLog(uid, WaterLogEntry(amount, getCurrentTimestamp()))
                                }
                            }
                        }
                    )

                    // Dashboard Screen
                    is Screen.Dashboard -> DashboardScreen(
                        currentCalories = currentCalories,
                        targetCalories = userProfile.targetCalories,
                        caloriesBurned = caloriesBurned,
                        currentProtein = currentProtein,
                        targetProtein = userProfile.targetProtein,
                        currentCarbs = currentCarbs,
                        targetCarbs = userProfile.targetCarbs,
                        currentFats = currentFats,
                        targetFats = userProfile.targetFats,
                        waterMl = waterIntake,
                        currentWeight = userProfile.weight,
                        exerciseLogs = exerciseLogs,
                        onAddWater = { amount ->
                            waterIntake += amount
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    waterRepo.addWaterLog(uid, WaterLogEntry(amount, getCurrentTimestamp()))
                                }
                            }
                        },
                        onNavigateToScan = { navigateTo(Screen.FoodScanner) },
                        onNavigateToTracker = { navigateTo(Screen.DailySummary) },
                        onNavigateToChat = { navigateTo(Screen.AICoach) }
                    )

                    // Tracker Screen
                    is Screen.DailySummary -> DailyTrackerScreen(
                        loggedFoods = loggedFoods,
                        currentCalories = currentCalories,
                        targetCalories = userProfile.targetCalories,
                        waterMl = waterIntake,
                        onAddWater = { amount ->
                            waterIntake += amount
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    waterRepo.addWaterLog(uid, WaterLogEntry(amount, getCurrentTimestamp()))
                                }
                            }
                        },
                        onResetWater = {
                            waterIntake = 0
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    waterRepo.clearWaterLogs(uid)
                                }
                            }
                        },
                        onClearLogs = {
                            loggedFoods.clear()
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    foodLogRepo.clearFoodLogs(uid)
                                }
                            }
                        },
                        onMealClick = { entry ->
                            navigateTo(Screen.MealDetail(entry))
                        }
                    )

                    // Food Detail
                    is Screen.MealDetail -> MealDetailScreen(
                        entry = currentScreen.entry,
                        onBack = { popBack() },
                        onEdit = { popBack() },
                        onDelete = {
                            val entryToDelete = currentScreen.entry
                            loggedFoods.remove(entryToDelete)
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    foodLogRepo.deleteFoodLog(uid, entryToDelete.id)
                                    val foodLogs = foodLogRepo.getFoodLogs(uid)
                                    loggedFoods.clear()
                                    loggedFoods.addAll(foodLogs)
                                }
                            }
                            popBack()
                        },
                        onDuplicate = {
                            val entryToDuplicate = currentScreen.entry
                            val duplicatedEntry = entryToDuplicate.copy(
                                id = "",
                                timestamp = getCurrentTimestamp()
                            )
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    foodLogRepo.addFoodLog(uid, duplicatedEntry)
                                    val foodLogs = foodLogRepo.getFoodLogs(uid)
                                    loggedFoods.clear()
                                    loggedFoods.addAll(foodLogs)
                                }
                            } else {
                                loggedFoods.add(duplicatedEntry)
                            }
                            popBack()
                        },
                        onAddToFavorites = { popBack() },
                        onShare = { popBack() }
                    )

                    // Food Scanner
                    is Screen.FoodScanner -> FoodScanScreen(
                        onLogFood = { name, cal, p, c, f ->
                            val newEntry = FoodLogEntry(
                                foodName = name,
                                calories = cal,
                                protein = p,
                                carbs = c,
                                fats = f,
                                timestamp = getCurrentTimestamp()
                            )
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    foodLogRepo.addFoodLog(uid, newEntry)
                                    val foodLogs = foodLogRepo.getFoodLogs(uid)
                                    loggedFoods.clear()
                                    loggedFoods.addAll(foodLogs)
                                }
                            } else {
                                loggedFoods.add(newEntry)
                            }
                        },
                        analyzeImage = { bytes, filename ->
                            apiService.analyzeFoodImage(bytes, filename)
                        }
                    )

                    // AI Full Screen Assistant
                    is Screen.AICoach -> AIChatAssistantScreen(
                        chatHandler = { message ->
                            try {
                                val latestWorkout = exerciseLogs.lastOrNull()
                                apiService.chatWithAI(
                                    message = message,
                                    goal = userProfile.goal,
                                    height = userProfile.height,
                                    weight = userProfile.weight,
                                    waterMl = waterIntake,
                                    foodCals = currentCalories,
                                    exerciseMins = exerciseLogs.sumOf { it.durationMinutes },
                                    exerciseCals = caloriesBurned,
                                    targetCalories = userProfile.targetCalories,
                                    currentProtein = currentProtein,
                                    currentCarbs = currentCarbs,
                                    currentFats = currentFats,
                                    targetProtein = userProfile.targetProtein,
                                    targetCarbs = userProfile.targetCarbs,
                                    targetFats = userProfile.targetFats,
                                    bmr = userProfile.bmr,
                                    tdee = userProfile.tdee,
                                    bmi = userProfile.bmi,
                                    age = userProfile.age,
                                    gender = userProfile.gender,
                                    activityLevel = userProfile.activityLevel,
                                    dietaryPreference = userProfile.dietaryPreference,
                                    targetWater = userProfile.targetWater,
                                    latestWorkoutName = latestWorkout?.name,
                                    latestWorkoutDuration = latestWorkout?.durationMinutes ?: 0,
                                    latestWorkoutCalories = latestWorkout?.caloriesBurned ?: 0,
                                    latestWorkoutHeartRate = latestWorkout?.heartRate ?: 0
                                )
                            } catch (e: Exception) {
                                println("[App.kt chatHandler] Error occurred contacting backend AI model server:")
                                e.printStackTrace()
                                throw e
                            }
                        },
                        userProfile = userProfile,
                        waterMl = waterIntake,
                        foodCals = currentCalories,
                        exerciseMins = exerciseLogs.sumOf { it.durationMinutes },
                        exerciseCals = caloriesBurned,
                        chatRepository = chatRepo,
                        userId = authRepository.getUserId() ?: ""
                    )

                    // Exercise Screen
                    is Screen.WorkoutDashboard -> ExerciseScreen(
                        userProfile = userProfile,
                        exerciseLogs = exerciseLogs,
                        onLogExercise = { name, dur, cals, intensity, heartRate ->
                            val newLog = ExerciseLogEntry(
                                id = "",
                                name = name,
                                durationMinutes = dur,
                                caloriesBurned = cals,
                                intensity = intensity,
                                heartRate = heartRate,
                                timestamp = getCurrentTimestamp()
                            )
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    val exRepo = getExerciseRepository()
                                    exRepo.addExerciseLog(uid, newLog)
                                    val logs = exRepo.getExerciseLogs(uid)
                                    exerciseLogs.clear()
                                    exerciseLogs.addAll(logs)
                                }
                            } else {
                                exerciseLogs.add(newLog)
                            }
                        },
                        onClearLogs = {
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    val exRepo = getExerciseRepository()
                                    exRepo.clearExerciseLogs(uid)
                                    exerciseLogs.clear()
                                }
                            } else {
                                exerciseLogs.clear()
                            }
                        }
                    )

                    // Diet Plans Screen
                    is Screen.GoalPlanner -> DietPlansScreen(
                        userProfile = userProfile,
                        onSelectPlan = { name, cal, prot, carb, fat ->
                            userProfile = userProfile.copy(
                                goal = name,
                                targetCalories = cal,
                                targetProtein = prot,
                                targetCarbs = carb,
                                targetFats = fat
                            )
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    userRepo.saveUserProfile(uid, userProfile)
                                }
                            }
                        }
                    )

                    // Profile Screen
                    is Screen.Profile -> ProfileScreen(
                        userProfile = userProfile,
                        onSaveProfile = { newProfile ->
                            userProfile = newProfile
                            val uid = authRepository.getUserId()
                            if (uid != null) {
                                scope.launch {
                                    userRepo.saveUserProfile(uid, newProfile)
                                }
                            }
                        },
                        onNavigateToSubscription = { navigateTo(Screen.Subscription) },
                        onOpenDietPlans = { navigateTo(Screen.GoalPlanner) },
                        onLogout = {
                            authRepository.logout()
                            isLoggedIn = false
                        }
                    )

                    // Social Screens
                    is Screen.CommunityFeed -> SocialDashboardScreen(
                        onBack = { popBack() },
                        onNavigateToLeaderboard = { navigateTo(Screen.Leaderboard) },
                        onNavigateToChallenges = { navigateTo(Screen.Challenges) },
                        onNavigateToAchievements = { navigateTo(Screen.AchievementCenter) }
                    )
                    is Screen.Leaderboard -> LeaderboardScreen(onBack = { popBack() })
                    is Screen.Challenges -> ChallengesScreen(onBack = { popBack() })
                    is Screen.AchievementCenter -> AchievementsScreen(onBack = { popBack() })

                    // Fitness Screens
                    is Screen.ActivityAnalytics -> FitnessDashboardScreen(
                        onBack = { popBack() },
                        onNavigateToExerciseHistory = { navigateTo(Screen.ExerciseHistory) },
                        onNavigateToStepCounter = { navigateTo(Screen.StepCounter) },
                        onNavigateToCalBurn = { navigateTo(Screen.CaloriesBurned) },
                        onNavigateToAnalytics = { navigateTo(Screen.ProgressAnalytics) }
                    )
                    is Screen.StepCounter -> StepCounterScreen(onBack = { popBack() })

                    is Screen.DeveloperSettings -> DeveloperSettingsScreen(
                        apiService = apiService,
                        onBack = { popBack() }
                    )
                    is Screen.Subscription -> SubscriptionScreen(
                        onBack = { popBack() }
                    )
                    // Dynamic Universal Screen for settings and calculators
                    else -> GenericUniversalScreen(
                        screen = currentScreen,
                        userProfile = userProfile,
                        waterMl = waterIntake,
                        currentCalories = currentCalories,
                        onBack = { popBack() }
                    )
                }
            }
        }

        // Floating pulsing AI chatbot assistant button (visible when logged in)
        if (showTopAndBottomBars) {
            val infiniteTransition = rememberInfiniteTransition()
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.96f,
                targetValue = 1.04f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonGreen, Cyan))
                        )
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { showChatbotOverlay = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.SmartToy, contentDescription = "AI Assistant", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }

        // Context-aware chatbot overlay panel
        if (showChatbotOverlay) {
            ChatbotOverlayPanel(
                userProfile = userProfile,
                waterMl = waterIntake,
                calories = currentCalories,
                chatHandler = { msg ->
                    try {
                        val latestWorkout = exerciseLogs.lastOrNull()
                        apiService.chatWithAI(
                            message = msg,
                            goal = userProfile.goal,
                            height = userProfile.height,
                            weight = userProfile.weight,
                            waterMl = waterIntake,
                            foodCals = currentCalories,
                            exerciseMins = exerciseLogs.sumOf { it.durationMinutes },
                            exerciseCals = caloriesBurned,
                            targetCalories = userProfile.targetCalories,
                            currentProtein = currentProtein,
                            currentCarbs = currentCarbs,
                            currentFats = currentFats,
                            targetProtein = userProfile.targetProtein,
                            targetCarbs = userProfile.targetCarbs,
                            targetFats = userProfile.targetFats,
                            bmr = userProfile.bmr,
                            tdee = userProfile.tdee,
                            bmi = userProfile.bmi,
                            age = userProfile.age,
                            gender = userProfile.gender,
                            activityLevel = userProfile.activityLevel,
                            dietaryPreference = userProfile.dietaryPreference,
                            targetWater = userProfile.targetWater,
                            latestWorkoutName = latestWorkout?.name,
                            latestWorkoutDuration = latestWorkout?.durationMinutes ?: 0,
                            latestWorkoutCalories = latestWorkout?.caloriesBurned ?: 0,
                            latestWorkoutHeartRate = latestWorkout?.heartRate ?: 0
                        )
                    } catch (e: Exception) {
                        println("[App.kt ChatbotOverlayPanel chatHandler] Error occurred contacting backend AI model server:")
                        e.printStackTrace()
                        throw e
                    }
                },
                onDismiss = { showChatbotOverlay = false }
            )
        }
    }
}

// Custom Top App Bar (Stable layout with Row)
@Composable
fun CustomTopAppBar(
    currentScreen: Screen,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val title = when (currentScreen) {
        is Screen.Home -> "HEALTH MATRIX"
        is Screen.Dashboard -> "ANALYTICS PANEL"
        is Screen.GoalPlanner -> "DIET PLANS"
        is Screen.DailySummary -> "MEAL TRACKER"
        is Screen.WorkoutDashboard -> "EXERCISE LOGS"
        is Screen.Profile -> "PROFILE SETTINGS"
        else -> currentScreen::class.simpleName?.uppercase() ?: "FOODSNAP AI"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .clickable { onNavigateToProfile() },
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 16.sp)
        }

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.5.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                contentDescription = "Toggle Theme",
                tint = Color.White,
                modifier = Modifier.size(22.dp).clickable { onToggleTheme() }
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlassSurface)
                    .border(1.dp, NeonGreen, RoundedCornerShape(10.dp))
                    .clickable { onNavigateToScan() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = "Scan", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CustomFloatingBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(NeonGreen.copy(alpha = 0.8f), Cyan.copy(alpha = 0.8f))),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurface.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    selected = currentScreen == Screen.Dashboard,
                    onClick = { onNavigate(Screen.Dashboard) },
                    icon = Icons.Rounded.BarChart,
                    label = "Dashboard"
                )
                BottomBarItem(
                    selected = currentScreen == Screen.GoalPlanner,
                    onClick = { onNavigate(Screen.GoalPlanner) },
                    icon = Icons.Rounded.Restaurant,
                    label = "Diet Plans"
                )
                
                Spacer(modifier = Modifier.width(64.dp))
                
                BottomBarItem(
                    selected = currentScreen == Screen.DailySummary,
                    onClick = { onNavigate(Screen.DailySummary) },
                    icon = Icons.Rounded.Timer,
                    label = "Tracker"
                )
                BottomBarItem(
                    selected = currentScreen == Screen.WorkoutDashboard,
                    onClick = { onNavigate(Screen.WorkoutDashboard) },
                    icon = Icons.Rounded.FitnessCenter,
                    label = "Exercise"
                )
            }
        }
        
        Box(
            modifier = Modifier
                .offset(y = (-16).dp)
                .scale(if (currentScreen == Screen.Home) 1.15f else 1.0f)
                .size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentScreen == Screen.Home) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Cyan.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonGreen, Cyan)
                        )
                    )
                    .border(3.dp, DarkBackground, CircleShape)
                    .clickable { onNavigate(Screen.Home) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Home, contentDescription = "Home", tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
fun RowScope.BottomBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    val scale by animateFloatAsState(if (selected) 1.15f else 1.0f)
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val activeColor = NeonGreen
        val inactiveColor = GrayText
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.scale(scale)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(NeonGreen.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                )
            }
            Icon(
                icon,
                contentDescription = label,
                tint = if (selected) activeColor else inactiveColor,
                modifier = Modifier.size(if (selected) 24.dp else 20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) activeColor else inactiveColor
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .background(activeColor, CircleShape)
            )
        }
    }
}

// Floating Chatbot Overlay Panel (Context-aware)
@Composable
fun ChatbotOverlayPanel(
    userProfile: UserProfile,
    waterMl: Int,
    calories: Int,
    chatHandler: suspend (String) -> String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var inputMessage by remember { mutableStateOf("") }
    val messages = remember { 
        mutableStateListOf<ChatMessage>(
            ChatMessage("assistant", "Hello! I am your FoodSnap AI Coach. I have calibrated your goal (${userProfile.goal}), height (${userProfile.height}cm), weight (${userProfile.weight}kg), and today's intake data. Ask me anything!", "12:00")
        )
    }
    var loadingReply by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
                .clickable(enabled = false) {}, // prevent closing on panel click
            colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SmartToy, contentDescription = "AI", tint = Cyan, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("FoodSnap AI Assistant", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Context Engine active", fontSize = 11.sp, color = NeonGreen)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }

                Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Chat history list
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages) { msg ->
                            val isAssistant = msg.role == "assistant"
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isAssistant) Alignment.CenterStart else Alignment.CenterEnd
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isAssistant) Color(0x1106B6D4) else NeonGreen.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (loadingReply) {
                    LinearProgressIndicator(color = NeonGreen, modifier = Modifier.fillMaxWidth().height(2.dp).padding(vertical = 4.dp))
                }

                // Chat input controls
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Ask about meal recipes or goals...") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GlassBorder
                        )
                    )
                    Button(
                        onClick = {
                            if (inputMessage.isNotBlank()) {
                                val userText = inputMessage
                                messages.add(ChatMessage("user", userText, "12:00"))
                                inputMessage = ""
                                loadingReply = true
                                scope.launch {
                                    try {
                                        val response = chatHandler(userText)
                                        messages.add(ChatMessage("assistant", response, "12:00"))
                                    } catch (e: Exception) {
                                        messages.add(ChatMessage("assistant", "Apologies, I couldn't reach the AI model server. Check Ktor host endpoints.", "12:00"))
                                    } finally {
                                        loadingReply = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}

// Universal fallback container for the remaining settings, details, and report screens
@Composable
fun GenericUniversalScreen(
    screen: Screen,
    userProfile: UserProfile,
    waterMl: Int,
    currentCalories: Int,
    onBack: () -> Unit
) {
    val title = screen::class.simpleName ?: "Universal Screen"
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simple back title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GlassSurface)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("Back", color = Color.White)
                }
            }
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.width(60.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Layout Injectors depending on Screen SimpleName
        when (title) {
            "BmiCalculator" -> BmiCalculatorWidget(userProfile)
            "BmrCalculator" -> BmrCalculatorWidget(userProfile)
            "TdeeCalculator" -> TdeeCalculatorWidget(userProfile)
            "MacroCalculator" -> MacroCalculatorWidget(userProfile)
            "GrocerySuggestions" -> SmartGroceryWidget()
            "Favorites", "SavedMeals" -> SavedItemsWidget()
            "StreakCenter", "AchievementCenter" -> StreakCenterWidget()
            "WeeklySummary", "MonthlySummary", "FoodHistory", "WaterHistory" -> HistoryWidget(title, waterMl, currentCalories)
            "AppearanceSettings" -> AppearanceSettingsWidget()
            "AiPreferences" -> AiPreferencesWidget()
            "ConnectedDevices" -> ConnectedDevicesWidget()
            "HealthInsights", "SmartInsights" -> HealthInsightsWidget(userProfile)
            "NutritionBreakdown" -> NutritionBreakdownWidget(currentCalories)
            "CompareFoods" -> CompareFoodsWidget()
            "ChatHistory" -> ChatHistoryWidget(getChatRepository(), userProfile.email)
            "PersonalizedRecommendations", "MealSuggestions" -> MealSuggestionsWidget()
            "ExerciseHistory" -> ExerciseHistoryWidget(emptyList())
            "CaloriesBurned" -> CaloriesBurnedWidget(emptyList())
            "WaterTracker" -> WaterTrackerWidget(waterMl, userProfile.targetWater)
            "WeightTracker", "ProgressAnalytics" -> WeightTrackerWidget(userProfile)
            "Settings", "NotificationSettings", "PrivacySettings" -> SettingsWidget()
            "Subscription", "PremiumFeatures" -> PremiumFeaturesWidget()
            "About" -> AboutWidget()
            "NutritionEducation" -> NutritionEducationWidget()
            "NotificationsCenter" -> NotificationsCenterWidget()
            "MealReminders", "WaterReminders", "WorkoutReminders" -> RemindersWidget()
            "HealthTimeline" -> HealthTimelineWidget()
            "PersonalRecords" -> PersonalRecordsWidget()
            "FriendsProgress" -> FriendsProgressWidget()
            "ExportReports" -> ExportReportsWidget()
            "DataBackup" -> DataBackupWidget()
            else -> {
                // Default settings card placeholder
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = GrayText, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("This panel holds settings parameters and charts configured dynamically for $title.", color = GrayText, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ----------------------------------------
// UNIVERSAL SCREEN INTERACTIVE WIDGETS
// ----------------------------------------

@Composable
fun BmiCalculatorWidget(userProfile: UserProfile) {
    var height by remember { mutableStateOf(userProfile.height.toFloat()) }
    var weight by remember { mutableStateOf(userProfile.weight.toFloat()) }
    
    val bmi = weight / ((height / 100f) * (height / 100f))
    val status = if (bmi < 18.5) "Underweight" else if (bmi < 25.0) "Normal Weight" else "Overweight / Obese"

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("BMI Calculator Engine", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            
            BiometricSlider("Height", height.toInt(), "cm", 100..250, { height = it.toFloat() })
            BiometricSlider("Weight", weight.toInt(), "kg", 30..200, { weight = it.toFloat() })
            
            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BMI Index Score:", color = GrayText, fontSize = 13.sp)
                Text("${(bmi * 10).toInt() / 10f}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Physiological Status:", color = GrayText, fontSize = 13.sp)
                Text(status, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun BmrCalculatorWidget(userProfile: UserProfile) {
    var height by remember { mutableStateOf(userProfile.height.toFloat()) }
    var weight by remember { mutableStateOf(userProfile.weight.toFloat()) }
    var age by remember { mutableStateOf(userProfile.age.toFloat()) }
    var isMale by remember { mutableStateOf(userProfile.gender == "Male") }

    val bmr = if (isMale) {
        10.0 * weight + 6.25 * height - 5.0 * age + 5.0
    } else {
        10.0 * weight + 6.25 * height - 5.0 * age - 161.0
    }

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("BMR Calculator Engine", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            
            BiometricSlider("Height", height.toInt(), "cm", 100..250, { height = it.toFloat() })
            BiometricSlider("Weight", weight.toInt(), "kg", 30..200, { weight = it.toFloat() })
            BiometricSlider("Age", age.toInt(), "years", 10..100, { age = it.toFloat() })
            
            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BMR Calorie Baseline:", color = GrayText, fontSize = 13.sp)
                Text("${bmr.toInt()} kcal", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun TdeeCalculatorWidget(userProfile: UserProfile) {
    var activityIndex by remember { mutableStateOf(2) } // Moderate
    val multiplier = listOf(1.2f, 1.375f, 1.55f, 1.725f, 1.9f)[activityIndex]
    val tdee = userProfile.bmr * multiplier

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("TDEE Calculator Engine", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Select Activity Level Multiplier:", fontSize = 11.sp, color = GrayText)
            Spacer(modifier = Modifier.height(8.dp))
            
            listOf("Sedentary (1.2x)", "Light (1.375x)", "Moderate (1.55x)", "Active (1.725x)", "Athlete (1.9x)").forEachIndexed { i, label ->
                val active = activityIndex == i
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) Cyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (active) Cyan else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { activityIndex = i }
                        .padding(12.dp)
                ) {
                    Text(label, color = if (active) Color.White else GrayText, fontSize = 12.sp)
                }
            }
            
            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Daily Energy Expenditure:", color = GrayText, fontSize = 13.sp)
                Text("${tdee.toInt()} kcal", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun MacroCalculatorWidget(userProfile: UserProfile) {
    var calsInput by remember { mutableStateOf("2000") }
    var proteinPercent by remember { mutableStateOf(30f) }
    var carbPercent by remember { mutableStateOf(40f) }
    
    val fatPercent = (100f - proteinPercent - carbPercent).coerceIn(0f, 100f)
    val totalCals = calsInput.toIntOrNull() ?: 2000
    
    val pGrams = ((totalCals * (proteinPercent / 100f)) / 4).toInt()
    val cGrams = ((totalCals * (carbPercent / 100f)) / 4).toInt()
    val fGrams = ((totalCals * (fatPercent / 100f)) / 9).toInt()

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Macro Ratio Calculator", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = calsInput,
                onValueChange = { calsInput = it },
                label = { Text("Daily Calories Target") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = NeonGreen)
            )
            
            BiometricSlider("Protein Ratio", proteinPercent.toInt(), "%", 10..80, { proteinPercent = it.toFloat() })
            BiometricSlider("Carbohydrate Ratio", carbPercent.toInt(), "%", 10..80, { carbPercent = it.toFloat() })
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Fats Ratio Index:", color = GrayText, fontSize = 12.sp)
                Text("${fatPercent.toInt()} %", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))
            
            Text("CALCULATED TARGETS IN GRAMS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow("Protein Target", "${pGrams}g")
            DetailRow("Carbs Target", "${cGrams}g")
            DetailRow("Fat Target", "${fGrams}g")
        }
    }
}

@Composable
fun SmartGroceryWidget() {
    val listItems = remember { 
        mutableStateListOf(
            "Organic Chicken Breast (500g)" to false,
            "Almond Milk Unsweetened" to false,
            "Fresh Blueberries (150g)" to true,
            "Greek Yogurt 0% Fat (500g)" to false,
            "Fresh Spinach Bunch" to false,
            "Eggs Carton (12 eggs)" to false,
            "Sweet Potatoes (1kg)" to false
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Smart Grocery Checklist", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            listItems.forEachIndexed { idx, (item, checked) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { listItems[idx] = item to !checked }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { listItems[idx] = item to it },
                        colors = CheckboxDefaults.colors(checkedColor = NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        fontSize = 13.sp,
                        color = if (checked) GrayText else Color.White,
                        fontWeight = if (checked) FontWeight.Normal else FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SavedItemsWidget() {
    val itemsList = listOf(
        "Avocado Toast whites" to "280 kcal | 18g P",
        "Whey Protein Shake" to "140 kcal | 26g P",
        "Baked Salmon Bowl" to "450 kcal | 38g P",
        "Grilled Chicken Salad" to "380 kcal | 35g P",
        "Greek Yogurt Medley" to "180 kcal | 16g P"
    )

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Saved Meals & Recipes", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            itemsList.forEach { (name, details) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(details, color = GrayText, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Log +", fontSize = 11.sp, color = Color.White)
                    }
                }
                Divider(color = GlassBorder)
            }
        }
    }
}

@Composable
fun StreakCenterWidget() {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gamification Streak Center", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("M" to true, "T" to true, "W" to true, "T" to true, "F" to true, "S" to false, "S" to false).forEach { (day, active) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (active) NeonGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                .border(1.dp, if (active) NeonGreen else GlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day, color = if (active) NeonGreen else GrayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Current Streak: 5 Days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Complete calorie logs tomorrow to earn 50 XP bonus multiplier.", color = GrayText, fontSize = 11.sp)
        }
    }
}

@Composable
fun HistoryWidget(title: String, waterMl: Int, currentCalories: Int) {
    val items = listOf(
        "June 17" to "1,850 kcal | 2,100 ml",
        "June 16" to "1,920 kcal | 2,800 ml",
        "June 15" to "1,740 kcal | 2,500 ml",
        "June 14" to "2,100 kcal | 3,100 ml",
        "June 13" to "1,650 kcal | 2,000 ml"
    )

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Historical Logs: $title", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Today", fontWeight = FontWeight.Bold, color = Color.White)
                Text("$currentCalories kcal | $waterMl ml", color = NeonGreen, fontWeight = FontWeight.Bold)
            }
            Divider(color = GlassBorder)
            
            items.forEach { (date, summary) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(date, color = GrayText, fontSize = 12.sp)
                    Text(summary, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Divider(color = GlassBorder)
            }
        }
    }
}

@Composable
fun AppearanceSettingsWidget() {
    var themeChoice by remember { mutableStateOf("Green Gradient") }
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Appearance Theme Calibrations", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            listOf("Green Gradient", "Purple Breeze", "Deep Cyan Glow").forEach { theme ->
                val selected = themeChoice == theme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) NeonGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (selected) NeonGreen else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { themeChoice = theme }
                        .padding(12.dp)
                ) {
                    Text(theme, color = if (selected) Color.White else GrayText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AiPreferencesWidget() {
    var notifEnabled by remember { mutableStateOf(true) }
    var recoveryEnabled by remember { mutableStateOf(true) }
    var voiceProfile by remember { mutableStateOf("AI Coach Sarah") }

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Preferences Panel", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AI Context Notifications", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Auto alert nutrition advice indexes", fontSize = 11.sp, color = GrayText)
                }
                Switch(checked = notifEnabled, onCheckedChange = { notifEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen))
            }
            
            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Real-Time Recovery Audit", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Analyze muscle rest metrics", fontSize = 11.sp, color = GrayText)
                }
                Switch(checked = recoveryEnabled, onCheckedChange = { recoveryEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen))
            }
        }
    }
}

@Composable
fun ConnectedDevicesWidget() {
    var devices = listOf(
        "Fitbit Luxe Smartwatch" to "Connected",
        "Google Fit Engine" to "Syncing active",
        "Smart Hydration Scale" to "Standby"
    )

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("IoT Smart Connected Devices", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            devices.forEach { (dev, status) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(dev, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(status, color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Divider(color = GlassBorder)
            }
        }
    }
}

@Composable
fun HealthInsightsWidget(userProfile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Smart Health Insights", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Based on your ${userProfile.gender} profile, age ${userProfile.age}, and goal (${userProfile.goal}), our AI engine has calibrated these insights:", fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow("Metabolic Health", "Optimal cellular efficiency")
            DetailRow("BMI Score Status", if (userProfile.bmi < 25.0) "Optimal (Healthy Range)" else "Slightly elevated")
            DetailRow("Recommended Focus", "Maintain lean muscle mass")
        }
    }
}

@Composable
fun NutritionBreakdownWidget(currentCalories: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Detailed Nutrition Breakdown", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Calibration metrics for today's logs:", fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Micro-nutrients", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Percentage", fontSize = 11.sp, color = GrayText)
            }
            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
            DetailRow("Calcium", "45% of daily target")
            DetailRow("Iron", "60% of daily target")
            DetailRow("Vitamin D", "100% (Fully Calibrated)")
        }
    }
}

@Composable
fun CompareFoodsWidget() {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Food Comparison Engine", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Compare nutritional efficiency of different food items side-by-side:", fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f).background(Color.White.copy(alpha = 0.02f)).padding(10.dp)) {
                    Text("Avocado Toast", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    Text("280 kcal", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Fiber: 8g", fontSize = 11.sp, color = GrayText)
                    Text("Score: 92/100", fontSize = 11.sp, color = Cyan, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f).background(Color.White.copy(alpha = 0.02f)).padding(10.dp)) {
                    Text("Croissant", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    Text("340 kcal", color = Color(0xFFF43F5E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Fiber: 1.5g", fontSize = 11.sp, color = GrayText)
                    Text("Score: 35/100", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChatHistoryWidget(chatRepo: ChatRepository, userId: String) {
    val logs = listOf(
        "Keto meal options for lunch" to "June 17, 2026",
        "How much protein post workout?" to "June 15, 2026",
        "Water target calculation audit" to "June 12, 2026"
    )
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Consultation History", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            logs.forEach { (topic, date) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(topic, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(date, color = GrayText, fontSize = 11.sp)
                    }
                    Text("View →", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Divider(color = GlassBorder)
            }
        }
    }
}

@Composable
fun MealSuggestionsWidget() {
    val suggestions = listOf(
        "High Protein Tofu Buddha Bowl" to "380 kcal | 24g P",
        "Lemon Baked Salmon & Asparagus" to "420 kcal | 35g P",
        "AI Calibrated Green Smoothie" to "190 kcal | 15g P"
    )
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Smart Meal Suggestions", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Based on your macro targets, we recommend these meal options:", fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(12.dp))
            suggestions.forEach { (name, details) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(details, color = GrayText, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add", fontSize = 11.sp, color = Color.White)
                    }
                }
                Divider(color = GlassBorder)
            }
        }
    }
}

@Composable
fun ExerciseHistoryWidget(exerciseLogs: List<ExerciseLogEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Exercise Tracking Logs", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            if (exerciseLogs.isEmpty()) {
                Text("No exercise logs recorded today. Start a workout session from the Exercise panel.", color = GrayText, fontSize = 12.sp)
            } else {
                exerciseLogs.forEach { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(log.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("${log.durationMinutes} mins | ${log.intensity} Intensity", color = GrayText, fontSize = 11.sp)
                        }
                        Text("-${log.caloriesBurned} kcal", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Divider(color = GlassBorder)
                }
            }
        }
    }
}

@Composable
fun CaloriesBurnedWidget(exerciseLogs: List<ExerciseLogEntry>) {
    val totalCals = exerciseLogs.sumOf { it.caloriesBurned }
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Active Calories Burned Summary", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Today's Active Calorie Deficit Nodes:", fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Active Calorie Burn:", color = Color.White, fontSize = 13.sp)
                Text("$totalCals kcal", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Keep active to increase metabolic calibration and optimize rest intervals.", color = GrayText, fontSize = 11.sp)
        }
    }
}

@Composable
fun WaterTrackerWidget(waterMl: Int, target: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Water Intake Hub", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(16.dp))
            Text("$waterMl", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Cyan)
            Text("/ $target ml target", color = GrayText, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { (waterMl.toFloat() / target.coerceAtLeast(1000)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Cyan,
                trackColor = Color.White.copy(alpha = 0.05f)
            )
        }
    }
}

@Composable
fun WeightTrackerWidget(userProfile: UserProfile) {
    var weightInput by remember { mutableStateOf("${userProfile.weight}") }
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Weight Analytics Tracker", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Log Current Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = NeonGreen)
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow("Target Weight Objective", "${userProfile.targetWeight} kg")
            DetailRow("Calibrated Deficit", "${(userProfile.weight - userProfile.targetWeight).format(1)} kg remaining")
        }
    }
}

@Composable
fun SettingsWidget() {
    var notifOn by remember { mutableStateOf(true) }
    var metricOn by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("System Preferences & Toggles", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Allow Push Warnings", fontSize = 13.sp, color = Color.White)
                Switch(checked = notifOn, onCheckedChange = { notifOn = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen))
            }
            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Use Metric System (kg/cm)", fontSize = 13.sp, color = Color.White)
                Switch(checked = metricOn, onCheckedChange = { metricOn = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen))
            }
        }
    }
}

@Composable
fun PremiumFeaturesWidget() {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("★ FoodSnap AI Premium", fontWeight = FontWeight.Bold, color = NeonGreen)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Unlock real-time automated nutrition analysis, unlimited barcode matches, IoT integration, and private consultation channels.", fontSize = 12.sp, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade to Pro - $9.99/mo", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AboutWidget() {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("About FoodSnap AI", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("FoodSnap AI is a modern health-tech application that leverages artificial intelligence to recognize food components, estimate sizes, calculate macro metrics, and coach users to achieve peak wellness.", fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(12.dp))
            Text("App Version: v2.4.0 (Redesign)", fontSize = 11.sp, color = Cyan)
            Text("Engine: Next-Gen Core 2.0", fontSize = 11.sp, color = GrayText)
        }
    }
}

@Composable
fun NutritionEducationWidget() {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Educational Hub", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("1. Calorie Deficit vs Surplus: Deficits burn stored fat nodes, surpluses trigger muscle and mass synthesis.", fontSize = 12.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("2. Protein Synthesis: Consume 20-30g protein every 3-4 hours to optimize muscle repair.", fontSize = 12.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("3. Hydration Cycles: Fluid intake maintains cellular pressure and speeds up recovery from workouts.", fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
fun NotificationsCenterWidget() {
    val notifications = listOf(
        "Hydration Alert: You need 500ml to stay on target" to "2h ago",
        "Goals Calibrated: Onboarding completed successfully" to "5h ago",
        "Streak Multiplier: Hit 5-day logging milestone" to "1d ago"
    )
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("System Notifications Logs", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            notifications.forEach { (text, time) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(time, color = GrayText, fontSize = 10.sp)
                }
                Divider(color = GlassBorder)
            }
        }
    }
}

@Composable
fun RemindersWidget() {
    var mOn by remember { mutableStateOf(true) }
    var wOn by remember { mutableStateOf(true) }
    var eOn by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Smart Action Reminders", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Meal Log Alerts", fontSize = 13.sp, color = Color.White)
                Switch(checked = mOn, onCheckedChange = { mOn = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen))
            }
            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Water Hydration Alerts", fontSize = 13.sp, color = Color.White)
                Switch(checked = wOn, onCheckedChange = { wOn = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen))
            }
            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Workout Activation Alerts", fontSize = 13.sp, color = Color.White)
                Switch(checked = eOn, onCheckedChange = { eOn = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen))
            }
        }
    }
}

@Composable
fun HealthTimelineWidget() {
    val timeline = listOf(
        "Logged first meal item: Cheeseburger" to "June 18",
        "Completed AI Profile Calibration" to "June 18",
        "Registered Account Credentials" to "June 18"
    )
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Activity Chronological Timeline", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            timeline.forEach { (action, date) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(action, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(date, color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Divider(color = GlassBorder)
            }
        }
    }
}

@Composable
fun PersonalRecordsWidget() {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Personal Milestones & Records", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow("Max Day Streak Achieved", "7 Days")
            DetailRow("Max Daily Water Intake Log", "3,100 ml")
            DetailRow("Max Exercise Time Logged", "90 mins")
            DetailRow("Max Calories Burned Target", "450 kcal")
        }
    }
}

@Composable
fun FriendsProgressWidget() {
    val friends = listOf(
        "Jane Cooper" to "14 days | Level 7",
        "Wade Warren" to "7 days | Level 4",
        "Kristin Watson" to "5 days | Level 3"
    )
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Friends Activity Rankings", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            friends.forEach { (name, stats) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(stats, color = GrayText, fontSize = 11.sp)
                    }
                    Text("Cheer", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Divider(color = GlassBorder)
            }
        }
    }
}

@Composable
fun ExportReportsWidget() {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Export Data Reports Engine", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Export your calculated metabolic graphs and water logs to standardized formats:", fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Export PDF", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Export CSV", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DataBackupWidget() {
    var driveBackup by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Automated Data Cloud Backup", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Google Cloud Platform Sync", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Auto-backup database parameters", fontSize = 11.sp, color = GrayText)
                }
                Switch(checked = driveBackup, onCheckedChange = { driveBackup = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen))
            }
        }
    }
}

