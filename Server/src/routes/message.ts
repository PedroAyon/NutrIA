import express, { Request, Response } from "express";
import fs from "fs";
import path from "path";
import { ChatHistory, ChatHistoryType } from "../types";
import { upload } from "../config/multer";
import { handleUserMessage } from "../core";

const router = express.Router();

router.post(
  "/message/send",
  upload.array("photos"),
  async (req: Request, res: Response) => {
    let chatHistory: ChatHistoryType;

    try {
      const authHeader = req.headers.authorization;
      if (!authHeader || !authHeader.startsWith("Bearer ")) {
        res.status(401).json({ error: "Missing or invalid auth token" });
        console.log(authHeader)
        return;
      }
      const token = authHeader.replace("Bearer ", "");

      const rawHistory = req.body.chatHistory;
      const parsed =
        typeof rawHistory === "string" ? JSON.parse(rawHistory) : rawHistory;

      chatHistory = ChatHistory.parse(parsed);

      // Parse shopping list from body (optional)
      let shoppingList: string[] = [];
      const rawShoppingList = req.body.shoppingList;
      if (rawShoppingList) {
        shoppingList =
          typeof rawShoppingList === "string"
            ? JSON.parse(rawShoppingList)
            : rawShoppingList;
      }

      const photos = (req.files as Express.Multer.File[]) || [];
      const photoPaths = photos.map((file) => path.resolve(file.path));

      const response = await handleUserMessage(
        chatHistory,
        photoPaths,
        token,
        shoppingList
      );

      res.json(response);
    } catch (err: any) {
      console.error("Error:", err);
      res.status(400).json({ error: err.message || "Invalid request" });
    } finally {
      const files = (req.files as Express.Multer.File[]) || [];
      await Promise.all(
        files.map((file) =>
          fs.promises
            .unlink(file.path)
            .catch((err) => console.warn(`Could not delete ${file.path}:`, err))
        )
      );
    }
  }
);

export default router;
