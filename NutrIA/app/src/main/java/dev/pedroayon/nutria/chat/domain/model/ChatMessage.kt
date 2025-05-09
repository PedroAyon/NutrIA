package dev.pedroayon.nutria.chat.domain.model

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val suggestedMessages: List<String>? = null
)
