package com.foodsnap.nutritionai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.theme.*
import kotlinx.coroutines.launch
import com.foodsnap.nutritionai.model.UserProfile
import com.foodsnap.nutritionai.model.format
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.foodsnap.nutritionai.repository.ChatRepository
import com.foodsnap.nutritionai.repository.getCurrentTimestamp
import com.foodsnap.nutritionai.model.ChatMessage

data class Message(val role: String, val text: String)

@Composable
fun AIChatAssistantScreen(
    chatHandler: suspend (String) -> String,
    userProfile: UserProfile,
    waterMl: Int,
    foodCals: Int,
    exerciseMins: Int,
    exerciseCals: Int,
    chatRepository: ChatRepository,
    userId: String
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    var textInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val messages = remember { mutableStateListOf<Message>() }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            isLoading = true
            try {
                val history = chatRepository.getChatHistory(userId)
                messages.clear()
                if (history.isNotEmpty()) {
                    messages.addAll(history.map { Message(it.role, it.text) })
                } else {
                    messages.add(Message("assistant", "Hello! I'm your FoodSnap AI Coach. How can I help you reach your health and nutrition goals today?"))
                }
            } catch (e: Exception) {
                messages.add(Message("assistant", "Error loading history. Let's start a new conversation!"))
            } finally {
                isLoading = false
            }
        }
    }

    val quickQuestions = remember(userProfile.goal, waterMl, exerciseMins) {
        val list = mutableListOf<String>()
        if (userProfile.goal.isNotEmpty()) {
            list.add("Diet tips for my ${userProfile.goal} goal?")
        } else {
            list.add("How to set a proper fitness goal?")
        }
        if (waterMl < 2000) {
            list.add("How can I hit my hydration goals?")
        } else {
            list.add("Benefits of high water consumption?")
        }
        if (exerciseMins == 0) {
            list.add("Suggest a quick active workout.")
        } else {
            list.add("Post-workout nutrition recovery?")
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, DarkSurface)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassSurface)
                    .border(0.dp, Color.Transparent, RoundedCornerShape(0.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(AccentOrange.copy(alpha = 0.2f))
                            .border(1.dp, AccentOrange, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.SmartToy, contentDescription = "AI", tint = AccentOrange, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AI Nutrition Coach", fontWeight = FontWeight.Bold, color = LightText, fontSize = 16.sp)
                        Text("Online & Ready", fontSize = 11.sp, color = PrimaryGreen)
                    }
                }
            }

            // REAL-TIME AI CONTEXT MATRIX
            var showContextPanel by remember { mutableStateOf(true) }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface.copy(alpha = 0.5f))
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = GlassBorder,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showContextPanel = !showContextPanel }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REAL-TIME AI CONTEXT MATRIX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Connected", fontSize = 9.sp, color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Icon(
                        if (showContextPanel) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null, tint = GrayText, modifier = Modifier.size(18.dp)
                    )
                }
                
                if (showContextPanel) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContextCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.GpsFixed,
                            title = "Goal",
                            value = userProfile.goal
                        )
                        ContextCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Favorite,
                            title = "Stats",
                            value = "${userProfile.height.toInt()}cm/${userProfile.weight.toInt()}kg"
                        )
                        ContextCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.WaterDrop,
                            title = "Water",
                            value = "${(waterMl / 1000.0).format(2)}/2.8L"
                        )
                        ContextCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.LocalFireDepartment,
                            title = "Energy",
                            value = "${foodCals}kcal"
                        )
                    }
                }
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(messages) { message ->
                    val isAssistant = message.role == "assistant"
                    val bubbleBg = if (isAssistant) GlassSurface else PrimaryGreen.copy(alpha = 0.2f)
                    val bubbleBorder = if (isAssistant) GlassBorder else PrimaryGreen
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isAssistant) Alignment.Start else Alignment.End
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isAssistant) 4.dp else 16.dp,
                                        bottomEnd = if (isAssistant) 16.dp else 4.dp
                                    )
                                )
                                .background(bubbleBg)
                                .border(
                                    1.dp,
                                    bubbleBorder,
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isAssistant) 4.dp else 16.dp,
                                        bottomEnd = if (isAssistant) 16.dp else 4.dp
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = message.text,
                                color = LightText,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Coach is thinking...", fontSize = 12.sp, color = GrayText)
                        }
                    }
                }
            }

            // Quick Chips
            if (messages.size == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickQuestions.forEach { question ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GlassSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    scope.launch {
                                        isLoading = true
                                        messages.add(Message("user", question))
                                        listState.animateScrollToItem(messages.size - 1)
                                        
                                        if (userId.isNotEmpty()) {
                                            chatRepository.addChatMessage(userId, ChatMessage("user", question, getCurrentTimestamp()))
                                        }
                                        
                                        try {
                                            val reply = chatHandler(question)
                                            messages.add(Message("assistant", reply))
                                            
                                            if (userId.isNotEmpty()) {
                                                chatRepository.addChatMessage(userId, ChatMessage("assistant", reply, getCurrentTimestamp()))
                                            }
                                        } catch (e: Exception) {
                                            println("[AIChatAssistantScreen Quick Chips] Chat response exception:")
                                            e.printStackTrace()
                                            messages.add(Message("assistant", "Couldn't reach AI model server. Check your connection or retry shortly."))
                                        } finally {
                                            isLoading = false
                                            listState.animateScrollToItem(messages.size - 1)
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(question, fontSize = 11.sp, color = LightText)
                        }
                    }
                }
            }

            // Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Ask your nutritionist coach...", color = GrayText, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(GlassSurface),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                val messageToSend = textInput
                                textInput = ""
                                scope.launch {
                                    isLoading = true
                                    messages.add(Message("user", messageToSend))
                                    listState.animateScrollToItem(messages.size - 1)
                                    
                                    if (userId.isNotEmpty()) {
                                        chatRepository.addChatMessage(userId, ChatMessage("user", messageToSend, getCurrentTimestamp()))
                                    }
                                    
                                    try {
                                        val reply = chatHandler(messageToSend)
                                        messages.add(Message("assistant", reply))
                                        
                                        if (userId.isNotEmpty()) {
                                            chatRepository.addChatMessage(userId, ChatMessage("assistant", reply, getCurrentTimestamp()))
                                        }
                                    } catch (e: Exception) {
                                        println("[AIChatAssistantScreen] Chat response exception:")
                                        e.printStackTrace()
                                        messages.add(Message("assistant", "Couldn't reach AI model server. Check your connection or retry shortly."))
                                    } finally {
                                        isLoading = false
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Send", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp)) // height for navigation padding
        }
    }
}

@Composable
fun ContextCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBackground.copy(alpha = 0.5f))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = GrayText, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 11.sp, color = LightText, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
