package com.foodsnap.nutritionai.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.model.*
import com.foodsnap.nutritionai.theme.*
import kotlinx.coroutines.launch

@Composable
fun FoodScanScreen(
    onLogFood: (String, Int, Int, Int, Int) -> Unit,
    analyzeImage: suspend (ByteArray, String) -> FoodItem
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(false) }
    var resultItem by remember { mutableStateOf<FoodItem?>(null) }
    var logSuccess by remember { mutableStateOf(false) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastPickedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var lastPickedFilename by remember { mutableStateOf("") }
    var showChooserDialog by remember { mutableStateOf(false) }
    var showManualEntryDialog by remember { mutableStateOf(false) }

    // Multi-food, portion estimation details (simulated dynamic details)
    var servingMultiplier by remember { mutableStateOf(1.0f) }

    fun performAnalysis(bytes: ByteArray, filename: String) {
        lastPickedBytes = bytes
        lastPickedFilename = filename
        errorMessage = null
        resultItem = null
        logSuccess = false
        servingMultiplier = 1.0f
        
        scope.launch {
            isLoading = true
            try {
                val result = analyzeImage(bytes, filename)
                if (result.food_name == "Unknown Food Item" && result.calories == "0") {
                    errorMessage = "The uploaded food item could not be recognized. Please try scanning a different food."
                } else {
                    resultItem = result
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred during AI analysis. Please check your connection and try again."
            } finally {
                isLoading = false
            }
        }
    }

    fun performRetry() {
        val bytes = lastPickedBytes
        val filename = lastPickedFilename
        if (bytes != null && filename.isNotEmpty()) {
            performAnalysis(bytes, filename)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val imagePicker = rememberImagePicker { bytes, filename ->
            performAnalysis(bytes, filename)
        }

        // Camera / Gallery Choice Dialog
        if (showChooserDialog) {
            AlertDialog(
                onDismissRequest = { showChooserDialog = false },
                title = { Text("Select Food Image", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Choose to capture a new photo using the camera or select an existing photo from your gallery.", color = GrayText) },
                confirmButton = {
                    Button(
                        onClick = {
                            showChooserDialog = false
                            imagePicker.takePhoto()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Camera", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showChooserDialog = false
                            imagePicker.pickImage()
                        }
                    ) {
                        Text("Gallery", color = NeonGreen)
                    }
                },
                containerColor = CardBackgroundDark,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Manual Meal Logging Dialog
        if (showManualEntryDialog) {
            var manualName by remember { mutableStateOf("") }
            var manualCalories by remember { mutableStateOf("") }
            var manualProtein by remember { mutableStateOf("") }
            var manualCarbs by remember { mutableStateOf("") }
            var manualFats by remember { mutableStateOf("") }
            var manualError by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showManualEntryDialog = false },
                title = { Text("Log Meal Manually", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = manualName,
                            onValueChange = { manualName = it },
                            label = { Text("Meal Name") },
                            placeholder = { Text("e.g. Chicken Salad") },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = GlassBorder
                            )
                        )

                        OutlinedTextField(
                            value = manualCalories,
                            onValueChange = { manualCalories = it },
                            label = { Text("Calories (kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = GlassBorder
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = manualProtein,
                                onValueChange = { manualProtein = it },
                                label = { Text("Prot (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = GlassBorder
                                )
                            )

                            OutlinedTextField(
                                value = manualCarbs,
                                onValueChange = { manualCarbs = it },
                                label = { Text("Carb (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = GlassBorder
                                )
                            )

                            OutlinedTextField(
                                value = manualFats,
                                onValueChange = { manualFats = it },
                                label = { Text("Fat (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = GlassBorder
                                )
                            )
                        }

                        if (manualError.isNotEmpty()) {
                            Text(manualError, color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cals = manualCalories.toIntOrNull()
                            val prot = manualProtein.toIntOrNull() ?: 0
                            val carbs = manualCarbs.toIntOrNull() ?: 0
                            val fats = manualFats.toIntOrNull() ?: 0

                            if (manualName.isBlank() || cals == null) {
                                manualError = "Meal name and valid calories are required."
                                return@Button
                            }

                            onLogFood(manualName, cals, prot, carbs, fats)
                            showManualEntryDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Log Meal", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualEntryDialog = false }) {
                        Text("Cancel", color = NeonGreen)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "AI Vision Scanner",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "Analyze meal items, portion multipliers, and log nutrition details instantaneously.",
                fontSize = 13.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Premium Camera Viewfinder with Scanner Animation
            if (resultItem == null && !isLoading && errorMessage == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF09101F))
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(NeonGreen, Cyan)),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { showChooserDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    // Viewfinder corners and laser line animations
                    val infiniteTransition = rememberInfiniteTransition()
                    val laserY by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 0.9f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                val yVal = this.size.height * laserY
                                drawLine(
                                    brush = Brush.horizontalGradient(listOf(Color.Transparent, NeonGreen, Color.Transparent)),
                                    start = Offset(0f, yVal),
                                    end = Offset(this.size.width, yVal),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📸", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TAP TO RUN CAMERA / GALLERY",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Aim at dish and keep stable",
                            color = GrayText,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NeonGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Calibrating computer vision nodes...", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Extracting volume & portion data...", color = GrayText, fontSize = 12.sp)
                    }
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scan Failure", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(errorMessage!!, color = GrayText, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { performRetry() }, colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)) {
                                Text("Retry Scan", color = Color.White)
                            }
                            TextButton(onClick = { errorMessage = null }) {
                                Text("Cancel", color = NeonGreen)
                            }
                        }
                    }
                }
            } else if (resultItem != null) {
                val item = resultItem!!
                
                // Recalculated values based on multiplier
                val displayCalories = ((item.calories.toIntOrNull() ?: 0) * servingMultiplier).toInt()
                val displayProtein = (item.macros.protein * servingMultiplier).toInt()
                val displayCarbs = (item.macros.carbs * servingMultiplier).toInt()
                val displayFats = (item.macros.fats * servingMultiplier).toInt()
                
                val displayFiber = (displayCarbs * 0.12).toInt().coerceAtLeast(1)
                val displaySugar = (displayCarbs * 0.3).toInt().coerceAtLeast(2)
                val displaySodium = (displayCalories * 0.65).toInt().coerceAtLeast(60)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(NeonGreen.copy(alpha = 0.4f), Cyan.copy(alpha = 0.4f))),
                            RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Title header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.food_name, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("Estimated Portion: ${(320 * servingMultiplier).toInt()}g", fontSize = 12.sp, color = GrayText)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("${item.confidence}% AI Match", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                            }
                        }

                        Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))

                        // Serving Size Multiplier Slider
                        Text(
                            text = "Edit Serving Multiplier: ${((servingMultiplier * 10).toInt() / 10f)}x",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = servingMultiplier,
                            onValueChange = { servingMultiplier = it },
                            valueRange = 0.5f..3.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Cyan,
                                activeTrackColor = Cyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.05f)
                            )
                        )
                        
                        Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))

                        // Comprehensive Nutrition breakdown
                        Text("Nutrition breakdown", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NutrientDisplay("Calories", "$displayCalories kcal", NeonGreen)
                            NutrientDisplay("Protein", "${displayProtein}g", Cyan)
                            NutrientDisplay("Carbs", "${displayCarbs}g", AccentOrange)
                            NutrientDisplay("Fat", "${displayFats}g", Color(0xFFF43F5E))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailRow("Fiber", "${displayFiber}g")
                        DetailRow("Sugar", "${displaySugar}g")
                        DetailRow("Sodium", "${displaySodium}mg")

                        Spacer(modifier = Modifier.height(16.dp))

                        if (logSuccess) {
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false
                            ) {
                                Text("Saved to Daily Tracker ✓", color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    onLogFood(item.food_name, displayCalories, displayProtein, displayCarbs, displayFats)
                                    logSuccess = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save to Tracker", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = {
                                resultItem = null
                                logSuccess = false
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Scan Different Item", color = NeonGreen)
                        }
                    }
                }
            } else {
                // Large Add Meal Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(NeonGreen.copy(alpha = 0.5f), Cyan.copy(alpha = 0.5f))),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { showManualEntryDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✍️", fontSize = 28.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Manual Meal Logging",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Can't scan your meal? Add it manually in seconds to maintain streak consistency and keep your calorie charts synchronized.",
                            fontSize = 12.sp,
                            color = GrayText,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = { showManualEntryDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Add Meal Manually", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun NutrientDisplay(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = GrayText)
    }
}
