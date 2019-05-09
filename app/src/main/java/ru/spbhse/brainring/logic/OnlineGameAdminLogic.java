package ru.spbhse.brainring.logic;

import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.utils.Question;

/** Realizes admin's logic in online mode */
public class OnlineGameAdminLogic {
    private UserScore user1;
    private UserScore user2;
    private Question currentQuestion;
    private String answeringUserId;
    private boolean interrupted;
    private long currentRound;
    private List<AnswerTime> waitingAnswer= new ArrayList<>();

    private static final byte[] ALLOW_ANSWER = Message.generateMessage(Message.ALLOWED_TO_ANSWER, "");
    private static final byte[] FORBID_ANSWER = Message.generateMessage(Message.FORBIDDEN_TO_ANSWER, "");
    private static final byte[] OPPONENT_ANSWERING = Message.generateMessage(Message.OPPONENT_IS_ANSWERING, "");
    private static final byte[] TIME_START = Message.generateMessage(Message.TIME_START, "");

    private static final int WINNER_SCORE = 5;
    private static final int SECOND = 1000;
    private static final int TIME_TO_SHOW_ANSWER = 5;
    private static final int TIME_TO_READ_QUESTION = 10;
    private static final int DELIVERING_FAULT_MILLIS = 500;

    /** Returns UserScore object connected with given user */
    public OnlineGameAdminLogic() {
        user1 = new UserScore(OnlineController.NetworkController.getMyParticipantId());
        user2 = new UserScore(OnlineController.NetworkController.getOpponentParticipantId());
    }

    /** Returns UserScore object connected with given user */
    private UserScore getThisUser(String userId) {
        return user1.status.participantId.equals(userId) ? user1 : user2;
    }

    /** Returns UserScore object connected with opponent of given user */
    private UserScore getOtherUser(String userId) {
        return user1.status.participantId.equals(userId) ? user2 : user1;
    }

    public void onFalseStart(String userId) {
        getThisUser(userId).status.alreadyAnswered = true;
    }

    private void allowAnswer(String userId) {
        Log.d("BrainRing","Allow to answer " + userId);
        answeringUserId = userId;
        UserScore user = getThisUser(userId);
        user.status.alreadyAnswered = true;
        OnlineController.NetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
        OnlineController.NetworkController.sendMessageToConcreteUser(
                getOtherUser(userId).status.participantId, OPPONENT_ANSWERING);
    }

    private void forbidAnswer(String userId) {
        Log.d("BrainRing","Allow to answer " + userId);
        OnlineController.NetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
    }

    public void onTimeLimit(long roundNumber, String userId) {
        // If other is answering, then no effect
        if (roundNumber != currentRound) {
            return;
        }
        if (getOtherUser(userId).status.alreadyAnswered ||
                !userId.equals(OnlineController.NetworkController.getMyParticipantId())) {
            getThisUser(userId).status.alreadyAnswered = true;
            showAnswer();
        } else {
            new Handler().postDelayed(() -> {
                if (roundNumber == currentRound) {
                    getThisUser(userId).status.alreadyAnswered = true;
                    if (bothAnswered()) {
                        showAnswer();
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

    private void restartTime(String previousUserId, String previousAnswer) {
        if (bothAnswered()) {
            showAnswer();
            return;
        }
        OnlineController.NetworkController.sendMessageToConcreteUser(
                getOtherUser(previousUserId).status.participantId,
                Message.generateMessage(Message.SENDING_INCORRECT_OPPONENT_ANSWER, previousAnswer));
    }

    /** Rejects or accepts answer written by user */
    public void onAnswerIsWritten(String writtenAnswer, String id) {
        Log.d("BrainRing","Got answer: " + writtenAnswer + " from user " + id);
        if (!id.equals(answeringUserId)) {
            return;
        }
        String userId = answeringUserId;
        answeringUserId = null;
        if (!currentQuestion.checkAnswer(writtenAnswer)) {
            if (!getOtherUser(userId).status.alreadyAnswered) {
                restartTime(userId, writtenAnswer);
                return;
            }
        } else {
            ++getThisUser(userId).score;
        }

        showAnswer();
    }

    /** Sends answer and shows it for {@code TIME_TO_SHOW_ANSWER} seconds */
    private void showAnswer() {
        OnlineController.NetworkController.sendMessageToAll(generateAnswer());
        new Handler().postDelayed(this::newQuestion, TIME_TO_SHOW_ANSWER * SECOND);
    }

    /** Determines if game is finished. If not, generates new question and sends it */
    public void newQuestion() {
        if (user1.score >= WINNER_SCORE || user2.score >= WINNER_SCORE) {
            OnlineController.NetworkController.sendMessageToAll(
                    Message.generateMessage(Message.FINISH, ""));
            OnlineController.finishOnlineGame();
            return;
        }

        Log.d("BrainRing", "New question");
        user1.status.onNewQuestion();
        user2.status.onNewQuestion();

        currentQuestion = DatabaseController.getRandomQuestion();
        byte[] message = Message.generateMessage(Message.SENDING_QUESTION, currentQuestion.getQuestion());
        OnlineController.NetworkController.sendQuestion(message);
        currentRound = 1;
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
            showAnswer();
        }
    }

    public void finishGame() {
        interrupted = true;
    }

    private byte[] generateAnswer() {
        String answer = currentQuestion.getAllAnswers();
        byte[] buf;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeInt(Message.SENDING_CORRECT_ANSWER_AND_SCORE);
            dout.writeInt(user1.score);
            dout.writeInt(user2.score);
            dout.writeChars(answer);
            buf = bout.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            buf = null;
        }
        return buf;
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
