package dev.pedroayon.nutria.common.model

import com.google.gson.annotations.SerializedName

enum class UserIntentionType {
    @SerializedName("ACTION_GENERATE_RECIPE")
    GENERATE_RECIPE,
    @SerializedName("ACTION_MODIFY_RECIPE")
    MODIFY_RECIPE,
    @SerializedName("ACTION_ALTER_SHOPPING_LIST")
    ALTER_SHOPPING_LIST,
    @SerializedName("ACTION_QUESTION_ABOUT_NUTRITION_OR_COOKING")
    QUESTION,
    @SerializedName("ACTION_UNKNOWN")
    UNKNOWN
}