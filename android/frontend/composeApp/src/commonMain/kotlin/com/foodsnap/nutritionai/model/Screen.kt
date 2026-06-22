package com.foodsnap.nutritionai.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    // Auth
    @Serializable object Splash : Screen()
    @Serializable object Welcome : Screen()
    @Serializable data class Onboarding(val step: Int) : Screen()
    @Serializable object Login : Screen()
    @Serializable object Register : Screen()
    @Serializable object ForgotPassword : Screen()
    @Serializable object VerifyEmail : Screen()

    // Home
    @Serializable object Dashboard : Screen()
    @Serializable object DailySummary : Screen()
    @Serializable object WeeklySummary : Screen()
    @Serializable object MonthlySummary : Screen()
    @Serializable object HealthInsights : Screen()

    // Food
    @Serializable object FoodScanner : Screen()
    @Serializable data class ScanResult(
        val foodName: String,
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fats: Int,
        val healthScore: Int
    ) : Screen()
    @Serializable data class MealDetail(val entry: FoodLogEntry) : Screen()
    @Serializable object FoodHistory : Screen()
    @Serializable object Favorites : Screen()
    @Serializable object SavedMeals : Screen()
    @Serializable object NutritionBreakdown : Screen()
    @Serializable object CompareFoods : Screen()

    // AI
    @Serializable object AICoach : Screen()
    @Serializable object ChatHistory : Screen()
    @Serializable object PersonalizedRecommendations : Screen()
    @Serializable object MealSuggestions : Screen()
    @Serializable object GrocerySuggestions : Screen()

    // Fitness
    @Serializable object WorkoutDashboard : Screen()
    @Serializable object ExerciseHistory : Screen()
    @Serializable object StepCounter : Screen()
    @Serializable object CaloriesBurned : Screen()
    @Serializable object ActivityAnalytics : Screen()

    // Hydration
    @Serializable object WaterTracker : Screen()
    @Serializable object WaterHistory : Screen()

    // Health
    @Serializable object BmiCalculator : Screen()
    @Serializable object MacroCalculator : Screen()
    @Serializable object GoalPlanner : Screen()
    @Serializable object WeightTracker : Screen()
    @Serializable object ProgressAnalytics : Screen()

    // Social
    @Serializable object CommunityFeed : Screen()
    @Serializable object Leaderboard : Screen()
    @Serializable object Challenges : Screen()
    @Serializable object AchievementCenter : Screen()

    // Profile
    @Serializable object Profile : Screen()
    @Serializable object EditProfile : Screen()
    @Serializable object Settings : Screen()
    @Serializable object NotificationSettings : Screen()
    @Serializable object PrivacySettings : Screen()
    @Serializable object Subscription : Screen()
    @Serializable object About : Screen()

    // New Redesign & Premium Screens
    @Serializable object Home : Screen()
    @Serializable object StreakCenter : Screen()
    @Serializable object BmrCalculator : Screen()
    @Serializable object TdeeCalculator : Screen()
    @Serializable object NutritionEducation : Screen()
    @Serializable object NotificationsCenter : Screen()
    @Serializable object MealReminders : Screen()
    @Serializable object WaterReminders : Screen()
    @Serializable object WorkoutReminders : Screen()
    @Serializable object SmartInsights : Screen()
    @Serializable object HealthTimeline : Screen()
    @Serializable object PersonalRecords : Screen()
    @Serializable object FriendsProgress : Screen()
    @Serializable object AppearanceSettings : Screen()
    @Serializable object AiPreferences : Screen()
    @Serializable object ConnectedDevices : Screen()
    @Serializable object ExportReports : Screen()
    @Serializable object DataBackup : Screen()
    @Serializable object PremiumFeatures : Screen()
    @Serializable object DeveloperSettings : Screen()
}
