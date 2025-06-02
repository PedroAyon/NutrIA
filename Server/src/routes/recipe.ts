import express, { Request, Response } from "express";
import { ZodError } from "zod";
import { Recipe as zRecipe } from "../types";
import { Recipe } from "../db/models/Recipe";
import { User } from "../db/models/User";

const router = express.Router();

router.post("/recipe", async (req: Request, res: Response) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    res.status(401).json({ error: "Missing or invalid auth token" });
    return;
  }

  const token = authHeader.replace("Bearer ", "");

  try {
    const parsedRecipe = zRecipe.parse(req.body);
    console.log("Parsed recipe:", parsedRecipe);

    // Verificar si el usuario existe, y crearlo si no
    const [user, created] = await User.findOrCreate({
      where: { id: token },
    });

    if (created) {
      console.log(`User with ID ${token} was created`);
    }
    
    const newRecipe = await Recipe.create({
      userId: token,
      name: parsedRecipe.name,
      description: parsedRecipe.description,
      ingredients: parsedRecipe.ingredients,
      instructions: parsedRecipe.instructions,
      prepTime: parsedRecipe.prepTime,
      calories: parsedRecipe.calories,
    });

    console.log("Recipe's auto-generated ID:", newRecipe.get("id"));

    res.status(201).json({ message: "Recipe saved successfully", recipeId: newRecipe.get("id") });
  } catch (err) {
    if (err instanceof ZodError) {
      res.status(400).json({
        error: "Invalid recipe format",
        details: err.errors,
      });
    } else {
      res.status(500).json({ error: "Server error" });
    }
  }
});

router.delete("/recipe/:id", async (req: Request, res: Response) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    res.status(401).json({ error: "Missing or invalid auth token" });
    return;
  }

  const token = authHeader.replace("Bearer ", "");
  const recipeId = req.params.id;

  try {
    const recipe = await Recipe.findOne({
      where: {
        id: recipeId,
        userId: token,
      },
    });

    if (!recipe) {
      res.status(404).json({ error: "Recipe not found or not authorized" });
      return;
    }

    await recipe.destroy();

    res.status(200).json({ message: "Recipe deleted successfully" });
  } catch (err) {
    res.status(500).json({ error: "Server error" });
  }
});

router.get("/recipes", async (req: Request, res: Response) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    res.status(401).json({ error: "Missing or invalid auth token" });
    return;
  }

  const token = authHeader.replace("Bearer ", "");

  try {
    const recipes = await Recipe.findAll({
      where: { userId: token },
      order: [["createdAt", "DESC"]],
    });

    res.status(200).json({ recipes });
  } catch (err) {
    res.status(500).json({ error: "Server error" });
  }
});



export default router;
