package dev.pedroayon.nutria.common.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShoppingListManager(context: Context) {

    private val PREFS_NAME = "nutria_prefs"
    private val SHOPPING_LIST_KEY = "shopping_list"
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // MutableStateFlow to hold the current shopping list
    private val _shoppingListFlow = MutableStateFlow<List<String>>(emptyList())
    val shoppingListFlow: StateFlow<List<String>> = _shoppingListFlow.asStateFlow()

    init {
        // Initialize the flow with the saved list when the manager is created
        loadShoppingList()
    }

    private fun loadShoppingList() {
        val json = prefs.getString(SHOPPING_LIST_KEY, null)
        // Explicitly cast the result of fromJson to List<String> to help Kotlin's inference
        val list: List<String> = if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
        _shoppingListFlow.value = list // Update the flow with the loaded list
    }

    fun getShoppingList(): List<String> {
        // This method can still be used if you need the current value without observing
        return _shoppingListFlow.value
    }

    fun saveShoppingList(shoppingList: List<String>) {
        val json = gson.toJson(shoppingList)
        prefs.edit().putString(SHOPPING_LIST_KEY, json).apply()
        _shoppingListFlow.value = shoppingList // Update the flow after saving
    }
}