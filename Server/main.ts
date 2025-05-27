import dotenv from "dotenv";
import express from "express";
import messageRouter from "./src/routes/message";
import recipeRouter from "./src/routes/recipe";
import { syncDB } from "./src/db/sync";
dotenv.config();

const app = express();
const port = 3000;

app.use(express.json());
app.use(messageRouter);
app.use(recipeRouter);

// First sync DB, then start server
syncDB().then(() => {
  app.listen(port, () => {
    console.log(`🚀 Server running at http://localhost:${port}`);
  });
});
