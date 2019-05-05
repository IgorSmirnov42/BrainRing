package ru.spbhse.brainring.logic;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.utils.Question;

/** Realizes admin's logic in online mode */
public class OnlineGameAdminLogic {
    private UserScore user1;
    private UserScore user2;
    private Question currentQuestion;
    private volatile String answeringUserId;
    private static final byte[] ALLOW_ANSWER = Message.generateMessage(Message.ALLOWED_TO_ANSWER, "");
    private static final byte[] FORBID_ANSWER = Message.generateMessage(Message.FORBIDDEN_TO_ANSWER, "");
    private static final byte[] OPPONENT_ANSWERING = Message.generateMessage(Message.OPPONENT_IS_ANSWERING, "");

    private static final int WINNER_SCORE = 5;
    private static final int FIRST_COUNTDOWN = 20;
    private static final int SECOND_COUNTDOWN = 20;
    private static final int SENDING_COUNTDOWN = 5;
    private static final int SECOND = 1000;
    private final CountDownTimer firstGameTimer = new CountDownTimer(FIRST_COUNTDOWN * SECOND,
            SECOND) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("BrainRing", "Tick first timer");
            if (millisUntilFinished <= SENDING_COUNTDOWN * SECOND) {
                Controller.NetworkController.sendMessageToAll(
                        Message.generateMessage(Message.TICK,
                                String.valueOf(millisUntilFinished / SECOND)));
            }
        }

        @Override
        public void onFinish() {
            Log.d("BrainRing", "Finish first timer");
            synchronized (OnlineGameAdminLogic.this) {
                if (answeringUserId == null) {
                    showAnswer();
                }
            }
        }
    };
    private final CountDownTimer secondGameTimer = new CountDownTimer(SECOND_COUNTDOWN * SECOND,
            SECOND) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("BrainRing", "Tick second timer");
            if (millisUntilFinished <= SENDING_COUNTDOWN * SECOND) {
                Controller.NetworkController.sendMessageToAll(
                        Message.generateMessage(Message.TICK,
                                String.valueOf(millisUntilFinished / SECOND)));
            }
        }

        @Override
        public void onFinish() {
            Log.d("BrainRing", "Finish second timer");
            synchronized (OnlineGameAdminLogic.this) {
                if (answeringUserId == null) {
                    showAnswer();
                }
            }
        }
    };
    private CountDownTimer timer;

    /** Returns UserScore object connected with given user */
    public OnlineGameAdminLogic() {
        user1 = new UserScore(Controller.NetworkController.getMyParticipantId());
        user2 = new UserScore(Controller.NetworkController.getOpponentParticipantId());
    }

    /** Returns UserScore object connected with given user */
    private UserScore getThisUser(String userId) {
        return user1.status.participantId.equals(userId) ? user1 : user2;
    }

    /** Returns UserScore object connected with opponent of given user */
    private UserScore getOtherUser(String userId) {
        return user1.status.participantId.equals(userId) ? user2 : user1;
    }

    /**
     * Allows or forbids to answer team that pushed answer button
     * Determines (no) false starts
     */
    public synchronized void onAnswerIsReady(String userId) {
        timer.cancel();
        UserScore user = getThisUser(userId);
        if (user.status.alreadyAnswered || answeringUserId != null) {
            Controller.NetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
        } else {
            answeringUserId = userId;
            user.status.alreadyAnswered = true;
            Controller.NetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
            Controller.NetworkController.sendMessageToConcreteUser(
                    getOtherUser(userId).status.participantId, OPPONENT_ANSWERING);
        }
    }

    /** Rejects or accepts answer written by user */
    public synchronized void onAnswerIsWritten(String writtenAnswer, String id) {
        Log.d("BrainRing","GOT ANSWER: " + writtenAnswer + " from user " + id);
        if (!id.equals(answeringUserId)) {
            return;
        }
        String userId = answeringUserId;
        answeringUserId = null;
        if (!currentQuestion.checkAnswer(writtenAnswer)) {
            if (!getOtherUser(userId).status.alreadyAnswered) {
                Controller.NetworkController.sendMessageToConcreteUser(
                        getOtherUser(userId).status.participantId,
                        Message.generateMessage(Message.SENDING_INCORRECT_OPPONENT_ANSWER, writtenAnswer));
                timer = secondGameTimer;
                timer.start();
                return;
            }
        } else {
            ++getThisUser(userId).score;
        }

        showAnswer();
    }

    /** Sends answer and shows it for 5 seconds */
    private void showAnswer() {
        Controller.NetworkController.sendMessageToAll(generateAnswer());
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                newQuestion();
            }
        }, 5000);
    }

    /** Determines if game is finished. If not, generates new question and sends it */
    public void newQuestion() {
        if (user1.score >= WINNER_SCORE || user2.score >= WINNER_SCORE) {
            Controller.finishOnlineGame();
            return;
        }

        Log.d("BrainRing", "New question");
        user1.status.onNewQuestion();
        user2.status.onNewQuestion();

        currentQuestion = Controller.DatabaseController.getRandomQuestion();
        Controller.NetworkController.sendMessageToAll(
                Message.generateMessage(Message.SENDING_QUESTION, currentQuestion.getQuestion()));
        timer = firstGameTimer;
        timer.start();
    }

    public void finishGame() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
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
}
