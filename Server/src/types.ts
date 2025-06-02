import { z } from "genkit";

export const Ingredient = z
  .object({
    name: z.string().describe("The name of the ingredient."),
    quantity: z.string().describe("The quantity of the ingredient."),
    unit: z.string().describe("The unit of measurement for the ingredient."),
  })
  .describe("An ingredient used in a recipe.");

export const IngredientList = z
  .array(Ingredient)
  .describe("A list of ingredients used in the recipe.");

export const Instruction = z
  .object({
    step: z.number().describe("Number of step."),
    description: z.string().describe("A very breve description of the step."),
    duration: z
      .string()
      .optional()
      .describe("The time required for this step, if applicable."),
    instructions: z.string().describe("Detailed instructions for this step."),
  })
  .describe("An instruction for preparing the recipe.");

export const GenerateRecipeInput = z
  .object({
    ingredients: z.array(z.string()).optional(),
    requirements: z.string().optional(),
  })
  .refine((data) => data.ingredients?.length || data.requirements, {
    message: "If no ingredients are provided, requirements must not be null.",
    path: ["ingredients", "requirements"],
  });

export type GenerateRecipeInputType = z.infer<typeof GenerateRecipeInput>;


export enum UserIntentionType {
  GENERATE_RECIPE = "ACTION_GENERATE_RECIPE",
  MODIFY_RECIPE = "ACTION_MODIFY_RECIPE",
  ALTER_SHOPPING_LIST = "ACTION_ALTER_SHOPPING_LIST",
  QUESTION = "ACTION_QUESTION_ABOUT_NUTRITION_OR_COOKING",
  UNKNOWN = "ACTION_UNKNOWN",
}

export const UserIntention = z.object({
  intention: z.enum([
    UserIntentionType.GENERATE_RECIPE,
    UserIntentionType.MODIFY_RECIPE,
    UserIntentionType.ALTER_SHOPPING_LIST,
    UserIntentionType.QUESTION,
    UserIntentionType.UNKNOWN,
  ])
    .describe(`The user's intention based on the chat history. Possible values are:
      - ${UserIntentionType.GENERATE_RECIPE}: User wants to generate or create a recipe.
      - ${UserIntentionType.MODIFY_RECIPE}: User wants to modify an existing recipe.
      - ${UserIntentionType.ALTER_SHOPPING_LIST}: User wants to alter the shopping list.
      - ${UserIntentionType.QUESTION}: User has a question about nutrition or cooking, or something else related to food, or asking about what the AI assitant can do, or just greeting the AI.
      - ${UserIntentionType.UNKNOWN}: User's intention is not clear or does not match any known actions. This is the default value if the user is not asking for something the AI is designed to do.`),
});


export const Recipe = z
  .object({
    name: z
      .string()
      .describe(
        "Food name (or create a name if it doesn't have one). For example, 'Spaghetti Bolognese' or 'Tossed Salad'."
      ),
    description: z.string().describe("A description of the food / drink."),
    ingredients: IngredientList.describe(
      "A list of ingredients used in the recipe."
    ),
    instructions: z
      .array(Instruction)
      .describe("A list of instructions for preparing the recipe."),
    prepTime: z.string().describe("The time it takes to prepare the food."),
    calories: z.number().describe("The estimated number of calories."),
  })
  .describe("A cooking recipe for a food.");

export type RecipeType = z.infer<typeof Recipe>;

export const Message = z.object({
  id: z
    .number()
    .describe("Unique identifier for the message. Greater is newer."),
  role: z
    .enum(["user", "assistant"])
    .describe("The role of the message sender."),
  text: z.string().describe("The content of the message.").optional(),
  imagePaths: z.array(z.string()).optional(),
  recipe: Recipe.optional().describe(
    "The recipe associated with the message, if applicable."
  ),
});

export type MessageType = z.infer<typeof Message>;

export const ChatHistory = z
  .array(Message)
  .describe("The chat history between the user and the assistant.");

export type ChatHistoryType = z.infer<typeof ChatHistory>;

export const ShoppingList = z
  .array(z.string())
  .describe("A shopping list containing ingredients to buy.");

export type ShoppingListType = z.infer<typeof ShoppingList>;