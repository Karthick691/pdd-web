package com.foodsnap.nutritionai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.theme.*

@Composable
fun SubscriptionScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, DarkSurface)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upgrade Subscription",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unlock the full power of FoodSnap Premium AI. Connect Fitbit/Google Fit devices, get unlimited real-time AI nutrition coaching, and export advanced clinical reports.",
                fontSize = 13.sp,
                color = GrayText,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Plans List
            SubscriptionPlanCard(
                name = "Basic Plan",
                price = "Free",
                features = listOf("Basic meal logs", "Manual water tracking", "Standard dashboard stats"),
                isPopular = false,
                borderColor = GlassBorder,
                accentColor = GrayText
            )

            Spacer(modifier = Modifier.height(16.dp))

            SubscriptionPlanCard(
                name = "Pro Coach Plan",
                price = "$9.99 / mo",
                features = listOf("Unlimited AI Coach responses", "Real-time context analysis", "Custom macro goal planners", "Priority server connection"),
                isPopular = false,
                borderColor = Cyan.copy(alpha = 0.5f),
                accentColor = Cyan
            )

            Spacer(modifier = Modifier.height(16.dp))

            SubscriptionPlanCard(
                name = "Premium Sync (Recommended)",
                price = "$19.99 / mo",
                features = listOf("All Pro features included", "Fitbit Smartwatch Sync", "Google Fit integration", "Real-time calorie & step sync", "Automatic activity logging"),
                isPopular = true,
                borderColor = NeonGreen.copy(alpha = 0.8f),
                accentColor = NeonGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            SubscriptionPlanCard(
                name = "AI Nutrition Ninja",
                price = "$29.99 / mo",
                features = listOf("All Premium features included", "Advanced clinic exportable PDF reports", "Weekly smart health audits", "Early access to developer beta modes"),
                isPopular = false,
                borderColor = AccentOrange.copy(alpha = 0.6f),
                accentColor = AccentOrange
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SubscriptionPlanCard(
    name: String,
    price: String,
    features: List<String>,
    isPopular: Boolean,
    borderColor: Color,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isPopular) 2.dp else 1.dp,
                brush = Brush.linearGradient(
                    colors = if (isPopular) listOf(NeonGreen, Cyan) else listOf(borderColor, borderColor)
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = price,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }

                if (isPopular) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "POPULAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }
            }

            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = LightText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Activate Subscription",
                    color = if (accentColor == GrayText) Color.White else DarkBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
