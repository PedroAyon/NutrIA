import { googleAI } from "@genkit-ai/googleai";
import { genkit } from "genkit";
import { extractUserIntention } from "./flows/extractUserIntention";
import { defineExtractIngredientsFlow } from "./flows/extractIngredientsFromPicture";
import { defineGenerateRecipeFlow } from "./flows/generateRecipe";
import { defineAnswerQuestionFlow } from "./flows/answerQuestion";
import { extractRecipeGenerationRequirementsFlow } from "./flows/extractRecipeGenerationRequierements";
import { defineGenerateShoppingListFlow } from "./flows/generateShoppingList";

const ai = genkit({
  plugins: [googleAI()],
  model: googleAI.model("gemini-1.5-flash"),
});

export const extractUserIntentionFlow = extractUserIntention(ai);
export const extractIngredientsFlow = defineExtractIngredientsFlow(ai);
export const generateRecipeFlow = defineGenerateRecipeFlow(ai);
export const answerQuestion = defineAnswerQuestionFlow(ai);
export const extractRecipeGenerationRequirements = extractRecipeGenerationRequirementsFlow(ai);
export const generateShoppingListFlow = defineGenerateShoppingListFlow(ai);
export default ai;
