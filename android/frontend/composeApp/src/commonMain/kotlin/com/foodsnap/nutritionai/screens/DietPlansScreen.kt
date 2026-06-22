package com.foodsnap.nutritionai.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import com.foodsnap.nutritionai.model.UserProfile
import com.foodsnap.nutritionai.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DietPlanMeal(
    val time: String,
    val type: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val recipe: String,
    val instructions: String
)

@Composable
fun DietPlansScreen(
    userProfile: UserProfile,
    onSelectPlan: (String, Int, Int, Int, Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    val categories = listOf(
        "Weight Gain",
        "Weight Loss",
        "Muscle Gain",
        "Vegetarian",
        "Diabetic Friendly",
        "High Protein",
        "Athlete"
    )

    val recommendedPlan = when (userProfile.goal) {
        "Weight Loss", "Weight loss" -> "Weight Loss"
        "Muscle Building", "Muscle Gain" -> "Muscle Gain"
        "Weight Gain", "Weight gain" -> "Weight Gain"
        else -> null
    }

    val recommendedIndex = if (recommendedPlan != null) {
        categories.indexOf(recommendedPlan).takeIf { it != -1 } ?: 1
    } else {
        1
    }

    var activeCategoryIndex by remember(userProfile.goal) { mutableStateOf(recommendedIndex) }
    var isRegenerating by remember { mutableStateOf(false) }

    // Simulated schedule items based on category and regeneration status
    val baseMeals = remember(activeCategoryIndex, isRegenerating) {
        val multiplier = if (isRegenerating) 1.05f else 1f
        val suffix = if (isRegenerating) " (AI Optimized)" else ""
        
        when (categories[activeCategoryIndex]) {
            "Weight Loss" -> listOf(
                DietPlanMeal("8:00 AM", "Breakfast", "Avocado & Egg Whites Toast$suffix", (320 * multiplier).toInt(), 22, "2 Slices Whole Wheat Toast, 4 Egg Whites, 1/2 Avocado, Salt & Pepper.", "1. Toast bread. 2. Scramble whites. 3. Spread mashed avocado on toast and top with egg whites."),
                DietPlanMeal("11:00 AM", "Mid Morning", "Greek Yogurt with Berries$suffix", (150 * multiplier).toInt(), 15, "150g Organic Non-Fat Greek Yogurt, 50g Blueberries, Chia seeds.", "1. Spoon yogurt into bowl. 2. Top with washed berries and sprinkle chia seeds."),
                DietPlanMeal("1:30 PM", "Lunch", "Grilled Chicken Salad$suffix", (450 * multiplier).toInt(), 38, "150g Grilled Chicken Breast, Mixed Salad Greens, Cucumbers, 1 tbsp Olive oil dressing.", "1. Grill chicken. 2. Chop vegetables. 3. Toss with dressing and serve."),
                DietPlanMeal("4:30 PM", "Snack", "Almond Handful & Apple$suffix", (180 * multiplier).toInt(), 4, "1 Medium Apple, 15 Raw Almonds.", "1. Slice apple. 2. Serve with almonds."),
                DietPlanMeal("8:00 PM", "Dinner", "Baked Salmon with Broccoli$suffix", (420 * multiplier).toInt(), 35, "140g Baked Salmon Fillet, 1 Cup Broccoli Florets, Lemon juice.", "1. Season salmon. 2. Bake at 200C for 15m. 3. Steam broccoli and drizzle lemon juice."),
                DietPlanMeal("9:30 PM", "Post Workout", "Whey Protein Shake$suffix", (130 * multiplier).toInt(), 25, "1 Scoop Whey Isolate, 250ml Water.", "1. Mix in shaker bottle until smooth.")
            )
            "Muscle Gain" -> listOf(
                DietPlanMeal("8:00 AM", "Breakfast", "Oatmeal with Peanut Butter$suffix", (550 * multiplier).toInt(), 28, "75g Oats, 1 Scoop Protein Powder, 2 tbsp Natural Peanut Butter.", "1. Cook oats in water. 2. Stir in protein powder. 3. Top with peanut butter."),
                DietPlanMeal("11:00 AM", "Mid Morning", "Boiled Eggs & Rice Cakes$suffix", (280 * multiplier).toInt(), 18, "3 Whole Boiled Eggs, 2 Brown Rice Cakes.", "1. Boil eggs for 8 mins. 2. Serve with rice cakes."),
                DietPlanMeal("1:30 PM", "Lunch", "Beef Stir-Fry with Rice$suffix", (650 * multiplier).toInt(), 45, "150g Lean Ground Beef, 1 Cup Jasmine Rice, Green Peppers.", "1. Cook rice. 2. Brown beef with peppers. 3. Serve together."),
                DietPlanMeal("4:30 PM", "Snack", "Protein Shake & Banana$suffix", (350 * multiplier).toInt(), 28, "1 Scoop Whey Protein, 1 Large Banana, 250ml Almond milk.", "1. Blend shake with banana."),
                DietPlanMeal("8:00 PM", "Dinner", "Grilled Turkey & Sweet Potato$suffix", (580 * multiplier).toInt(), 40, "180g Turkey Breast Fillet, 150g Baked Sweet Potato, Asparagus.", "1. Bake turkey and potato. 2. Steam asparagus and serve."),
                DietPlanMeal("9:30 PM", "Post Workout", "Cottage Cheese Bowl$suffix", (200 * multiplier).toInt(), 24, "200g Low-Fat Cottage Cheese, Pineapple chunks.", "1. Mix cheese and pineapples.")
            )
            else -> listOf(
                DietPlanMeal("8:00 AM", "Breakfast", "Whole Wheat Egg Scramble$suffix", (380 * multiplier).toInt(), 24, "3 Eggs, 1 Slice Whole Toast, Tomatoes.", "1. Scramble eggs. 2. Toast bread and serve."),
                DietPlanMeal("11:00 AM", "Mid Morning", "Mixed Nuts Medley$suffix", (180 * multiplier).toInt(), 6, "30g Almonds, Walnuts & Cashews.", "1. Serve raw nuts."),
                DietPlanMeal("1:30 PM", "Lunch", "Quinoa with Tofu Bowl$suffix", (460 * multiplier).toInt(), 25, "1 Cup Quinoa, 150g Crispy Tofu, Steamed Spinach.", "1. Cook quinoa. 2. Sauté tofu. 3. Combine in bowl."),
                DietPlanMeal("4:30 PM", "Snack", "Hummus & Carrots$suffix", (140 * multiplier).toInt(), 4, "3 tbsp Hummus, 100g Carrot Sticks.", "1. Cut carrots. 2. Serve with dip."),
                DietPlanMeal("8:00 PM", "Dinner", "Baked Fish & Quinoa$suffix", (410 * multiplier).toInt(), 32, "150g Baked White Fish, 1/2 Cup Quinoa, Asparagus.", "1. Season and bake fish. 2. Serve with quinoa."),
                DietPlanMeal("9:30 PM", "Post Workout", "Casein Shake$suffix", (120 * multiplier).toInt(), 24, "1 Scoop Casein Protein, Water.", "1. Shake and consume before bed.")
            )
        }
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
            // Header
            Text(
                text = "Dietary Calibrations",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "Select plan category to view and activate full daily schedule. AI optimizes recipes.",
                fontSize = 13.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Targets Card at the top
            val isRecommendedActive = recommendedPlan != null && categories[activeCategoryIndex] == recommendedPlan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("YOUR PROFILE GOAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Cyan)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(userProfile.goal, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        if (isRecommendedActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonGreen.copy(alpha = 0.15f))
                                    .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("AI Recommended Plan Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = GlassBorder)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Target Calories", fontSize = 11.sp, color = GrayText)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${userProfile.targetCalories} kcal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Protein Target", fontSize = 11.sp, color = GrayText)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${userProfile.targetProtein}g", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Water Target", fontSize = 11.sp, color = GrayText)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${userProfile.targetWater} ml", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Category Chips Row (Horizontal Scroll)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(categories) { index, cat ->
                    val active = activeCategoryIndex == index
                    val isRecommended = cat == recommendedPlan

                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseScale by if (isRecommended) {
                        infiniteTransition.animateFloat(
                            initialValue = 0.96f,
                            targetValue = 1.04f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                    } else {
                        remember { mutableStateOf(1.0f) }
                    }

                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isRecommended) NeonGreen.copy(alpha = 0.25f)
                                else if (active) NeonGreen.copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.03f)
                            )
                            .border(
                                width = if (isRecommended) 2.5.dp else 1.dp,
                                color = if (isRecommended) NeonGreen else if (active) NeonGreen else GlassBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                activeCategoryIndex = index
                                // Activate matching profile configurations
                                val cal = if (cat == "Weight Loss") 1500 else if (cat == "Weight Gain") 2500 else 2000
                                val prot = if (cat == "Weight Loss") 120 else if (cat == "Weight Gain") 160 else 140
                                val carb = if (cat == "Weight Loss") 140 else if (cat == "Weight Gain") 240 else 200
                                val fat = if (cat == "Weight Loss") 50 else if (cat == "Weight Gain") 80 else 65
                                onSelectPlan(cat, cal, prot, carb, fat)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = cat,
                                color = if (active || isRecommended) Color.White else GrayText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isRecommended) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Recommend",
                                        color = AccentOrange,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Schedule Cards Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Meal Timeline",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            isRegenerating = true
                            delay(1500)
                            isRegenerating = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isRegenerating
                ) {
                    if (isRegenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Regenerate AI", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Meals Timeline list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                baseMeals.forEach { meal ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Cyan.copy(alpha = 0.1f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(meal.time, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Cyan)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(meal.type, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(meal.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${meal.calories} kcal", fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                                    Text("Protein: ${meal.protein}g", fontSize = 11.sp, color = GrayText)
                                }
                            }
                            
                            // Expandable Details (Recipe & Instructions)
                            if (isExpanded) {
                                Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))
                                Text("Ingredients Blueprint", fontSize = 12.sp, color = Cyan, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(meal.recipe, fontSize = 12.sp, color = Color.White)
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Cooking Directions", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(meal.instructions, fontSize = 12.sp, color = GrayText, lineHeight = 16.sp)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Click to collapse details", fontSize = 9.sp, color = GrayText, modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

