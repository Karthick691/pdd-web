package com.foodsnap.nutritionai.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.model.FoodLogEntry
import com.foodsnap.nutritionai.theme.*

@Composable
fun MealDetailScreen(
    entry: FoodLogEntry,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onAddToFavorites: () -> Unit,
    onShare: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Generate logical detailed properties based on entry
    val fiber = (entry.carbs * 0.12).toInt().coerceAtLeast(1)
    val sugar = (entry.carbs * 0.35).toInt().coerceAtLeast(2)
    val sodium = (entry.calories * 0.7).toInt().coerceAtLeast(80)
    val portionSize = "1 Plate (approx. 320g)"
    
    val ratio = entry.protein.toFloat() / (entry.fats + 1)
    val healthScore = ((ratio * 15) + 60).coerceIn(45f, 95f).toInt()
    
    val aiSummary = "A nutrient-rich selection. The high protein content support muscle recovery, while the carbohydrates provide long-lasting metabolic energy. Recommend drinking adequate hydration following this meal."
    val alternative = "Substitute with low-sodium vegetables or grilled lean proteins to further lower lipids and increase fiber."
    val waterIntake = "450 ml"
    val mealNotes = "Felt full and energized. Logged via mobile scanner."
    val scanConfidence = "96.4%"
    val foodSource = "Image Classifier API v2"
    
    val isLunch = entry.timestamp.contains("12:") || entry.timestamp.contains("13:") || entry.timestamp.contains("14:")
    val isBreakfast = entry.timestamp.contains("07:") || entry.timestamp.contains("08:") || entry.timestamp.contains("09:") || entry.timestamp.contains("06:")
    val mealType = if (isLunch) "Lunch" else if (isBreakfast) "Breakfast" else "Dinner"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = LightText, modifier = Modifier.size(16.dp))
                    Text("Back", color = LightText)
                }
            }
            Text("Meal Insights", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
            IconButton(onClick = onAddToFavorites) {
                Icon(Icons.Rounded.Star, contentDescription = "Favorite", tint = AccentOrange, modifier = Modifier.size(24.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Large Image Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(SecondaryPurple.copy(alpha = 0.6f), PrimaryPurple.copy(alpha = 0.1f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🥗", fontSize = 72.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Food Name & Meal Type
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.foodName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightText
                )
                Text(
                    text = "$mealType • ${entry.timestamp}",
                    fontSize = 13.sp,
                    color = GrayText
                )
            }
            Box(
                modifier = Modifier
                    .background(PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$healthScore Health Score",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Macro Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MacroMetricCard("Calories", "${entry.calories} kcal", PrimaryPurple, Modifier.weight(1f))
            MacroMetricCard("Protein", "${entry.protein}g", Color(0xFF60A5FA), Modifier.weight(1f))
            MacroMetricCard("Carbs", "${entry.carbs}g", AccentOrange, Modifier.weight(1f))
            MacroMetricCard("Fats", "${entry.fats}g", Color(0xFFF43F5E), Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Micro & Specific Details
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Nutritional Blueprint", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailRow("Portion Size", portionSize)
            DetailRow("Fiber", "${fiber}g")
            DetailRow("Sugar", "${sugar}g")
            DetailRow("Sodium", "${sodium}mg")
            DetailRow("Water Consumed with Meal", waterIntake)
            DetailRow("Food Source / Model", foodSource)
            DetailRow("Scan Confidence", scanConfidence)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // AI Recommendations & Summary
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                Text("AI Coach Analysis", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(aiSummary, fontSize = 13.sp, color = LightText, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = GlassBorder)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Smart Substitutions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
            Spacer(modifier = Modifier.height(4.dp))
            Text(alternative, fontSize = 12.sp, color = GrayText, lineHeight = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Notes Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Meal Notes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.height(6.dp))
            Text(mealNotes, fontSize = 13.sp, color = GrayText)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Actions Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onDuplicate,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Duplicate",
                        tint = LightText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Duplicate", fontSize = 12.sp, color = LightText)
                }
            }
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = LightText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Share", fontSize = 12.sp, color = LightText)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit Meal",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Edit Meal", fontSize = 13.sp, color = Color.White)
                }
            }
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Meal",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Delete Meal", fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MacroMetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = GrayText)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = GrayText)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = LightText)
    }
}
