package dev.pedroayon.nutria.chat.domain.model

import com.google.gson.annotations.SerializedName

enum class MessageRole {
    @SerializedName("user")
    USER,

    @SerializedName("assistant")
    ASSISTANT
}