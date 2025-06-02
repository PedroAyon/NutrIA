package dev.pedroayon.nutria.recipe.model

import com.google.gson.annotations.SerializedName
import dev.pedroayon.nutria.common.model.Recipe

data class GetRecipesResponse(
    @SerializedName("recipes") val recipes: List<Recipe>
)