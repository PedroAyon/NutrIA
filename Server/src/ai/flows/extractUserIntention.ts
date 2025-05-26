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

      const { output } = await aiInstance.generate({
        prompt: [
          {
            text: `Extract the user's intention from the provided user prompt and the chat history (sorted from older to newer).
                    User prompt: ${lastUserMessage.text}
                    Chat history: ${formattedHistory}`,
          },
        ],
        system:
          "You are part of a multiagent system with a chatbot to help with nutrition and cooking. You are a utility tool that extracts the user's intention from their last message. This intention is the action the user wants to perform.",
        output: {
          schema: UserIntention,
        },
      });
      if (output == null) {
        throw new Error("Response doesn't satisfy schema.");
      }
      return output;
    }
  );
}
