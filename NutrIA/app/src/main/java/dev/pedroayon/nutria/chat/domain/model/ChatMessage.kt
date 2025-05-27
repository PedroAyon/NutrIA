package dev.pedroayon.nutria.chat.domain.model

import dev.pedroayon.nutria.auth.domain.model.MessageType

data class ChatMessage(
    val text: String,
    val messageType: MessageType,
)
