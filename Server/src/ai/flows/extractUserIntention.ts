import { z } from "genkit";
import { ChatHistory, ChatHistoryType, UserIntention } from "../../types";

export function extractUserIntention(aiInstance: any) {
  return aiInstance.defineFlow(
    {
      name: "extractUserIntention",
      inputSchema: z.object({
        chatHistory: ChatHistory,
      }),
      outputSchema: UserIntention,
    },
    async ({ chatHistory }: { chatHistory: ChatHistoryType }) => {
      const lastUserMessage = [...chatHistory]
        .reverse()
        .find((msg) => msg.role === "user");

      if (!lastUserMessage) {
        throw new Error("No user prompt found in chat history.");
      }

      const formattedHistory = chatHistory
        .map((msg) => `${msg.role.toUpperCase()}: ${msg.text}`)
        .join("\n");

      const { output } = await aiInstance.generate({
        prompt: [
          {
            text: `You are given a chat history between a user and an assistant.
Your task is to extract the user's **current intention** based on their last message, using the full context of the conversation.

Chat history (from oldest to newest):
${formattedHistory}

Based on the last user message: "${lastUserMessage.text}", what is the user's intention?`,
          },
        ],
        system: `You are a classification agent in a nutrition and cooking assistant system.
Your only job is to identify the user's intention from their most recent message and context.

Be cautious not to misclassify — if the user's intent is vague or unrelated, return ACTION_UNKNOWN.`,
        output: {
          schema: UserIntention,
        },
      });

      if (!output) {
        throw new Error("Failed to extract intention: no valid output.");
      }

      return output;
    }
  );
}
