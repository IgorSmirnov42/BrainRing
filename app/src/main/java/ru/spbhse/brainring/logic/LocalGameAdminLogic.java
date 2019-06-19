package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.managers.LocalAdminGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.messageTypes.AllowedToAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.FalseStartMessage;
import ru.spbhse.brainring.network.messages.messageTypes.ForbiddenToAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.TimeStartMessage;
import ru.spbhse.brainring.ui.LocalGameLocation;

import static java.lang.Math.max;

/** Class realizing admin's logic (counting time, switching locations etc) in local mode */
public class LocalGameAdminLogic {
    public static final String GREEN = "green";
    public static final String RED = "red";
    private LocalAdminGameManager manager;
    private LocalGameLocation location = LocalGameLocation.GAME_WAITING_START;
    private UserScore green;
    private UserScore red;
    /** Id of user who is currently answering. null if no one is answering */
    private String answeringUserId;
    private int handshakeAccepted = 0;

    private static final Message ALLOW_ANSWER = new AllowedToAnswerMessage();
    private static final Message FORBID_ANSWER = new ForbiddenToAnswerMessage();
    private static final Message FALSE_START = new FalseStartMessage();
    private static final Message TIME_START = new TimeStartMessage();

    /** Same as {@code FIRST_COUNTDOWN} in online mode, but editable here */
    private final int firstCountdown;
    /** Same as {@code SECOND_COUNTDOWN} in online mode, but editable here */
    private final int secondCountdown;
    private static final int SECOND = 1000;
    /** Time when timer starts showing left time to think */
    private static final int SENDING_COUNTDOWN = 5;
    /** Time on delivering fault */
    private static final int FAULT = 1000;

    /** Timer on firstCountdown * SECOND + FAULT ms */
    private final CountDownTimer firstGameTimer;
    /** Timer on secondCountdown * SECOND + FAULT ms */
    private final CountDownTimer secondGameTimer;
    /** Current timer */
    private CountDownTimer timer;

    /** Creates new instance of LocalGameAdminLogic. Initializes timers */
    public LocalGameAdminLogic(int firstCountdown, int secondCountdown,
                               LocalAdminGameManager manager) {
        this.firstCountdown = firstCountdown;
        this.secondCountdown = secondCountdown;
        this.manager = manager;

        firstGameTimer = new CountDownTimer(firstCountdown * SECOND + FAULT,
                SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timer == this) {
                    manager.getActivity().showTime(max((millisUntilFinished - FAULT) / SECOND, 0));

                    if (millisUntilFinished - FAULT <= SENDING_COUNTDOWN * SECOND) {
                        new Thread(() -> {
                            MediaPlayer player = MediaPlayer.create(manager.getActivity(),
                                    R.raw.countdown);
                            player.setOnCompletionListener(MediaPlayer::release);
                            player.start();
                        }).start();
                    }
                }
            }

            @Override
            public void onFinish() {
                Log.d(Controller.APP_TAG, "Finish first timer");
                if (timer == this) {
                    new Thread(() -> {
                        MediaPlayer player = MediaPlayer.create(manager.getActivity(), R.raw.beep);
                        player.setOnCompletionListener(MediaPlayer::release);
                        player.start();
                    }).start();
                    newQuestion();
                }
            }
        };

        secondGameTimer = new CountDownTimer(secondCountdown * SECOND + FAULT,
                SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timer == this) {
                    manager.getActivity().showTime(max((millisUntilFinished - FAULT) / SECOND, 0));

                    if (millisUntilFinished - FAULT <= SENDING_COUNTDOWN * SECOND) {
                        new Thread(() -> {
                            MediaPlayer player = MediaPlayer.create(manager.getActivity(),
                                    R.raw.countdown);
                            player.setOnCompletionListener(MediaPlayer::release);
                            player.start();
                        }).start();
                    }
                }
            }

            @Override
            public void onFinish() {
                Log.d(Controller.APP_TAG, "Finish second timer");
                if (timer == this) {
                    new Thread(() -> {
                        MediaPlayer player = MediaPlayer.create(manager.getActivity(), R.raw.beep);
                        player.setOnCompletionListener(MediaPlayer::release);
                        player.start();
                    }).start();
                    newQuestion();
                }
            }
        };
    }

    /** Changes location here and on a screen */
    private void toLocation(@NonNull LocalGameLocation newLocation) {
        location = newLocation;
        manager.getActivity().setLocation(location);
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
            manager.getActivity().setGreenStatus(manager.getActivity().getString(R.string.answered));
        } else {
            manager.getActivity().setRedStatus(manager.getActivity().getString(R.string.answered));
        }
        answeringUserId = null;
        if (!other.status.getAlreadyAnswered()) {
            manager.getActivity().showTime(secondCountdown);
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
        // If the game isn't started buttons are inactive
        if (location == LocalGameLocation.GAME_WAITING_START) {
            return false;
        }
        // Cannot switch while answer
        if (location == LocalGameLocation.ONE_IS_ANSWERING) {
            return false;
        }
        // Switching to reading question
        if (location == LocalGameLocation.NOT_STARTED) {
            toLocation(LocalGameLocation.READING_QUESTION);
            return true;
        }
        // Switching to timer
        if (location == LocalGameLocation.READING_QUESTION) {
            manager.getActivity().showTime(firstCountdown);
            toLocation(LocalGameLocation.COUNTDOWN);
            timer = firstGameTimer;
            timer.start();
            new Thread(() -> {
                MediaPlayer player = MediaPlayer.create(manager.getActivity(), R.raw.start);
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
            }).start();
            manager.getNetwork().sendMessageToOthers(TIME_START);
            return true;
        }
        // Start of a new round
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
        return green.status.getParticipantId().equals(userId) ? green : red;
    }

    /** Returns UserScore object connected with opponent of given user */
    private UserScore getOtherUser(@NonNull String userId) {
        return green.status.getParticipantId().equals(userId) ? red : green;
    }

    /** Checks if no one can answer more */
    private boolean bothAnswered() {
        return red.status.getAlreadyAnswered() && green.status.getAlreadyAnswered();
    }

    /** Checks if given user is green */
    private boolean isGreen(@NonNull String userId) {
        return green.status.getParticipantId().equals(userId);
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
            user.status.setAlreadyAnswered(true);
            manager.getNetwork().sendMessageToConcreteUser(userId, FALSE_START);
            if (isGreen(userId)) {
                manager.getActivity().setGreenStatus(
                        manager.getActivity().getString(R.string.false_start));
            } else {
                manager.getActivity().setRedStatus(
                        manager.getActivity().getString(R.string.false_start));
            }
            if (bothAnswered()) {
                newQuestion();
            }
            return;
        }
        if (user.status.getAlreadyAnswered() || location != LocalGameLocation.COUNTDOWN) {
            manager.getNetwork().sendMessageToConcreteUser(userId, FORBID_ANSWER);
        } else {
            user.status.setAlreadyAnswered(true);
            answeringUserId = userId;
            new Thread(() -> {
                MediaPlayer player = MediaPlayer.create(manager.getActivity(), R.raw.answering);
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
            }).start();
            manager.getNetwork().sendMessageToConcreteUser(userId, ALLOW_ANSWER);
            manager.getActivity().onReceivingAnswer(getColor(userId));
            location = LocalGameLocation.ONE_IS_ANSWERING;
        }
    }

    /** Returns user's color by id */
    private String getColor(@NonNull String userId) {
        if (green.status.getParticipantId().equals(userId)) {
            return GREEN;
        } else {
            return RED;
        }
    }

    /** Creates UserScore Objects for users and starts game */
    public void startGameCycle(@NonNull String green, @NonNull String red) {
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
        manager.getActivity().setGreenStatus("");
        manager.getActivity().setRedStatus("");
        answeringUserId = null;
        green.status.onNewQuestion();
        red.status.onNewQuestion();
        toLocation(LocalGameLocation.GAME_WAITING_START);
        handshakeAccepted = 0;
        manager.getNetwork().regularHandshake();
    }

    /** Changes user's information when he/she answers on handshake */
    public void onHandshakeAccept(@NonNull String userId) {
        ++handshakeAccepted;
        if (handshakeAccepted == 2) {
            manager.getActivity().setGreenStatus("");
            manager.getActivity().setRedStatus("");
            toLocation(LocalGameLocation.NOT_STARTED);
            return;
        }
        if (getColor(userId).equals("red")) {
            manager.getActivity().setRedStatus(manager.getActivity().getString(R.string.connected));
        } else {
            manager.getActivity().setGreenStatus(manager.getActivity().getString(R.string.connected));
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

    /** Checks if jury can change score in current location */
    private boolean canChangeScore() {
        if (location == LocalGameLocation.NOT_STARTED && red != null && green != null) {
            return true;
        } else {
            Toast.makeText(manager.getActivity(),
                    manager.getActivity().getString(R.string.cannot_change_score),
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

    /** Cancels timer */
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
