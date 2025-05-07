package dev.pedroayon.nutria.chat.ui.domain.model

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val suggestedMessages: List<String>? = null
)
