package com.foodsnap.nutritionai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.theme.*

@Composable
fun FitnessDashboardScreen(
    onBack: () -> Unit,
    onNavigateToExerciseHistory: () -> Unit,
    onNavigateToStepCounter: () -> Unit,
    onNavigateToCalBurn: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GlassSurface)) {
                Text("← Back", color = LightText)
            }
            Text("Workout & Fitness", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.width(60.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToExerciseHistory,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                modifier = Modifier.weight(1f)
            ) {
                Text("🏋️ Logs", fontSize = 11.sp, color = LightText)
            }
            Button(
                onClick = onNavigateToStepCounter,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                modifier = Modifier.weight(1f)
            ) {
                Text("👟 Steps", fontSize = 11.sp, color = LightText)
            }
            Button(
                onClick = onNavigateToCalBurn,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                modifier = Modifier.weight(1f)
            ) {
                Text("🔥 Burn", fontSize = 11.sp, color = LightText)
            }
            Button(
                onClick = onNavigateToAnalytics,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                modifier = Modifier.weight(1f)
            ) {
                Text("📊 Analytics", fontSize = 11.sp, color = LightText)
            }
        }

        // Active State Summary Cards
        GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Text("Today's Activity", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Steps", fontSize = 12.sp, color = GrayText)
                    Text("7,420 steps", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                }
                Column {
                    Text("Active Time", fontSize = 12.sp, color = GrayText)
                    Text("45 mins", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                }
                Column {
                    Text("Burned", fontSize = 12.sp, color = GrayText)
                    Text("350 kcal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
                }
            }
        }

        Text("Fitness Tracking Chart", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightText)
        Spacer(modifier = Modifier.height(12.dp))
        WeeklyTrendGraph(listOf(240, 310, 450, 180, 290, 380, 350))
    }
}

@Composable
fun StepCounterScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GlassSurface)) {
                Text("← Back", color = LightText)
            }
            Text("Step Analytics", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.width(60.dp))
        }

        GlassCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text("Step Summary", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CalorieProgressRing(7420, 10000, 0, modifier = Modifier.size(180.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            DetailRow("Average Daily Steps", "8,120 steps")
            DetailRow("Weekly Total Distance", "38.4 km")
            DetailRow("Floor Climbed Today", "12 floors")
        }
    }
}
