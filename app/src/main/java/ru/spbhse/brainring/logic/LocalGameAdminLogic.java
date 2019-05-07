package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.R;
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
    private static final byte[] FALSE_START = Message.generateMessage(Message.FALSE_START, "");
    private static final byte[] TIME_START = Message.generateMessage(Message.TIME_START, "");

    private final int firstCountdown;
    private final int secondCountdown;
    private static final int SECOND = 1000;
    private static final int SENDING_COUNTDOWN = 5;

    private final CountDownTimer firstGameTimer;
    private final CountDownTimer secondGameTimer;
    private CountDownTimer timer;

    public LocalGameAdminLogic(int firstCountdown, int secondCountdown) {
        this.firstCountdown = firstCountdown;
        this.secondCountdown = secondCountdown;

        firstGameTimer = new CountDownTimer(firstCountdown * SECOND,
                SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("BrainRing", "Tick first timer");
                if (timer == this) {
                    Controller.LocalNetworkAdminUIController.showTime(
                            min(millisUntilFinished / SECOND + 1, firstCountdown));

                    if (millisUntilFinished <= SENDING_COUNTDOWN * SECOND) {
                        new Thread(() -> {
                            MediaPlayer player = MediaPlayer.create(Controller.getJuryActivity(), R.raw.countdown);
                            player.setOnCompletionListener(MediaPlayer::release);
                            player.start();
                        }).start();
                    }
                }
            }

            @Override
            public void onFinish() {
                Log.d("BrainRing", "Finish first timer");
                synchronized (LocalGameAdminLogic.this) {
                    if (timer == this) {
                        new Thread(() -> {
                            MediaPlayer player = MediaPlayer.create(Controller.getJuryActivity(), R.raw.beep);
                            player.setOnCompletionListener(MediaPlayer::release);
                            player.start();
                        }).start();
                        newQuestion();
                    }
                }
            }
        };

        secondGameTimer = new CountDownTimer(secondCountdown * SECOND,
                SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("BrainRing", "Tick first timer");
                if (timer == this) {
                    Controller.LocalNetworkAdminUIController.showTime(
                            min(millisUntilFinished / SECOND + 1, secondCountdown));

                    if (millisUntilFinished <= SENDING_COUNTDOWN * SECOND) {
                        new Thread(() -> {
                            MediaPlayer player = MediaPlayer.create(Controller.getJuryActivity(), R.raw.countdown);
                            player.setOnCompletionListener(MediaPlayer::release);
                            player.start();
                        }).start();
                    }
                }
            }

            @Override
            public void onFinish() {
                Log.d("BrainRing", "Finish first timer");
                synchronized (LocalGameAdminLogic.this) {
                    if (timer == this) {
                        new Thread(() -> {
                            MediaPlayer player = MediaPlayer.create(Controller.getJuryActivity(), R.raw.beep);
                            player.setOnCompletionListener(MediaPlayer::release);
                            player.start();
                        }).start();
                        newQuestion();
                    }
                }
            }
        };
    }

    /** Called when jury accepts answer */
    public void onAcceptAnswer() {
        getThisUser(answeringUserId).score++;
        newQuestion();
    }

    /** Called when jury rejects answer */
    public void onRejectAnswer() {
        UserScore other = getOtherUser(answeringUserId);
        if (isGreen(answeringUserId)) {
            Controller.LocalNetworkAdminUIController.setGreenStatus("Ответил");
        } else {
            Controller.LocalNetworkAdminUIController.setRedStatus("Ответил");
        }
        answeringUserId = null;
        if (!other.status.alreadyAnswered) {
            Controller.LocalNetworkAdminUIController.showTime(secondCountdown);
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
            Controller.LocalNetworkAdminUIController.showTime(firstCountdown);
            location = LocalGameLocation.COUNTDOWN;
            Controller.LocalNetworkAdminUIController.setLocation(location);
            timer = firstGameTimer;
            timer.start();
            new Thread(() -> {
                MediaPlayer player = MediaPlayer.create(Controller.getJuryActivity(), R.raw.start);
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
            }).start();
            Controller.LocalNetworkController.sendMessageToOthers(TIME_START);
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
    private UserScore getThisUser(@NonNull String userId) {
        return green.status.participantId.equals(userId) ? green : red;
    }

    /** Returns UserScore object connected with opponent of given user */
    private UserScore getOtherUser(@NonNull String userId) {
        return green.status.participantId.equals(userId) ? red : green;
    }

    private boolean bothAnswered() {
        return red.status.alreadyAnswered && green.status.alreadyAnswered;
    }

    private boolean isGreen(@NonNull String userId) {
        return green.status.participantId.equals(userId);
    }

    /**
     * Allows or forbids to answer team that pushed answer button
     * Determines (no) false starts
     */
    public synchronized void onAnswerIsReady(@NonNull String userId) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        UserScore user = getThisUser(userId);
        if (location == LocalGameLocation.READING_QUESTION) {
            user.status.alreadyAnswered = true;
            Controller.LocalNetworkController.sendMessageToConcreteUser(userId, FALSE_START);
            if (isGreen(userId)) {
                Controller.LocalNetworkAdminUIController.setGreenStatus("Фальстарт");
            } else {
                Controller.LocalNetworkAdminUIController.setRedStatus("Фальстарт");
            }
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
            new Thread(() -> {
                MediaPlayer player = MediaPlayer.create(Controller.getJuryActivity(), R.raw.answering);
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
            }).start();
            Controller.LocalNetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
            //Controller.NetworkController.sendMessageToConcreteUser(
            //        getOtherUser(userId).status.participantId, OPPONENT_ANSWERING);
            Controller.LocalNetworkAdminUIController.onReceivingAnswer(getColor(userId));
            location = LocalGameLocation.ONE_IS_ANSWERING;
        }
    }

    private String getColor(@NonNull String userId) {
        if (green.status.participantId.equals(userId)) {
            return "green";
        } else {
            return "red";
        }
    }

    /** Creates UserScore Objects for users */
    public void addUsers(@NonNull String green, @NonNull String red) {
        this.green = new UserScore(green);
        this.red = new UserScore(red);
        newQuestion();
    }

    /** Clears all information about previous question */
    private void newQuestion() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        Controller.LocalNetworkAdminUIController.setGreenStatus("");
        Controller.LocalNetworkAdminUIController.setRedStatus("");
        answeringUserId = null;
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

    private boolean canChangeScore() {
        if (location == LocalGameLocation.NOT_STARTED && red != null && green != null) {
            return true;
        } else {
            Toast.makeText(Controller.getJuryActivity(), "Изменение счёта невозможно во время вопроса.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /** Determines if jury can change score now and pluses point if possible */
    public void plusPoint(int userNumber) {
        if (canChangeScore()) {
            (userNumber == 1 ? green : red).score++;
        }
    }

    /** Determines if jury can change score now and minuses point if possible */
    public void minusPoint(int userNumber) {
        if (canChangeScore()) {
            (userNumber == 1 ? green : red).score--;
        }
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
