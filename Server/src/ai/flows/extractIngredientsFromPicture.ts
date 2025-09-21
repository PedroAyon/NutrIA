import { z } from "genkit";
import { readFile } from "fs/promises";

export function defineExtractIngredientsFlow(aiInstance: any) {
  return aiInstance.defineFlow(
    {
      name: "extractIngredientsFromPicture",
      inputSchema: z.object({
        imageURL: z.string().describe("Path to the image file containing the ingredients."),
      }),
      outputSchema: z.object({
        ingredients: z.array(z.string()).describe("List of detected ingredients, with quantity and units if visible."),
      }),
    },
    async ({ imageURL }: { imageURL: string }) => {
      const data = await readFile(imageURL);

      const { output } = await aiInstance.generate({
        prompt: [
          {
            media: { url: `data:image/jpeg;base64,${data.toString("base64")}` },
          },
          {
            text: `You are looking at a picture of food ingredients.

Your task is to extract all visible ingredients from the image. Follow these instructions:
- List every identifiable ingredient (e.g., 'onion', 'milk', 'chicken').
- If possible, include quantity and unit (e.g., '2 cups of flour').
- If quantity or unit is unclear, just mention the ingredient name.
- Do not fabricate or guess items. If unsure, omit them.

Return only the ingredients in a structured list.
Answer in Spanish.`,
          },
        ],
        system: `You are a visual ingredient extraction assistant. Your job is to analyze images of ingredients
and return a clean list of what is present.

Respond only with what is visible and identifiable. If nothing can be identified, return an empty array.`,
        output: {
          schema: z.object({
            ingredients: z.array(z.string()),
          }),
        },
      });

      if (!output) {
        throw new Error("Response doesn't satisfy schema.");
      }

      return output;
    }
  );
}
