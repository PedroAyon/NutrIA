import { z } from "genkit";
import { ChatHistory, ChatHistoryType } from "../../types";

export function defineAnswerQuestionFlow(aiInstance: any) {
  return aiInstance.defineFlow(
    {
      name: "answerQuestion",
      inputSchema: z.object({
        chatHistory: ChatHistory,
      }),
      outputSchema: z.string(),
    },
    async ({ chatHistory }: { chatHistory: ChatHistoryType }) => {
      const sortedHistory = chatHistory.slice().sort((a, b) => a.id - b.id);

      const lastUserMessage = sortedHistory
        .slice()
        .reverse()
        .find((msg) => msg.role === "user");

      if (!lastUserMessage) {
        throw new Error("No user message found in chat history.");
      }

      const formattedHistory = sortedHistory
        .map((msg) => `${msg.role}: ${msg.text}`)
        .join("\n");

      const { text } = await aiInstance.generate({
        prompt: [
          {
            text: `A user has asked a question related to nutrition or cooking.

Your task is to provide a helpful and concise answer based on the latest user question and the full context of the conversation.

User question: ${lastUserMessage.text}

Full chat history for context:
${formattedHistory}
`,
          },
        ],
        system: `You are NutrIA, a helpful AI assistant that specializes in nutrition and cooking.

- Answer clearly and naturally, like a human.
- Provide practical and healthy suggestions when relevant.
- You may use ingredients the user mentions or infers from pictures.
- Always respond in the user's language.
- Keep your tone warm, friendly, and informative.`,
      });

      if (!text || typeof text !== "string") {
        throw new Error("Response doesn't satisfy schema.");
      }

      return text;
    }
  );
}
