package ru.spbhse.brainring.logic;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.OnlineFinishCodes;
import ru.spbhse.brainring.utils.Question;

/** Realizes admin's logic in online mode */
public class OnlineGameAdminLogic {
    /** User on this device */
    private UserScore user1;
    /** User on other device */
    private UserScore user2;
    private Question currentQuestion;
    private String answeringUserId;
    /** Flag to stop sending messages if game was finished */
    private boolean interrupted;
    /**
     * 1 or 2. Before first answering is 1 round, after incorrect answer is 2 round
     * (see countdowns in user logic)
     */
    private int currentRound;
    /** Question number in a game */
    private int questionNumber;
    /** Flag do determine whether allowance to push button was sent to users */
    private boolean published;
    /** Number of users who are ready to continue game */
    private int readyUsers;

    /** Users that pushed a button and now in queue to determine who was first */
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

    /**
     * Minimal number of rounds in a game. It can be bigger if after {@code QUESTION_NUMBER_MIN}
     * rounds winner is not determined
     */
    private static final int QUESTIONS_NUMBER_MIN = 5;
    private static final int SECOND = 1000;
    /** Time between sending question and allowance to push a button */
    private static final int TIME_TO_READ_QUESTION = 10;
    /** Time we believe to be a maximal time to send a message from one user to another */
    private static final int DELIVERING_FAULT_MILLIS = 1000;
    /**
     * Gap between sending message about game results to opponent and to itself
     * Must be big enough to send a message and to get a result
     */
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

    /** Reacts on one's false start. Can be called even after publishing */
    public void onFalseStart(@NonNull String userId) {
        if (!published) {
            getThisUser(userId).status.setAlreadyAnswered(true);
        } else {
            if (answeringUserId != null) { // somebody is answering
                getThisUser(userId).status.setAlreadyAnswered(true);
            } else {
                if (getOtherUser(userId).status.getAlreadyAnswered()) {
                    showAnswer(null);
                } else {
                    getThisUser(userId).status.setAlreadyAnswered(true);
                }
            }
        }
    }

    /** Allows user to answer */
    private void allowAnswer(@NonNull String userId) {
        Log.d(Controller.APP_TAG,"Allow to answer " + userId);
        answeringUserId = userId;
        UserScore user = getThisUser(userId);
        user.status.setAlreadyAnswered(true);
        OnlineController.NetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
        OnlineController.NetworkController.sendMessageToConcreteUser(
                getOtherUser(userId).status.getParticipantId(), OPPONENT_ANSWERING);
    }

    /** Forbids user to answer */
    private void forbidAnswer(@NonNull String userId) {
        Log.d(Controller.APP_TAG,"Allow to answer " + userId);
        OnlineController.NetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
    }

    /**
     * Gets message from user that he/she didn't push a button at time.
     * It is not equal incorrect answer because if opponent pushed a button then this user will have
     * second countdown
     */
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

    /** Determines who of users from {@code waitingAnswer} was first and allows him/her to answer */
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

    /**
     * Restarts time or shows answer after incorrect answer depending on if there is a user
     * who haven't answered
     */
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
            questionMessage = OnlineController.getOnlineGameActivity()
                    .getString(R.string.nobody_answered);
        } else {
            questionMessage = OnlineController.NetworkController.getParticipantName(answeredUserId) +
                    OnlineController.getOnlineGameActivity().getString(R.string.answered_right);
        }
        Log.d(Controller.APP_TAG, "Question message: " + questionMessage);
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

    /** Sends results of the finished game to users */
    private void sendGameResults() {
        Log.d(Controller.APP_TAG, "Game finished");
        int user1Code, user2Code;
        if (user1.score > user2.score) {
            user1Code = OnlineFinishCodes.GAME_FINISHED_CORRECTLY_WON;
            user2Code = OnlineFinishCodes.GAME_FINISHED_CORRECTLY_LOST;
        } else {
            user2Code = OnlineFinishCodes.GAME_FINISHED_CORRECTLY_WON;
            user1Code = OnlineFinishCodes.GAME_FINISHED_CORRECTLY_LOST;
        }
        // The order of sending here is important!
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
    }

    /** Determines if game is finished. If not, generates new question and sends it */
    public void newQuestion() {
        if (questionNumber >= QUESTIONS_NUMBER_MIN && user1.score != user2.score) {
            sendGameResults();
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

    /** Counts {@code TIME_TO_READ_QUESTION} seconds and sends signal allowing pushing a button */
    public void publishing() {
        published = false;
        new Handler().postDelayed(this::publishQuestion, TIME_TO_READ_QUESTION * SECOND);
    }

    /** Checks whether both players cannot answer question more */
    private boolean bothAnswered() {
        return user1.status.getAlreadyAnswered() && user2.status.getAlreadyAnswered();
    }

    /** Checks whether both players had false start. If no sends signal allowing to push a button */
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

    /** Marks user as ready for next question */
    public void onReadyForQuestion(@NonNull String userId) {
        ++readyUsers;
        if (readyUsers == 2) {
            newQuestion();
        }
    }

    /** Blocks all future message sendings */
    public void finishGame() {
        interrupted = true;
    }

    /** Class to store information about user score and user status in a game */
    private static class UserScore {
        private int score;
        private UserStatus status;

        private UserScore(String userId) {
            status = new UserStatus(userId);
        }
    }

    /** Class for storing pairs (time, userId) of users' answer times */
    private static class AnswerTime {
        private long time;
        private String userId;

        private AnswerTime(long time, String userId) {
            this.time = time;
            this.userId = userId;
        }
    }
}
