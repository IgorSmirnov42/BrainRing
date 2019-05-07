package ru.spbhse.brainring.logic;

import android.os.CountDownTimer;
import android.util.Log;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.ui.LocalGameLocation;

import static java.lang.Math.min;

/**
 * Class realizing admin's logic (counting time, switching locations etc)
 *      in local network mode
 */
public class LocalGameAdminLogic {
    private LocalGameLocation location = LocalGameLocation.GAME_WAITING_START;
    private UserScore green;
    private UserScore red;
    private String answeringUserId;

    private static final byte[] ALLOW_ANSWER = Message.generateMessage(Message.ALLOWED_TO_ANSWER, "");;
    private static final byte[] FORBID_ANSWER = Message.generateMessage(Message.FORBIDDEN_TO_ANSWER, "");

    private static final int FIRST_COUNTDOWN = 20;
    private static final int SECOND_COUNTDOWN = 20;
    private static final int SECOND = 1000;

    private final CountDownTimer firstGameTimer = new CountDownTimer(FIRST_COUNTDOWN * SECOND,
            SECOND) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("BrainRing", "Tick first timer");
            if (timer == this) {
                Controller.LocalNetworkAdminUIController.showTime(
                        min(millisUntilFinished / SECOND + 1, FIRST_COUNTDOWN));
            }
        }

        @Override
        public void onFinish() {
            Log.d("BrainRing", "Finish first timer");
            synchronized (LocalGameAdminLogic.this) {
                if (timer == this) {
                    newQuestion();
                }
            }
        }
    };
    private final CountDownTimer secondGameTimer = new CountDownTimer(SECOND_COUNTDOWN * SECOND,
            SECOND) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("BrainRing", "Tick first timer");
            if (timer == this) {
                Controller.LocalNetworkAdminUIController.showTime(
                        min(millisUntilFinished / SECOND + 1, SECOND_COUNTDOWN));
            }
        }

        @Override
        public void onFinish() {
            Log.d("BrainRing", "Finish first timer");
            synchronized (LocalGameAdminLogic.this) {
                if (timer == this) {
                    newQuestion();
                }
            }
        }
    };
    private CountDownTimer timer;

    /** Called when jury accepts answer */
    public void onAcceptAnswer() {
        getThisUser(answeringUserId).score++;
        answeringUserId = null;
        location = LocalGameLocation.NOT_STARTED;
        Controller.LocalNetworkAdminUIController.setLocation(location);
    }

    /** Called when jury rejects answer */
    public void onRejectAnswer() {
        UserScore other = getOtherUser(answeringUserId);
        answeringUserId = null;
        if (!other.status.alreadyAnswered) {
            Controller.LocalNetworkAdminUIController.showTime(SECOND_COUNTDOWN);
            location = LocalGameLocation.COUNTDOWN;
            Controller.LocalNetworkAdminUIController.setLocation(location);
            timer = secondGameTimer;
            timer.start();
        } else {
            newQuestion();
        }
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
            Controller.LocalNetworkAdminUIController.showTime(FIRST_COUNTDOWN);
            location = LocalGameLocation.COUNTDOWN;
            Controller.LocalNetworkAdminUIController.setLocation(location);
            timer = firstGameTimer;
            timer.start();
            return true;
        }
        // Начало нового раунда
        if (location == LocalGameLocation.COUNTDOWN) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
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

    private boolean bothAnswered() {
        return red.status.alreadyAnswered && green.status.alreadyAnswered;
    }

    /**
     * Allows or forbids to answer team that pushed answer button
     * Determines (no) false starts
     */
    public synchronized void onAnswerIsReady(String userId) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        UserScore user = getThisUser(userId);
        if (location == LocalGameLocation.READING_QUESTION) {
            user.status.alreadyAnswered = true;
            Controller.LocalNetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
            if (bothAnswered()) {
                newQuestion();
            }
            return;
        }
        if (user.status.alreadyAnswered || location != LocalGameLocation.COUNTDOWN) {
            Controller.LocalNetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
        } else {
            user.status.alreadyAnswered = true;
            answeringUserId = userId;
            Controller.LocalNetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
            //Controller.NetworkController.sendMessageToConcreteUser(
            //        getOtherUser(userId).status.participantId, OPPONENT_ANSWERING);
            Controller.LocalNetworkAdminUIController.onReceivingAnswer();
            location = LocalGameLocation.ONE_IS_ANSWERING;
        }
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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        green.status.onNewQuestion();
        red.status.onNewQuestion();
        location = LocalGameLocation.NOT_STARTED;
        Controller.LocalNetworkAdminUIController.setLocation(location);
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

    public void finishGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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
