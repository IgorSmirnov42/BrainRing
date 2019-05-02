package ru.spbhse.brainring.logic;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ru.spbhse.brainring.network.messages.Message;

public class LocalGameAdminLogic {
    private UserScore green;
    private UserScore red;
    private String answeringUserId;
    private static final byte[] ALLOW_ANSWER;
    private static final byte[] FORBID_ANSWER;

    static {
        ALLOW_ANSWER = generateMessage(Message.ALLOWED_TO_ANSWER, "");
        FORBID_ANSWER = generateMessage(Message.FORBIDDEN_TO_ANSWER, "");
    }
    

    private UserScore getThisUser(String userId) {
        return green.status.participantId.equals(userId) ? green : red;
    }

    private UserScore getOtherUser(String userId) {
        return green.status.participantId.equals(userId) ? red : green;
    }

    public void onAnswerIsReady(String userId) {
        answeringUserId = userId;
        UserScore user = getThisUser(userId);
        if (user.status.alreadyAnswered) {
            //Controller.NetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
        } else {
            user.status.alreadyAnswered = true;
            //Controller.NetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
            //Controller.NetworkController.sendMessageToConcreteUser(
            //        getOtherUser(userId).status.participantId, OPPONENT_ANSWERING);
        }
    }

    public void addUsers(String green, String red) {
        this.green = new UserScore(green);
        this.red = new UserScore(red);
    }

    public void newQuestion() {
        green.status.onNewQuestion();
        red.status.onNewQuestion();
    }

    public String getRedScore() {
        return getScore(red);
    }

    public String getGreenScore() {
        return getScore(green);
    }

    private String getScore(UserScore user) {
        if (user == null) {
            return "?";
        }
        return String.valueOf(user.score);
    }

    public void plusPoint(int userNumber) {
        (userNumber == 1 ? green : red).score++;
    }

    public void minusPoint(int userNumber) {
        (userNumber == 1 ? green : red).score--;
    }

    private static byte[] generateMessage(int code, String message) {
        byte[] buf;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeInt(code);
            dout.writeChars(message);
            dout.flush();
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
