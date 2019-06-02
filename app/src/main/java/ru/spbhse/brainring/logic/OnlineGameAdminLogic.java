package ru.spbhse.brainring.logic;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageGenerator;
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
    private List<AnswerTime> waitingAnswer= new ArrayList<>();

    private static final byte[] ALLOW_ANSWER;
    private static final byte[] FORBID_ANSWER;
    private static final byte[] OPPONENT_ANSWERING;
    private static final byte[] TIME_START;
    private static final byte[] CORRECT_ANSWER;
    private static final byte[] FINISH;

    static {
        ALLOW_ANSWER = MessageGenerator.create().writeInt(Message.ALLOWED_TO_ANSWER).toByteArray();
        FORBID_ANSWER = MessageGenerator.create().writeInt(Message.FORBIDDEN_TO_ANSWER).toByteArray();
        TIME_START = MessageGenerator.create().writeInt(Message.TIME_START).toByteArray();
        CORRECT_ANSWER = MessageGenerator.create().writeInt(Message.CORRECT_ANSWER).toByteArray();
        OPPONENT_ANSWERING = MessageGenerator.create().writeInt(Message.OPPONENT_IS_ANSWERING).toByteArray();
        FINISH = MessageGenerator.create().writeInt(Message.FINISH).toByteArray();
    }

    private static final int QUESTIONS_NUMBER_MIN = 5;
    private static final int SECOND = 1000;
    private static final int TIME_TO_SHOW_ANSWER = 5;
    private static final int TIME_TO_READ_QUESTION = 10;
    private static final int DELIVERING_FAULT_MILLIS = 750;

    /** Returns UserScore object connected with given user */
    public OnlineGameAdminLogic() {
        user1 = new UserScore(OnlineController.NetworkController.getMyParticipantId());
        user2 = new UserScore(OnlineController.NetworkController.getOpponentParticipantId());
    }

    /** Returns UserScore object connected with given user */
    private UserScore getThisUser(@NonNull String userId) {
        return user1.status.participantId.equals(userId) ? user1 : user2;
    }

    /** Returns UserScore object connected with opponent of given user */
    private UserScore getOtherUser(@NonNull String userId) {
        return user1.status.participantId.equals(userId) ? user2 : user1;
    }

    public void onFalseStart(@NonNull String userId) {
        getThisUser(userId).status.alreadyAnswered = true;
    }

    private void allowAnswer(@NonNull String userId) {
        Log.d("BrainRing","Allow to answer " + userId);
        answeringUserId = userId;
        UserScore user = getThisUser(userId);
        user.status.alreadyAnswered = true;
        OnlineController.NetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
        OnlineController.NetworkController.sendMessageToConcreteUser(
                getOtherUser(userId).status.participantId, OPPONENT_ANSWERING);
    }

    private void forbidAnswer(@NonNull String userId) {
        Log.d("BrainRing","Allow to answer " + userId);
        OnlineController.NetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
    }

    public void onTimeLimit(int roundNumber, @NonNull String userId) {
        // If other is answering, then no effect
        if (roundNumber != currentRound) {
            return;
        }
        if (getOtherUser(userId).status.alreadyAnswered ||
                !userId.equals(OnlineController.NetworkController.getMyParticipantId())) {
            getThisUser(userId).status.alreadyAnswered = true;
            showAnswer(null);
        } else {
            new Handler().postDelayed(() -> {
                if (roundNumber == currentRound) {
                    getThisUser(userId).status.alreadyAnswered = true;
                    if (bothAnswered()) {
                        showAnswer(null);
                    }
                }
            }, DELIVERING_FAULT_MILLIS);
        }
    }

    /**
     * Allows or forbids to answer team that pushed answer button
     * Determines false starts
     */
    public void onAnswerIsReady(String userId, long time) {
        UserScore user = getThisUser(userId);
        if (user.status.alreadyAnswered || answeringUserId != null) {
            forbidAnswer(userId);
        } else {
            Log.d("BrainRing", "Received answer from user " + userId + " at " + time);
            currentRound = 2;
            // If other user has already answered or not admin wants and current don't then allow immediately
            if (getOtherUser(userId).status.alreadyAnswered ||
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
        Log.d("BrainRing", "Start judging");
        if (waitingAnswer.isEmpty()) {
            Log.wtf("BrainRing", "No one wants to answer, judgeFirst called");
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
                getOtherUser(previousUserId).status.participantId,
                MessageGenerator.create().
                        writeInt(Message.SENDING_INCORRECT_OPPONENT_ANSWER)
                        .writeString(previousAnswer)
                        .toByteArray());
    }

    /** Rejects or accepts answer written by user */
    public void onAnswerIsWritten(@NonNull String writtenAnswer, @NonNull String id) {
        Log.d("BrainRing","Got answer: " + writtenAnswer + " from user " + id);
        if (!id.equals(answeringUserId)) {
            return;
        }
        String userId = answeringUserId;
        answeringUserId = null;
        if (currentQuestion.checkAnswer(writtenAnswer)) {
            OnlineController.NetworkController.sendMessageToConcreteUser(id, CORRECT_ANSWER);
        }
        if (!currentQuestion.checkAnswer(writtenAnswer)) {
            if (!getOtherUser(userId).status.alreadyAnswered) {
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
        Log.d("BrainRing", "show answer " + answeredUserId);
        String questionMessage;
        if (answeredUserId == null) {
            questionMessage = "Никто не ответил на вопрос";
        } else {
            questionMessage = OnlineController.NetworkController.getParticipantName(answeredUserId) +
                    " ответил верно";
        }
        Log.d("BrainRing", "Question message:" + questionMessage);
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
        new Handler().postDelayed(this::newQuestion, TIME_TO_SHOW_ANSWER * SECOND);
    }

    /** Determines if game is finished. If not, generates new question and sends it */
    public void newQuestion() {
        if (questionNumber >= QUESTIONS_NUMBER_MIN && user1.score != user2.score) {
            OnlineController.NetworkController.sendMessageToAll(FINISH);
            OnlineController.finishOnlineGame();
            return;
        }

        Log.d("BrainRing", "New question");
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
        new Handler().postDelayed(this::publishQuestion, TIME_TO_READ_QUESTION * SECOND);
    }

    private boolean bothAnswered() {
        return user1.status.alreadyAnswered && user2.status.alreadyAnswered;
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
