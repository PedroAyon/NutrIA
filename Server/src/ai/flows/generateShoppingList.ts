import { z } from "genkit";
import { ChatHistory, ChatHistoryType } from "../../types";
import { Recipe } from "../../db/models/Recipe";

export function defineGenerateShoppingListFlow(aiInstance: any) {
  return aiInstance.defineFlow(
    {
      name: "alterShoppingList",
      inputSchema: z.object({
        chatHistory: ChatHistory,
        currentShoppingList: z.array(z.string()),
        userId: z.string(),
      }),
      outputSchema: z.array(z.string()),
    },
    async ({
      chatHistory,
      currentShoppingList,
      userId,
    }: {
      chatHistory: ChatHistoryType;
      currentShoppingList: string[];
      userId: string;
    }) => {
      const recipes = await Recipe.findAll({ where: { userId } });
      const userSavedRecipes = recipes.map((recipe: any) => recipe.toJSON());

      const formattedHistory = chatHistory
        .map((msg) => `${msg.role.toUpperCase()}: ${msg.text}`)
        .join("\n");

      const lastUserMessage = [...chatHistory]
        .reverse()
        .find((msg) => msg.role === "user");

      if (!lastUserMessage) {
        throw new Error("No user message found in chat history.");
      }

      const { output } = await aiInstance.generate({
        prompt: [
          {
            text: `You are an assistant that helps users manage their shopping list for cooking and nutrition.

Here is the current shopping list:
${JSON.stringify(currentShoppingList, null, 2)}

Here is the user's chat history for context (from oldest to newest):
${formattedHistory}

Here are the user's saved recipes:
${JSON.stringify(userSavedRecipes, null, 2)}

Based on the latest user message: "${lastUserMessage.text}", generate a fully updated shopping list as a list of strings.

Return ONLY the updated shopping list. Do NOT explain or include extra text.`,
          },
        ],
        system: `You are a utility AI in a multiagent system. Your job is to return an updated shopping list based on user intent, chat history, and saved recipes. Only return the final list of items.`,
        output: {
          schema: z.array(z.string()),
        },
      });

      if (!output) {
        throw new Error("Failed to generate updated shopping list.");
      }

      return output;
    }
  );
}
