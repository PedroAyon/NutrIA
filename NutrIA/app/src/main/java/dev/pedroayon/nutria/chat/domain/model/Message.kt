package dev.pedroayon.nutria.chat.domain.model

import com.google.gson.annotations.SerializedName
import dev.pedroayon.nutria.common.model.Recipe

data class Message(
    @SerializedName("id") val id: Int, // Assuming this ID is unique within the chat session
    @SerializedName("role") val role: MessageRole,
    @SerializedName("text") val text: String? = null,
    @SerializedName("imagePaths") val imagePaths: List<String>? = null,
    @SerializedName("recipe") val recipe: Recipe? = null // A message can embed a recipe
)