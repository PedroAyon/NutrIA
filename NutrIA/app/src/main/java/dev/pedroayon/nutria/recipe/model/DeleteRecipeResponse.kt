package dev.pedroayon.nutria.recipe.model

import com.google.gson.annotations.SerializedName

data class DeleteRecipeResponse(
    @SerializedName("message") val message: String
)