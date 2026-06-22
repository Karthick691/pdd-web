package com.foodsnap.nutritionai.repository

import com.foodsnap.nutritionai.model.ChatMessage

interface ChatRepository {
    suspend fun getChatHistory(userId: String): List<ChatMessage>
    suspend fun addChatMessage(userId: String, message: ChatMessage)
    suspend fun clearChatHistory(userId: String)
}
