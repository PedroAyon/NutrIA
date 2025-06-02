package dev.pedroayon.nutria.common.data

import dev.pedroayon.nutria.chat.domain.model.CreateRecipeResponse
import dev.pedroayon.nutria.chat.domain.model.MessageSendResponse
import dev.pedroayon.nutria.common.model.Recipe
import dev.pedroayon.nutria.recipe.model.DeleteRecipeResponse
import dev.pedroayon.nutria.recipe.model.GetRecipesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /**
     * Sends a message along with optional photos, chat history, and shopping list.
     * The response varies based on the bot's action.
     *
     * @param token The authorization token (Bearer).
     * @param photos List of photos to upload.
     * @param chatHistory JSON string representation of ChatHistory.
     * @param shoppingList JSON string representation of ShoppingList (optional).
     * @return A MessageSendResponse indicating the action performed and relevant data.
     */
    @Multipart
    @POST("message/send")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Part photos: List<MultipartBody.Part>,
        @Part("chatHistory") chatHistory: RequestBody,
        @Part("shoppingList") shoppingList: RequestBody? = null
    ): Response<MessageSendResponse> // Updated response type

    /**
     * Creates and saves a new recipe provided by the user.
     *
     * @param token The authorization token (Bearer).
     * @param recipe The recipe data to create.
     * @return Confirmation message and the ID of the newly created recipe.
     */
    @POST("recipe")
    suspend fun createRecipe(
        @Header("Authorization") token: String,
        @Body recipe: Recipe // User provides the full recipe object to be saved
    ): Response<CreateRecipeResponse> // Updated response type for recipeId

    /**
     * Deletes a specific recipe by its ID.
     *
     * @param token The authorization token (Bearer).
     * @param recipeId The ID of the recipe to delete.
     * @return Confirmation message of deletion.
     */
    @DELETE("recipe/{id}")
    suspend fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("id") recipeId: String // Path IDs are typically strings
    ): Response<DeleteRecipeResponse>

    /**
     * Retrieves all recipes for the authorized user.
     *
     * @param token The authorization token (Bearer).
     * @return A list of recipes.
     */
    @GET("recipes")
    suspend fun getRecipes(
        @Header("Authorization") token: String
    ): Response<GetRecipesResponse>
}