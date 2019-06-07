package ru.spbhse.brainring.logic;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.OnlineFinishCodes;
import ru.spbhse.brainring.utils.Question;

/** Realizes admin's logic in online mode */
public class OnlineGameAdminLogic {
    private UserScore user1;
    private UserScore user2;
    private Question currentQuestion;
    private String answeringUserId;
    private boolean interrupted;
    private int currentRound;
    private int questionNumber;
    private boolean published;
    private int readyUsers;
    private List<AnswerTime> waitingAnswer= new ArrayList<>();

    private static final byte[] ALLOW_ANSWER;
    private static final byte[] FORBID_ANSWER;
    private static final byte[] OPPONENT_ANSWERING;
    private static final byte[] TIME_START;
    private static final byte[] CORRECT_ANSWER;

    static {
        ALLOW_ANSWER = MessageGenerator.create()
                .writeInt(Message.ALLOWED_TO_ANSWER)
                .toByteArray();
        FORBID_ANSWER = MessageGenerator.create()
                .writeInt(Message.FORBIDDEN_TO_ANSWER)
                .toByteArray();
        TIME_START = MessageGenerator.create()
                .writeInt(Message.TIME_START)
                .toByteArray();
        CORRECT_ANSWER = MessageGenerator.create()
                .writeInt(Message.CORRECT_ANSWER)
                .toByteArray();
        OPPONENT_ANSWERING = MessageGenerator.create()
                .writeInt(Message.OPPONENT_IS_ANSWERING)
                .toByteArray();
    }

    private static final int QUESTIONS_NUMBER_MIN = 5;
    private static final int SECOND = 1000;
    private static final int TIME_TO_READ_QUESTION = 10;
    private static final int DELIVERING_FAULT_MILLIS = 1000;
    private static final int TIME_TO_SEND = 2000;

    /** Returns UserScore object connected with given user */
    public OnlineGameAdminLogic() {
        user1 = new UserScore(OnlineController.NetworkController.getMyParticipantId());
        user2 = new UserScore(OnlineController.NetworkController.getOpponentParticipantId());
    }

    /** Returns UserScore object connected with given user */
    private UserScore getThisUser(@NonNull String userId) {
        return user1.status.getParticipantId().equals(userId) ? user1 : user2;
    }

    /** Returns UserScore object connected with opponent of given user */
    private UserScore getOtherUser(@NonNull String userId) {
        return user1.status.getParticipantId().equals(userId) ? user2 : user1;
    }

    public void onFalseStart(@NonNull String userId) {
        if (!published) {
            getThisUser(userId).status.setAlreadyAnswered(true);
        } else {
            if (answeringUserId != null) { // somebody is answering
                getThisUser(userId).status.setAlreadyAnswered(true);
            } else {
                if (getOtherUser(userId).status.getAlreadyAnswered()) {
                    showAnswer(null);
                }
            }
        }
    }

    private void allowAnswer(@NonNull String userId) {
        Log.d(Controller.APP_TAG,"Allow to answer " + userId);
        answeringUserId = userId;
        UserScore user = getThisUser(userId);
        user.status.setAlreadyAnswered(true);
        OnlineController.NetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
        OnlineController.NetworkController.sendMessageToConcreteUser(
                getOtherUser(userId).status.getParticipantId(), OPPONENT_ANSWERING);
    }

    private void forbidAnswer(@NonNull String userId) {
        Log.d(Controller.APP_TAG,"Allow to answer " + userId);
        OnlineController.NetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
    }

    public void onTimeLimit(int roundNumber, @NonNull String userId) {
        // If other is answering, then no effect
        if (roundNumber != currentRound) {
            return;
        }
        if (getOtherUser(userId).status.getAlreadyAnswered() ||
                !userId.equals(OnlineController.NetworkController.getMyParticipantId())) {
            getThisUser(userId).status.setAlreadyAnswered(true);
            showAnswer(null);
        } else {
            new Handler().postDelayed(() -> {
                if (roundNumber == currentRound) {
                    getThisUser(userId).status.setAlreadyAnswered(true);
                }
            }, DELIVERING_FAULT_MILLIS);
        }
    }

    /**
     * Allows or forbids to answer team that pushed answer button
     * Determines false starts
     */
    public void onAnswerIsReady(@NonNull String userId, long time) {
        UserScore user = getThisUser(userId);
        if (user.status.getAlreadyAnswered() || answeringUserId != null) {
            forbidAnswer(userId);
        } else {
            Log.d(Controller.APP_TAG, "Received answer from user " + userId + " at " + time);
            currentRound = 2;
            // If other user has already answered or not admin wants and current don't
            // then allow immediately
            if (getOtherUser(userId).status.getAlreadyAnswered() ||
                    (!userId.equals(OnlineController.NetworkController.getMyParticipantId())
                            && waitingAnswer.isEmpty())) {
                allowAnswer(userId);
            } else {
                waitingAnswer.add(new AnswerTime(time, userId));
                if (waitingAnswer.size() == 1) {
                    new Handler().postDelayed(this::judgeFirst, DELIVERING_FAULT_MILLIS);
                }
            }
        }
    }

    private void judgeFirst() {
        Log.d(Controller.APP_TAG, "Start judging");
        if (waitingAnswer.isEmpty()) {
            Log.wtf(Controller.APP_TAG, "No one wants to answer, judgeFirst called");
            return;
        }
        AnswerTime best = waitingAnswer.get(0);
        for (AnswerTime player : waitingAnswer) {
            if (best.time > player.time) {
                best = player;
            }
        }
        allowAnswer(best.userId);
        for (AnswerTime player : waitingAnswer) {
            if (!best.userId.equals(player.userId)) {
                forbidAnswer(player.userId);
                break;
            }
        }
        waitingAnswer.clear();
    }

    private void restartTime(@NonNull String previousUserId, @NonNull String previousAnswer) {
        if (bothAnswered()) {
            showAnswer(null);
            return;
        }
        OnlineController.NetworkController.sendMessageToConcreteUser(
                getOtherUser(previousUserId).status.getParticipantId(),
                MessageGenerator.create()
                        .writeInt(Message.SENDING_INCORRECT_OPPONENT_ANSWER)
                        .writeString(previousAnswer)
                        .toByteArray()
        );
    }

    /** Rejects or accepts answer written by user */
    public void onAnswerIsWritten(@NonNull String writtenAnswer, @NonNull String id) {
        Log.d(Controller.APP_TAG,"Got answer: " + writtenAnswer + " from user " + id);
        if (!id.equals(answeringUserId)) {
            return;
        }
        String userId = answeringUserId;
        answeringUserId = null;
        if (currentQuestion.checkAnswer(writtenAnswer)) {
            OnlineController.NetworkController.sendMessageToConcreteUser(id, CORRECT_ANSWER);
        }
        if (!currentQuestion.checkAnswer(writtenAnswer)) {
            if (!getOtherUser(userId).status.getAlreadyAnswered()) {
                restartTime(userId, writtenAnswer);
            } else {
                showAnswer(null);
            }
        } else {
            ++getThisUser(userId).score;
            showAnswer(userId);
        }
    }

    /** Sends answer and shows it for {@code TIME_TO_SHOW_ANSWER} seconds */
    private void showAnswer(@Nullable String answeredUserId) {
        Log.d(Controller.APP_TAG, "show answer " + answeredUserId);
        String questionMessage;
        if (answeredUserId == null) {
            questionMessage = "Никто не ответил на вопрос";
        } else {
            questionMessage = OnlineController.NetworkController.getParticipantName(answeredUserId) +
                    " ответил верно";
        }
        Log.d(Controller.APP_TAG, "Question message:" + questionMessage);
        readyUsers = 0;
        OnlineController.NetworkController.sendMessageToAll(
                MessageGenerator.create()
                        .writeInt(Message.SENDING_CORRECT_ANSWER_AND_SCORE)
                        .writeString(currentQuestion.getAllAnswers())
                        .writeString(currentQuestion.getComment())
                        .writeInt(user1.score)
                        .writeInt(user2.score)
                        .writeString(questionMessage)
                        .toByteArray()
        );
    }

    /** Determines if game is finished. If not, generates new question and sends it */
    public void newQuestion() {
        if (questionNumber >= QUESTIONS_NUMBER_MIN && user1.score != user2.score) {
            Log.d(Controller.APP_TAG, "Game finished");
            int user1Code, user2Code;
            if (user1.score > user2.score) {
                user1Code = OnlineFinishCodes.GAME_FINISHED_CORRECTLY_WON;
                user2Code = OnlineFinishCodes.GAME_FINISHED_CORRECTLY_LOST;
            } else {
                user2Code = OnlineFinishCodes.GAME_FINISHED_CORRECTLY_WON;
                user1Code = OnlineFinishCodes.GAME_FINISHED_CORRECTLY_LOST;
            }
            // The order of sending here is critical!
            OnlineController.NetworkController.sendMessageToConcreteUser(
                    user2.status.getParticipantId(),
                    MessageGenerator.create()
                            .writeInt(Message.FINISH)
                            .writeInt(user2Code)
                            .toByteArray()
            );
            new Handler().postDelayed(() -> {
                OnlineController.NetworkController.sendMessageToConcreteUser(
                        user1.status.getParticipantId(),
                        MessageGenerator.create()
                                .writeInt(Message.FINISH)
                                .writeInt(user1Code)
                                .toByteArray()
                );
            }, TIME_TO_SEND);
            return;
        }

        Log.d(Controller.APP_TAG, "New question");
        user1.status.onNewQuestion();
        user2.status.onNewQuestion();

        currentQuestion = DatabaseController.getRandomQuestion();
        byte[] message = MessageGenerator.create()
                .writeInt(Message.SENDING_QUESTION)
                .writeInt(currentQuestion.getId())
                .writeString(currentQuestion.getQuestion())
                .toByteArray();
        OnlineController.NetworkController.sendQuestion(message);
        currentRound = 1;
        ++questionNumber;
    }

    public void publishing() {
        published = false;
        new Handler().postDelayed(this::publishQuestion, TIME_TO_READ_QUESTION * SECOND);
    }

    private boolean bothAnswered() {
        return user1.status.getAlreadyAnswered() && user2.status.getAlreadyAnswered();
    }

    private void publishQuestion() {
        if (interrupted) {
            return;
        }
        if (!bothAnswered()) {
            OnlineController.NetworkController.sendMessageToAll(TIME_START);
        } else {
            showAnswer(null);
        }
        published = true;
    }

    public void onReadyForQuestion(@NonNull String userId) {
        ++readyUsers;
        if (readyUsers == 2) {
            newQuestion();
        }
    }

    public void finishGame() {
        interrupted = true;
    }

    private static class UserScore {
        private int score;
        private UserStatus status;

        private UserScore(String userId) {
            status = new UserStatus(userId);
        }
    }

    private static class AnswerTime {
        private long time;
        private String userId;

        private AnswerTime(long time, String userId) {
            this.time = time;
            this.userId = userId;
        }
    }
}
