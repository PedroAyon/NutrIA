package dev.pedroayon.nutria.chat.domain.model

import com.google.gson.annotations.SerializedName

data class CreateRecipeResponse(
    @SerializedName("message") val message: String,
    @SerializedName("recipeId") val recipeId: Int // Assuming recipe ID from DB is Int
)