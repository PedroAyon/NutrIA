import { extractUserIntentionFlow, generateShoppingListFlow } from "../ai";
import { ChatHistoryType, UserIntentionType, UserIntention } from "../types";
import { handleGenerateRecipe } from "./generateRecipe";
import { handleQuestion } from "./question";
import { extractIngredientsFromPictures } from "./picture";

export async function handleUserMessage(
  chatHistory: ChatHistoryType,
  picturePaths: string[],
  userId: string,
  shoppingList: string[] = []
) {
  try {
    const { intention } = await extractUserIntentionFlow({ chatHistory });
    console.log("Extracted intention:", intention);

    const pictureIngredients = await extractIngredientsFromPictures(
      picturePaths
    );

    // Get last message from the user
    const sortedHistory = [...chatHistory].sort((a, b) => a.id - b.id);

    switch (intention) {
      case UserIntentionType.QUESTION:
        return await handleQuestion(sortedHistory);
      case UserIntentionType.GENERATE_RECIPE:
      case UserIntentionType.MODIFY_RECIPE:
        return await handleGenerateRecipe(
          chatHistory,
          pictureIngredients,
          intention,
          userId
        );
      case UserIntentionType.ALTER_SHOPPING_LIST:
        return {
          actionPerformed: UserIntentionType.ALTER_SHOPPING_LIST,
          shoppingList: await generateShoppingListFlow({
            chatHistory,
            currentShoppingList: shoppingList,
            userId,
          }),
        };
      case UserIntentionType.UNKNOWN:
        return {
          actionPerformed: UserIntentionType.UNKNOWN,
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
      authToken: userId,
    };
  } catch (err: any) {
    console.error("Error handling user interaction:", err);
    throw new Error(err.message || "Failed to handle user interaction");
  }
}
