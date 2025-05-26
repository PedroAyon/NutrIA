import {
  answerQuestion,
  extractIngredientsFlow,
  extractUserIntentionFlow,
  generateRecipeFlow,
} from "../ai";
import { ChatHistoryType, UserActions, UserIntention } from "../types";

export async function handleUserMessage(
  chatHistory: ChatHistoryType,
  picturePaths: string[],
  authToken: string
) {
  try {
    const { intention } = await extractUserIntentionFlow({ chatHistory });
    console.log("Extracted intention:", intention);
    const pictureIngredients: string[] = [];
    if (picturePaths.length > 0) {
      console.log("User uploaded pictures:", picturePaths);
      for (const picturePath of picturePaths) {
        console.log("Extracting ingredients from picture:", picturePath);
        const extracted = await extractIngredientsFlow({
          imageURL: picturePath,
        });
        console.log("Extracted ingredients:", extracted.ingredients);
        pictureIngredients.push(...extracted.ingredients);
      }
    }

    const sortedHistory = [...chatHistory].sort((a, b) => a.id - b.id);

    const lastUserMessage = [...sortedHistory]
      .reverse()
      .find((msg) => msg.role === "user");

    if (!lastUserMessage) {
      throw new Error("No user prompt found in chat history.");
    }
    console.log("Last user message:", lastUserMessage.text);

    switch (intention) {
      case UserActions.QUESTION:
        const message = await answerQuestion({
          chatHistory: sortedHistory,
        });

        return {
          message: message,
        };
      case UserActions.GENERATE_RECIPE:
        let requirements = `User requirements: ${lastUserMessage.text}`;
        if (pictureIngredients.length > 0) {
          requirements += `\nUser added one or more pictures, extracted ingredients from pictures: ${pictureIngredients.join(", ")}`;
        }
        const recipe = await generateRecipeFlow({ requirements: requirements})
        return {
          message: "Recipe generated successfully",
          recipe,
        };
      case UserActions.MODIFY_RECIPE:
        // Handle recipe modification
        console.log("User wants to modify a recipe.");
        break;
      case UserActions.DELETE_RECIPE:
        // Handle recipe deletion
        console.log("User wants to delete a recipe.");
        break;
      case UserActions.ALTER_SHOPPING_LIST:
        // Handle shopping list alteration
        console.log("User wants to alter the shopping list.");
        break;
      case UserActions.UNKNOWN:
        return {
          message: "I cannot respond to this message.",
        };
        break;
      default:
        throw new Error(`Unhandled user intention: ${intention}`);
    }

    return {
      message: "User interaction handled successfully",
      chatHistory,
      intention,
      picturePaths,
      authToken,
    };
  } catch (err: any) {
    console.error("Error handling user interaction:", err);
    throw new Error(err.message || "Failed to handle user interaction");
  }
}
