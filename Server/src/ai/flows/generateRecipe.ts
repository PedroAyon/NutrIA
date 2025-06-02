// tools/generateRecipe.ts
import { z } from "genkit";
import { Recipe } from "../../types"; // Assuming Recipe is defined in types.ts

export function defineGenerateRecipeFlow(aiInstance: any) {
  return aiInstance.defineFlow(
    {
      name: "generateRecipe",
      inputSchema: z.object({
        requirements: z
          .string()
          .optional()
          .describe(
            "Requirements for the recipe to make or edit."
          ),
      }),
      outputSchema: Recipe,
    },
    async ({
      requirements,
    }: {
      ingredients?: string[];
      requirements?: string;
    }) => {
      const { output } = await aiInstance.generate({
        prompt: [
          {
            text: `Generate a single food recipe. User Requierements:\n${requirements}`,
          },
        ],
        system:
          "You are a helpful assistant that suggests healthy food recipes (when possible) based on requirements or ingredients. Answer in the language of the user requirements. Answer in the user's language.",
        output: { schema: Recipe },
      });
      if (output == null) {
        throw new Error("Response doesn't satisfy schema.");
      }
      return output;
    }
  );
}
