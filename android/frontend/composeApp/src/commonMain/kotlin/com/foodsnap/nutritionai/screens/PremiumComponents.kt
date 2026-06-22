package com.foodsnap.nutritionai.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = cardModifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun GamificationHeader(
    xp: Int = 1250,
    level: Int = 4,
    streak: Int = 7
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Level $level Athlete",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
                Text(
                    text = "$xp XP total • Next level in 250 XP",
                    fontSize = 12.sp,
                    color = GrayText
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🔥 $streak Day Streak!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Progress bar for Level XP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f) // 80% progress
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(listOf(SecondaryPurple, PrimaryPurple)),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
fun CalorieProgressRing(
    consumed: Int,
    target: Int,
    burned: Int,
    modifier: Modifier = Modifier
) {
    val netCalories = consumed - burned
    val progress = if (target > 0) netCalories.toFloat() / target.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = LinearOutSlowInEasing)
    )

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            
            // Background ring
            drawArc(
                color = Color.White.copy(alpha = 0.05f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress ring
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(SecondaryPurple, PrimaryPurple, SecondaryPurple)
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$netCalories",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = LightText
            )
            Text(
                text = "net kcal",
                fontSize = 11.sp,
                color = GrayText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Goal: $target",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryPurple
            )
        }
    }
}

@Composable
fun MacroPieChart(
    protein: Int,
    carbs: Int,
    fats: Int,
    modifier: Modifier = Modifier
) {
    val total = (protein + carbs + fats).coerceAtLeast(1)
    val pAngle = (protein.toFloat() / total) * 360f
    val cAngle = (carbs.toFloat() / total) * 360f
    val fAngle = (fats.toFloat() / total) * 360f

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(110.dp)) {
            val strokeWidth = 16.dp.toPx()
            
            // Protein (Purple)
            drawArc(
                color = PrimaryPurple,
                startAngle = -90f,
                sweepAngle = pAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Carbs (Orange/Yellow)
            drawArc(
                color = AccentOrange,
                startAngle = -90f + pAngle,
                sweepAngle = cAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Fats (Blue/LightPurple)
            drawArc(
                color = Color(0xFF60A5FA),
                startAngle = -90f + pAngle + cAngle,
                sweepAngle = fAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MacroIndicator("Protein", "${protein}g", PrimaryPurple)
            MacroIndicator("Carbs", "${carbs}g", AccentOrange)
            MacroIndicator("Fats", "${fats}g", Color(0xFF60A5FA))
        }
    }
}

@Composable
private fun MacroIndicator(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Column {
            Text(label, fontSize = 11.sp, color = GrayText)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightText)
        }
    }
}

@Composable
fun WeeklyTrendGraph(
    caloriesList: List<Int> = listOf(1850, 1920, 1780, 2100, 1650, 1800, 1950)
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val maxVal = (caloriesList.maxOrNull() ?: 2000).coerceAtLeast(1)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text("Weekly Calories Trend", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightText)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            caloriesList.forEachIndexed { idx, valC ->
                val ratio = valC.toFloat() / maxVal.toFloat()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("${valC}", fontSize = 9.sp, color = GrayText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(ratio.coerceIn(0.1f, 1f))
                            .width(10.dp)
                            .background(
                                brush = Brush.verticalGradient(listOf(PrimaryPurple, SecondaryPurple)),
                                shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomEnd = 0.dp, bottomStart = 0.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(days[idx], fontSize = 11.sp, color = LightText)
                }
            }
        }
    }
}

@Composable
fun WeightTrendGraph(
    weights: List<Double> = listOf(72.0, 71.8, 71.5, 71.9, 71.2, 70.8, 70.5)
) {
    val minWeight = weights.minOrNull() ?: 60.0
    val maxWeight = weights.maxOrNull() ?: 80.0
    val weightDiff = (maxWeight - minWeight).coerceAtLeast(0.1)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text("Weight Tracker (kg)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightText)
        Spacer(modifier = Modifier.height(16.dp))
        
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            val width = size.width
            val height = size.height
            val pointsCount = weights.size
            val stepX = width / (pointsCount - 1).coerceAtLeast(1)
            
            val path = Path()
            weights.forEachIndexed { idx, w ->
                val ratioY = (w - minWeight) / weightDiff
                val x = idx * stepX
                val y = height - (ratioY * height).toFloat()
                
                if (idx == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                drawCircle(
                    color = PrimaryPurple,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            drawPath(
                path = path,
                color = PrimaryPurple.copy(alpha = 0.7f),
                style = Stroke(width = 3.dp.toPx())
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Start: ${weights.first()}kg", fontSize = 11.sp, color = GrayText)
            Text("Latest: ${weights.last()}kg", fontSize = 11.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AchievementBadge(
    title: String,
    desc: String,
    icon: String,
    xpReward: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassSurface, RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 32.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightText)
            Text(desc, fontSize = 11.sp, color = GrayText)
        }
        Box(
            modifier = Modifier
                .background(SecondaryPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("+$xpReward XP", fontSize = 11.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
        }
    }
}
