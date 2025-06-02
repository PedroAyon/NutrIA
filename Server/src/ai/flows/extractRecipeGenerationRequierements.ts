import { z } from "genkit";
import { ChatHistory, ChatHistoryType, RecipeType } from "../../types";
import { Recipe } from "../../db/models/Recipe";

export function extractRecipeGenerationRequirementsFlow(aiInstance: any) {
  return aiInstance.defineFlow(
    {
      name: "extractRecipeGenerationRequirements",
      inputSchema: z.object({
        userId: z.string().describe("The ID of the user making the request."),
        chatHistory: ChatHistory,
        modifyingRecipe: z.boolean().optional().default(false),
      }),
      outputSchema: z
        .string()
        .describe(`User requirements for recipe generation,`),
    },
    async ({
      userId,
      chatHistory,
      modifyingRecipe,
    }: {
      userId: string;
      chatHistory: ChatHistoryType;
      modifyingRecipe: boolean;
    }) => {
      const formattedHistory = chatHistory
        .map((msg) => `${msg.role}: ${msg.text}`)
        .join("\n");
      let userSavedRecipes: RecipeType[] = [];
      if (modifyingRecipe) {
        const recipes = await Recipe.findAll({
          where: {
            userId: userId, // Assuming the first message has the userId
          },
        });
        userSavedRecipes = recipes.map((recipe: any) => recipe.toJSON());
      }

      let promptText = `Extract the user's recipe requirements based on the chat history provided below.

The goal is to identify what kind of recipe the user wants to generate. This can include dietary preferences, ingredient constraints, cuisine type, cooking method, or other specific instructions.

Chat History:
${formattedHistory}
`;

      if (modifyingRecipe) {
        promptText = `The user wants to modify an existing recipe. Your task is to:
1. Identify which recipe the user is referring to (it may be in the chat or among saved recipes).
2. Extract the specific modifications they want to make.

Chat History:
${formattedHistory}

User's Saved Recipes:
${userSavedRecipes.map((r) => `- ${r.name}`).join("\n")}
`;
      }

      const response = await aiInstance.generate({
        prompt: [
          {
            text: promptText,
          },
        ],
        system: `You are part of a multi-agent assistant system that helps users with nutrition and cooking tasks. Your role is to analyze chat conversations and extract clear user intentions for generating or modifying recipes.
Be precise. Focus only on the user's requirements.
- If the user is generating a new recipe, summarize what they want in the recipe (e.g., ingredients, dietary goals, cuisines).
- If the user is modifying a recipe, identify which recipe they're talking about and what changes they requested.

Respond only with the extracted requirements. Do not include explanations or unrelated text.
Return the user requirements for recipe generation or modification. If modifying a recipe, mention the recipe name and a brief summary of requested changes. Examples:
    - "Create a vegetarian dinner using mushrooms and spinach."
    - "Modify 'Spicy Tofu Stir-fry' to be less spicy and add broccoli. Full recipe: <WRITE THE RECIPE OBJECT HERE>"
    - "Low-carb dessert using almond flour and stevia."
`,
      });
      if (response == null) {
        throw new Error("Response doesn't satisfy schema.");
      }
      console.log("Extracted requirements:", response.text);
      return response.text;
    }
  );
}
