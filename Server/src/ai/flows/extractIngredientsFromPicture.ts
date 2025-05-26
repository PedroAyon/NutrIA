// tools/extractIngredientsFromPicture.ts
import { z } from "genkit";
import { readFile } from "fs/promises"; // For reading the image file

// This function will define and return the tool
export function defineExtractIngredientsFlow(aiInstance: any) {
  return aiInstance.defineFlow(
    {
      name: "extractIngredientsFromPicture",
      inputSchema: z.object({ imageURL: z.string() }),
      outputSchema: z.object({ ingredients: z.array(z.string()) }),
    },
    async ({ imageURL }: { imageURL: string }) => {
      // Ensure the imageURL is a local path for readFile
      // If imageURL is a web URL, you'd use a different method (e.g., axios/fetch)
      const data = await readFile(imageURL);
      const { output } = await aiInstance.generate({
        prompt: [
          { media: { url: `data:image/jpeg;base64,${data.toString("base64")}` } },
          {
            text: "Extract all the ingredients from this image, do not miss any. If possible or readable, tell the quantity and (or not) unit of measurement for each ingredient, if not, just name the ingredient.",
          },
        ],
        output: {
          schema: z
            .object({ ingredients: z.array(z.string()) })
            .describe(
              "A list of ingredients extracted from the image, with quantity and units if possible. If the image has no ingredients, return an empty array."
            ),
        },
      });
      if (output == null) {
        throw new Error("Response doesn't satisfy schema.");
      }
      return output;
    }
  );
}