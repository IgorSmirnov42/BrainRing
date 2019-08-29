package ru.spbhse.brainring.messageProcessing;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.managers.OnlineGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerReadyMessage;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerWrittenMessage;
import ru.spbhse.brainring.network.messages.messageTypes.CorrectAnswerAndScoreMessage;
import ru.spbhse.brainring.network.messages.messageTypes.FinishMessage;
import ru.spbhse.brainring.network.messages.messageTypes.IncorrectOpponentAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.QuestionMessage;
import ru.spbhse.brainring.network.messages.messageTypes.TimeLimitMessage;

public class OnlineMessageProcessor {
    private OnlineGameManager manager;

    public OnlineMessageProcessor(@NonNull OnlineGameManager manager) {
        this.manager = manager;
    }

    public void process(@NonNull Message message, @NonNull String senderId) {
        switch (message.getMessageCode()) {
            case MessageCodes.ANSWER_IS_READY:
                long time = ((AnswerReadyMessage) message).getTime();
                manager.getAdminLogic().onAnswerIsReady(senderId, time);
                break;
            case MessageCodes.ANSWER_IS_WRITTEN:
                String answer = ((AnswerWrittenMessage) message).getAnswer();
                manager.getAdminLogic().onAnswerIsWritten(answer, senderId);
                break;
            case MessageCodes.FORBIDDEN_TO_ANSWER:
                manager.getUserLogic().onForbiddenToAnswer();
                break;
            case MessageCodes.ALLOWED_TO_ANSWER:
                manager.getUserLogic().onAllowedToAnswer();
                break;
            case MessageCodes.SENDING_QUESTION:
                QuestionMessage questionMessage = (QuestionMessage) message;
                int questionId = questionMessage.getQuestionId();
                String question = questionMessage.getQuestion();
                manager.getUserLogic().onReceivingQuestion(questionId, question);
                break;
            case MessageCodes.SENDING_INCORRECT_OPPONENT_ANSWER:
                String opponentAnswer = ((IncorrectOpponentAnswerMessage) message).getOpponentAnswer();
                manager.getUserLogic().onIncorrectOpponentAnswer(opponentAnswer);
                break;
            case MessageCodes.SENDING_CORRECT_ANSWER_AND_SCORE:
                CorrectAnswerAndScoreMessage answerMessage = (CorrectAnswerAndScoreMessage) message;
                String correctAnswer = answerMessage.getCorrectAnswer();
                String comment = answerMessage.getComment();
                int firstUserScore = answerMessage.getFirstUserScore();
                int secondUserScore = answerMessage.getSecondUserScore();
                String questionInfo = answerMessage.getQuestionMessage();
                manager.getUserLogic().onReceivingAnswer(firstUserScore,
                        secondUserScore, correctAnswer, comment, questionInfo);
                break;
            case MessageCodes.OPPONENT_IS_ANSWERING:
                manager.getUserLogic().onOpponentIsAnswering();
                break;
            case MessageCodes.TIME_START:
                manager.getUserLogic().onTimeStart();
                break;
            case MessageCodes.FALSE_START:
                manager.getAdminLogic().onFalseStart(senderId);
                break;
            case MessageCodes.HANDSHAKE:
                manager.getNetwork().continueGame();
                break;
            case MessageCodes.TIME_LIMIT:
                int roundNumber = ((TimeLimitMessage) message).getRoundNumber();
                manager.getAdminLogic().onTimeLimit(roundNumber, senderId);
                break;
            case MessageCodes.FINISH:
                String finishMessage = ((FinishMessage) message)
                        .getFinishMessage(manager.getActivity());
                manager.getNetwork().finishImmediately(finishMessage);
                break;
            case MessageCodes.CORRECT_ANSWER:
                manager.getNetwork().updateScore();
                break;
            case MessageCodes.READY_FOR_QUESTION:
                manager.getAdminLogic().onReadyForQuestion(senderId);
                break;
        }
    }
}
