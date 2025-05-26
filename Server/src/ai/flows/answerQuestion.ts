import { z } from "genkit";
import { ChatHistory, ChatHistoryType, UserIntention } from "../../types";

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
      const sortedHistory = [...chatHistory].sort((a, b) => a.id - b.id);

      const lastUserMessage = [...sortedHistory]
        .reverse()
        .find((msg) => msg.role === "user");

      if (!lastUserMessage) {
        throw new Error("No user prompt found in chat history.");
      }

      const formattedHistory = sortedHistory
        .map((msg) => `${msg.role}: ${msg.text}`)
        .join("\n");

      const { text } = await aiInstance.generate({
        prompt: [
          {
            text: `Answer the user's question based on the provided user prompt and the chat history (sorted from older to newer).
                    User prompt: ${lastUserMessage.text}
                    Chat history: ${formattedHistory}`,
          },
        ],
        system:
          "You are a helpful assistant that answers questions about nutrition and cooking. Provide a clear and concise answer to the user's question.",
      });

      if (text == null || typeof text !== 'string') {
        throw new Error("Response doesn't satisfy schema.");
      }
      
      return text;
    }
  );
}