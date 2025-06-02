package dev.pedroayon.nutria.common.model

import com.google.gson.annotations.SerializedName

data class Instruction(
    @SerializedName("step") val step: Int,
    @SerializedName("description") val description: String,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("instructions") val instructions: String
)
