import {
  extractUserIntentionFlow,
} from "../ai";
import { ChatHistoryType, UserActions, UserIntention } from "../types";
import { handleGenerateRecipe } from "./generateRecipe";
import { handleQuestion } from "./question";
import { extractIngredientsFromPictures } from "./utils";

export async function handleUserMessage(
  chatHistory: ChatHistoryType,
  picturePaths: string[],
  authToken: string
) {
  try {
    const { intention } = await extractUserIntentionFlow({ chatHistory });
    console.log("Extracted intention:", intention);

    const pictureIngredients = await extractIngredientsFromPictures(
      picturePaths
    );
    

    // Get last message from the user
    const sortedHistory = [...chatHistory].sort((a, b) => a.id - b.id);

    const lastUserMessage = [...sortedHistory]
      .reverse()
      .find((msg) => msg.role === "user");

    if (!lastUserMessage) {
      throw new Error("No user prompt found in chat history.");
    }

    switch (intention) {
      case UserActions.QUESTION:
        return await handleQuestion(sortedHistory);
      case UserActions.GENERATE_RECIPE:
        return await handleGenerateRecipe(lastUserMessage, pictureIngredients);
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
