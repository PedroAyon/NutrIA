import { extractRecipeGenerationRequirements, generateRecipeFlow } from "../ai";
import { ChatHistoryType, UserIntentionType } from "../types";

export async function handleGenerateRecipe(
  chatHistory: ChatHistoryType,
  pictureIngredients: string[],
  action: UserIntentionType,
  userId: string
) {
  let requirements = "";
  if (action == UserIntentionType.GENERATE_RECIPE) {
    requirements = await extractRecipeGenerationRequirements({ chatHistory, userId });

  } else {
    requirements = await extractRecipeGenerationRequirements({
      chatHistory,
      userId,
      modifyingRecipe: true,
    });
  }

  if (pictureIngredients.length > 0) {
    requirements += `\nUser added one or more pictures, you have to consider this extracted ingredients from pictures for your recipe: ${pictureIngredients.join(", ")}`;
  }
  const recipe = await generateRecipeFlow({ requirements: requirements });
  return {
    actionPerformed: UserIntentionType.GENERATE_RECIPE,
    recipe,
  };
}
