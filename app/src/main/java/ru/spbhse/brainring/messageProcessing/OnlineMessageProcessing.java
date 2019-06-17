package ru.spbhse.brainring.messageProcessing;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerReadyMessage;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerWrittenMessage;
import ru.spbhse.brainring.network.messages.messageTypes.CorrectAnswerAndScoreMessage;
import ru.spbhse.brainring.network.messages.messageTypes.FinishMessage;
import ru.spbhse.brainring.network.messages.messageTypes.IncorrectOpponentAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.QuestionMessage;
import ru.spbhse.brainring.network.messages.messageTypes.TimeLimitMessage;

public class OnlineMessageProcessing {
    public static void process(@NonNull Message message, @NonNull String senderId) {
        switch (message.getMessageCode()) {
            case MessageCodes.ANSWER_IS_READY:
                long time = ((AnswerReadyMessage) message).getTime();
                OnlineController.OnlineAdminLogicController.onAnswerIsReady(senderId, time);
                break;
            case MessageCodes.ANSWER_IS_WRITTEN:
                String answer = ((AnswerWrittenMessage) message).getAnswer();
                OnlineController.OnlineAdminLogicController.onAnswerIsWritten(answer, senderId);
                break;
            case MessageCodes.FORBIDDEN_TO_ANSWER:
                OnlineController.OnlineUserLogicController.onForbiddenToAnswer();
                break;
            case MessageCodes.ALLOWED_TO_ANSWER:
                OnlineController.OnlineUserLogicController.onAllowedToAnswer();
                break;
            case MessageCodes.SENDING_QUESTION:
                QuestionMessage questionMessage = (QuestionMessage) message;
                int questionId = questionMessage.getQuestionId();
                String question = questionMessage.getQuestion();
                OnlineController.OnlineUserLogicController
                        .onReceivingQuestion(questionId, question);
                break;
            case MessageCodes.SENDING_INCORRECT_OPPONENT_ANSWER:
                String opponentAnswer = ((IncorrectOpponentAnswerMessage) message).getOpponentAnswer();
                OnlineController.OnlineUserLogicController
                        .onIncorrectOpponentAnswer(opponentAnswer);
                break;
            case MessageCodes.SENDING_CORRECT_ANSWER_AND_SCORE:
                CorrectAnswerAndScoreMessage answerMessage = (CorrectAnswerAndScoreMessage) message;
                String correctAnswer = answerMessage.getCorrectAnswer();
                String comment = answerMessage.getComment();
                int firstUserScore = answerMessage.getFirstUserScore();
                int secondUserScore = answerMessage.getSecondUserScore();
                String questionInfo = answerMessage.getQuestionMessage();
                OnlineController.OnlineUserLogicController.onReceivingAnswer(firstUserScore,
                        secondUserScore, correctAnswer, comment, questionInfo);
                break;
            case MessageCodes.OPPONENT_IS_ANSWERING:
                OnlineController.OnlineUserLogicController.onOpponentIsAnswering();
                break;
            case MessageCodes.TIME_START:
                OnlineController.OnlineUserLogicController.onTimeStart();
                break;
            case MessageCodes.FALSE_START:
                OnlineController.OnlineAdminLogicController.onFalseStart(senderId);
                break;
            case MessageCodes.HANDSHAKE:
                OnlineController.NetworkController.continueGame();
                break;
            case MessageCodes.TIME_LIMIT:
                int roundNumber = ((TimeLimitMessage) message).getRoundNumber();
                OnlineController.OnlineAdminLogicController.onTimeLimit(roundNumber, senderId);
                break;
            case MessageCodes.FINISH:
                String finishMessage = ((FinishMessage) message).getFinishMessage();
                OnlineController.NetworkController.finishImmediately(finishMessage);
                break;
            case MessageCodes.CORRECT_ANSWER:
                OnlineController.NetworkController.updateScore();
                break;
            case MessageCodes.READY_FOR_QUESTION:
                OnlineController.OnlineAdminLogicController.onReadyForQuestion(senderId);
                break;
        }
    }
}
