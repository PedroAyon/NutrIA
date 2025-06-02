package dev.pedroayon.nutria.common.model

import com.google.gson.annotations.SerializedName

data class ApiErrorResponse(
    @SerializedName("error") val error: String
)