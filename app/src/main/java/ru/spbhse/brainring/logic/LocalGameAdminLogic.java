package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.LocalController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.ui.LocalGameLocation;

import static java.lang.Math.max;

/**
 * Class realizing admin's logic (counting time, switching locations etc)
 *      in local mode
 */
public class LocalGameAdminLogic {
    private LocalGameLocation location = LocalGameLocation.GAME_WAITING_START;
    private UserScore green;
    private UserScore red;
    private String answeringUserId;
    private int handshakeAccepted = 0;

    private static final byte[] ALLOW_ANSWER = Message.generateMessage(Message.ALLOWED_TO_ANSWER, "");;
    private static final byte[] FORBID_ANSWER = Message.generateMessage(Message.FORBIDDEN_TO_ANSWER, "");
    private static final byte[] FALSE_START = Message.generateMessage(Message.FALSE_START, "");
    private static final byte[] TIME_START = Message.generateMessage(Message.TIME_START, "");

    private final int firstCountdown;
    private final int secondCountdown;
    private static final int SECOND = 1000;
    private static final int SENDING_COUNTDOWN = 5;
    private static final int FAULT = 1000; // fault on sending message in milliseconds

    private final CountDownTimer firstGameTimer;
    private final CountDownTimer secondGameTimer;
    private CountDownTimer timer;

    public LocalGameAdminLogic(int firstCountdown, int secondCountdown) {
        this.firstCountdown = firstCountdown;
        this.secondCountdown = secondCountdown;

        firstGameTimer = new CountDownTimer(firstCountdown * SECOND + FAULT,
                SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                //if (timer == this) {
                    LocalController.LocalAdminUIController.showTime(
                            max((millisUntilFinished - FAULT) / SECOND, 0));

                    if (millisUntilFinished - FAULT <= SENDING_COUNTDOWN * SECOND) {
                        new Thread(() -> {
                            MediaPlayer player = MediaPlayer.create(LocalController.getJuryActivity(), R.raw.countdown);
                            player.setOnCompletionListener(MediaPlayer::release);
                            player.start();
                        }).start();
                    }
                //}
            }

            @Override
            public void onFinish() {
                Log.d("BrainRing", "Finish first timer");
                //if (timer == this) {
                    new Thread(() -> {
                        MediaPlayer player = MediaPlayer.create(LocalController.getJuryActivity(), R.raw.beep);
                        player.setOnCompletionListener(MediaPlayer::release);
                        player.start();
                    }).start();
                    newQuestion();
                //}
            }
        };

        secondGameTimer = new CountDownTimer(secondCountdown * SECOND + FAULT,
                SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                //if (timer == this) {
                    LocalController.LocalAdminUIController.showTime(
                            max((millisUntilFinished - FAULT) / SECOND, 0));

                    if (millisUntilFinished - FAULT <= SENDING_COUNTDOWN * SECOND) {
                        new Thread(() -> {
                            MediaPlayer player = MediaPlayer.create(LocalController.getJuryActivity(), R.raw.countdown);
                            player.setOnCompletionListener(MediaPlayer::release);
                            player.start();
                        }).start();
                    }
                //}
            }

            @Override
            public void onFinish() {
                Log.d("BrainRing", "Finish second timer");
                //if (timer == this) {
                    new Thread(() -> {
                        MediaPlayer player = MediaPlayer.create(LocalController.getJuryActivity(), R.raw.beep);
                        player.setOnCompletionListener(MediaPlayer::release);
                        player.start();
                    }).start();
                    newQuestion();
                //}
            }
        };
    }
    
    private void toLocation(LocalGameLocation newLocation) {
        location = newLocation;
        LocalController.LocalAdminUIController.setLocation(location);
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
            LocalController.LocalAdminUIController.setGreenStatus("Ответил");
        } else {
            LocalController.LocalAdminUIController.setRedStatus("Ответил");
        }
        answeringUserId = null;
        if (!other.status.alreadyAnswered) {
            LocalController.LocalAdminUIController.showTime(secondCountdown);
            toLocation(LocalGameLocation.COUNTDOWN);
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
            toLocation(LocalGameLocation.READING_QUESTION);
            return true;
        }
        // Переклчение на таймер
        if (location == LocalGameLocation.READING_QUESTION) {
            LocalController.LocalAdminUIController.showTime(firstCountdown);
            toLocation(LocalGameLocation.COUNTDOWN);
            timer = firstGameTimer;
            timer.start();
            new Thread(() -> {
                MediaPlayer player = MediaPlayer.create(LocalController.getJuryActivity(), R.raw.start);
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
            }).start();
            LocalController.LocalNetworkController.sendMessageToOthers(TIME_START);
            return true;
        }
        // Начало нового раунда
        if (location == LocalGameLocation.COUNTDOWN) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            toLocation(LocalGameLocation.NOT_STARTED);
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
     * Determines false starts
     */
    public void onAnswerIsReady(@NonNull String userId) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        UserScore user = getThisUser(userId);
        if (location == LocalGameLocation.READING_QUESTION) {
            user.status.alreadyAnswered = true;
            LocalController.LocalNetworkController.sendMessageToConcreteUser(userId, FALSE_START);
            if (isGreen(userId)) {
                LocalController.LocalAdminUIController.setGreenStatus("Фальстарт");
            } else {
                LocalController.LocalAdminUIController.setRedStatus("Фальстарт");
            }
            if (bothAnswered()) {
                newQuestion();
            }
            return;
        }
        if (user.status.alreadyAnswered || location != LocalGameLocation.COUNTDOWN) {
            LocalController.LocalNetworkController.sendMessageToConcreteUser(userId, FORBID_ANSWER);
        } else {
            user.status.alreadyAnswered = true;
            answeringUserId = userId;
            new Thread(() -> {
                MediaPlayer player = MediaPlayer.create(LocalController.getJuryActivity(), R.raw.answering);
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
            }).start();
            LocalController.LocalNetworkController.sendMessageToConcreteUser(userId, ALLOW_ANSWER);
            //LocalController.NetworkController.sendMessageToConcreteUser(
            //        getOtherUser(userId).status.participantId, OPPONENT_ANSWERING);
            LocalController.LocalAdminUIController.onReceivingAnswer(getColor(userId));
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
        LocalController.LocalAdminUIController.setGreenStatus("");
        LocalController.LocalAdminUIController.setRedStatus("");
        answeringUserId = null;
        green.status.onNewQuestion();
        red.status.onNewQuestion();
        toLocation(LocalGameLocation.GAME_WAITING_START);
        handshakeAccepted = 0;
        LocalController.LocalNetworkAdminController.handshake();
    }
    
    public void onHandshakeAccept(String userId) {
        ++handshakeAccepted;
        if (handshakeAccepted == 2) {
            LocalController.LocalAdminUIController.setGreenStatus("");
            LocalController.LocalAdminUIController.setRedStatus("");
            toLocation(LocalGameLocation.NOT_STARTED);
            return;
        }
        if (getColor(userId).equals("red")) {
            LocalController.LocalAdminUIController.setRedStatus("Connected");
        } else {
            LocalController.LocalAdminUIController.setGreenStatus("Connected");
        }
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
            Toast.makeText(LocalController.getJuryActivity(), "Изменение счёта невозможно во время вопроса.",
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
