package dev.pedroayon.nutria.auth.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Recipe(
    val calories: Int,
    val description: String,
    val ingredients: List<Ingredient>,
    val instructions: List<Instruction>,
    val name: String,
    val prepTime: String
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("# Receta: ${name}\n\n")
        sb.append("## Descripción General\n")
        sb.append("- ${description}\n\n")
        sb.append("- **Calories**: ${calories}\n")
        sb.append("- **Preparation Time**: ${prepTime}\n")

        sb.append("## Ingredientes\n")
        ingredients.forEachIndexed { index, ingredient ->
            sb.append("${index + 1}. ${ingredient.quantity} ${ingredient.unit} of ${ingredient.name}\n")
        }
        sb.append("\n")

        sb.append("## Instrucciones\n")
        instructions.forEach { instruction ->
            sb.append("### Paso ${instruction.step}: ${instruction.description}\n")
            sb.append("  - **Duración**: ${instruction.duration}\n")
            sb.append("  - **Detalles**: ${instruction.instructions}\n")
        }
        return sb.toString()
    }
}

@Serializable
data class Ingredient(
    val name: String,
    val quantity: String,
    val unit: String
)

@Serializable
data class Instruction(
    val description: String,
    val instructions: String,
    val step: Int,
    val duration: String
)