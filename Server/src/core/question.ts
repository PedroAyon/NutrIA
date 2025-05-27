import { answerQuestion } from "../ai";
import { ChatHistoryType, UserActions } from "../types";

export async function handleQuestion(chatHistory: ChatHistoryType) {
  const message = await answerQuestion({
    chatHistory: chatHistory,
  });

  return {
    actionPerformed: UserActions.QUESTION,
    message: message,
  };
}
