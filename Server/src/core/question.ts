import { answerQuestion } from "../ai";
import { ChatHistoryType, UserIntentionType } from "../types";

export async function handleQuestion(chatHistory: ChatHistoryType) {
  const message = await answerQuestion({
    chatHistory: chatHistory,
  });

  return {
    actionPerformed: UserIntentionType.QUESTION,
    message: message,
  };
}
