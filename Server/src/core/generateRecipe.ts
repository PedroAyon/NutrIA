import { generateRecipeFlow } from "../ai";
import { MessageType, UserActions } from "../types";

export async function handleGenerateRecipe(
  lastUserMessage: MessageType,
  pictureIngredients: string[]
) {
  let requirements = `User requirements: ${lastUserMessage.text}`;
  if (pictureIngredients.length > 0) {
    requirements += `\nUser added one or more pictures, extracted ingredients from pictures: ${pictureIngredients.join(
      ", "
    )}`;
  }
  const recipe = await generateRecipeFlow({ requirements: requirements });
  return {
    actionPerformed: UserActions.GENERATE_RECIPE,
    recipe,
  };
}
