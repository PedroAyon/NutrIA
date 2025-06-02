import { extractIngredientsFlow } from "../ai";

export async function extractIngredientsFromPictures(
  picturePaths: string[]
) {
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
  return pictureIngredients;
}
