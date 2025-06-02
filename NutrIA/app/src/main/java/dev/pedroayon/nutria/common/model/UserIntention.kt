package dev.pedroayon.nutria.common.model

import com.google.gson.annotations.SerializedName

data class UserIntention(
    @SerializedName("intention") val intention: UserIntentionType
)