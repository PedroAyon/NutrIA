import dotenv from "dotenv";
import express from "express";
import sendRoute from "./src/routes/send";
import { syncDB } from "./src/db/sync";
dotenv.config();

const app = express();
const port = 3000;

app.use(express.json());
app.use(sendRoute);

// First sync DB, then start server
syncDB().then(() => {
  app.listen(port, () => {
    console.log(`🚀 Server running at http://localhost:${port}`);
  });
});
