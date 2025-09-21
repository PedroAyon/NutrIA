package dev.pedroayon.nutria.chat.domain.model

import dev.pedroayon.nutria.common.model.Recipe // Our new Recipe model
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(), // For unique identification in LazyColumn
    var text: String, // Can be user input, bot text, or recipe.toString()
    val messageType: MessageType,
    var recipe: Recipe? = null, // Holds the actual Recipe object
    var isRecipeSavedInMemory: Boolean = false, // UI state for HeartSwitch, update after API call
    val apiMessageId: Int? = null // Optional: If your API messages have IDs you want to track
)