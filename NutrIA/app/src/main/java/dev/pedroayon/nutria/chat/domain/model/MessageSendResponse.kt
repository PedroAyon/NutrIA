package dev.pedroayon.nutria.chat.domain.model

import com.google.gson.annotations.SerializedName
import dev.pedroayon.nutria.common.model.Recipe
import dev.pedroayon.nutria.common.model.UserIntentionType

data class MessageSendResponse(
    @SerializedName("actionPerformed") val actionPerformed: UserIntentionType,
    @SerializedName("recipe") val recipe: Recipe? = null,
    @SerializedName("shoppingList") val shoppingList: List<String>? = null,
    @SerializedName("message") val message: String? = null // For general text responses from the bot
)