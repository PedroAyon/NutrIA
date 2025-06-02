package dev.pedroayon.nutria.common.model

import com.google.gson.annotations.SerializedName

data class Recipe(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("ingredients") val ingredients: List<Ingredient>,
    @SerializedName("instructions") val instructions: List<Instruction>,
    @SerializedName("prepTime") val prepTime: String,
    @SerializedName("calories") val calories: Int,
    @SerializedName("userId") val userId: String? = "1",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("# Receta: ${name}\n\n")
        sb.append("## Descripción General\n")
        sb.append("- ${description}\n\n") // Two newlines for paragraph break in some Markdown
        sb.append("- **Calorías**: ${calories} kcal\n") // Added kcal for clarity, adjust if not desired
        sb.append("- **Tiempo de Preparación**: ${prepTime}\n\n")

        sb.append("## Ingredientes\n")
        if (ingredients.isEmpty()) {
            sb.append("No hay ingredientes listados.\n")
        } else {
            ingredients.forEachIndexed { index, ingredient ->
                // Original: sb.append("${index + 1}. ${ingredient.quantity} ${ingredient.unit} of ${ingredient.name}\n")
                // Adjusted to match Spanish from example image implicitly ("de")
                sb.append("${index + 1}. ${ingredient.quantity} ${ingredient.unit} de ${ingredient.name}\n")
            }
        }
        sb.append("\n")

        sb.append("## Instrucciones\n")
        if (instructions.isEmpty()) {
            sb.append("No hay instrucciones listadas.\n")
        } else {
            instructions.forEach { instruction ->
                sb.append("### Paso ${instruction.step}: ${instruction.description}\n")
                // Handle nullable duration
                sb.append("  - **Duración**: ${instruction.duration ?: "No especificada"}\n")
                sb.append("  - **Detalles**: ${instruction.instructions}\n\n") // Added newline for better separation between steps
            }
        }
        return sb.toString().trim() // Trim trailing newlines if any
    }
}