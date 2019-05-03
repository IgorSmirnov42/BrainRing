package ru.spbhse.brainring.logic;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.ui.LocalGameLocation;

/**
 * Class realizing admin's logic (counting time, switching locations etc)
 *      in local network mode
 */
public class LocalGameAdminLogic {
    private LocalGameLocation location = LocalGameLocation.GAME_WAITING_START;
    private UserScore green;
    private UserScore red;
    private String answeringUserId;
    private static final byte[] ALLOW_ANSWER;
    private static final byte[] FORBID_ANSWER;

    static {
        ALLOW_ANSWER = Message.generateMessage(Message.ALLOWED_TO_ANSWER, "");
        FORBID_ANSWER = Message.generateMessage(Message.FORBIDDEN_TO_ANSWER, "");
    }

    /** Called when jury accepts answer */
    public void onAcceptAnswer() {
        getThisUser(answeringUserId).score++;
        answeringUserId = null;
        location = LocalGameLocation.NOT_STARTED;
        Controller.LocalNetworkAdminUIController.setLocation(location);
    }

    /** Called when jury rejects answer */
    public void onRejectAnswer() {
        answeringUserId = null;
        location = LocalGameLocation.COUNTDOWN;
        Controller.LocalNetworkAdminUIController.setLocation(location);
    }

    /**
     * Called when jury pushes button to switch location
     * @return true if location was switched
     */
    public boolean toNextState() {
        // Если игра не началась, кнопки неактивны
        if (location == LocalGameLocation.GAME_WAITING_START) {
            return false;
        }
        // Во время принятия ответа нельзя переключиться
        if (location == LocalGameLocation.ONE_IS_ANSWERING) {
            return false;
        }
        // Переключение на чтение вопроса
        if (location == LocalGameLocation.NOT_STARTED) {
            newQuestion();
            location = LocalGameLocation.READING_QUESTION;
            Controller.LocalNetworkAdminUIController.setLocation(location);
            return true;
        }
        // Переклчение на таймер
        if (location == LocalGameLocation.READING_QUESTION) {
            location = LocalGameLocation.COUNTDOWN;
            Controller.LocalNetworkAdminUIController.setLocation(location);
            return true;
        }
        // Начало нового раунда
        if (location == LocalGameLocation.COUNTDOWN) {
            location = LocalGameLocation.NOT_STARTED;
            Controller.LocalNetworkAdminUIController.setLocation(location);
            return true;
        }
        return false;
    }

    /** Returns UserScore object connected with given user */
    private UserScore getThisUser(String userId) {
        return green.status.participantId.equals(userId) ? green : red;
    }

    /** Returns UserScore object connected with opponent of given user */
    private UserScore getOtherUser(String userId) {
        return green.status.participantId.equals(userId) ? red : green;
    }

    /**
     * Allows or forbids to answer team that pushed answer button
     * Determines (no) false starts
     */
    public void onAnswerIsReady(String userId) {
        UserScore user = getThisUser(userId);
        if (location == LocalGameLocation.READING_QUESTION) {
            user.status.alreadyAnswered = true;
        }
        if (user.status.alreadyAnswered || location != LocalGameLocation.COUNTDOWN) {
            Controller.LocalNetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
        } else {
            user.status.alreadyAnswered = true;
            answeringUserId = userId;
            Controller.LocalNetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
            //Controller.NetworkController.sendMessageToConcreteUser(
            //        getOtherUser(userId).status.participantId, OPPONENT_ANSWERING);
        }
        Controller.LocalNetworkAdminUIController.onReceivingAnswer();
        location = LocalGameLocation.ONE_IS_ANSWERING;
    }

    /** Creates UserScore Objects for users */
    public void addUsers(String green, String red) {
        this.green = new UserScore(green);
        this.red = new UserScore(red);
        location = LocalGameLocation.NOT_STARTED;
        Controller.LocalNetworkAdminUIController.setLocation(location);
    }

    /** Clears all information about previous question */
    private void newQuestion() {
        green.status.onNewQuestion();
        red.status.onNewQuestion();
    }

    /** Returns score of red table if it is initialized and "?" otherwise */
    public String getRedScore() {
        return getScore(red);
    }

    /** Returns score of green table if it is initialized and "?" otherwise */
    public String getGreenScore() {
        return getScore(green);
    }

    /** Returns score or "?" by user */
    private String getScore(UserScore user) {
        if (user == null) {
            return "?";
        }
        return String.valueOf(user.score);
    }

    /** Determines if jury can change score now and pluses point if possible */
    public void plusPoint(int userNumber) {
        // TODO
        (userNumber == 1 ? green : red).score++;
    }

    /** Determines if jury can change score now and minuses point if possible */
    public void minusPoint(int userNumber) {
        // TODO
        (userNumber == 1 ? green : red).score--;
    }

    /** Class to store current score and status of user */
    private static class UserScore {
        private int score;
        private UserStatus status;

        private UserScore(String userId) {
            status = new UserStatus(userId);
        }
    }
}
