package ru.spbhse.brainring.logic;

import android.os.Handler;

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
    private String answeringUserId;
    private static final byte[] ALLOW_ANSWER;
    private static final byte[] FORBID_ANSWER;
    private static final byte[] OPPONENT_ANSWERING;

    static {
        ALLOW_ANSWER = Message.generateMessage(Message.ALLOWED_TO_ANSWER, "");
        FORBID_ANSWER = Message.generateMessage(Message.FORBIDDEN_TO_ANSWER, "");
        OPPONENT_ANSWERING = Message.generateMessage(Message.OPPONENT_IS_ANSWERING, "");
    }

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
    public void onAnswerIsReady(String userId) {
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
    public void onAnswerIsWritten(String writtenAnswer) {
        // TODO: проверять idшник
        System.out.println("GOT ANSWER:");
        System.out.println(writtenAnswer);
        String userId = answeringUserId;
        answeringUserId = null;
        if (!currentQuestion.checkAnswer(writtenAnswer)) {
            if (!getOtherUser(userId).status.alreadyAnswered) {
                Controller.NetworkController.sendMessageToConcreteUser(
                        getOtherUser(userId).status.participantId,
                        Message.generateMessage(Message.SENDING_INCORRECT_OPPONENT_ANSWER, writtenAnswer));
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
        if (user1.score >= 5 || user2.score >= 5) {
            Controller.finishOnlineGame();
            return;
        }

        System.out.println("ЗАДАЮ ВОПРОС");
        user1.status.onNewQuestion();
        user2.status.onNewQuestion();

        currentQuestion = Controller.DatabaseController.getRandomQuestion();
        Controller.NetworkController.sendMessageToAll(
                Message.generateMessage(Message.SENDING_QUESTION, currentQuestion.getQuestion()));
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
