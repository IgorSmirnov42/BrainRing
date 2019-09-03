package ru.spbhse.brainring.logic;

import android.app.AlertDialog;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.logic.timers.LocalTimer;
import ru.spbhse.brainring.managers.LocalAdminGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.messageTypes.AllowedToAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.FalseStartMessage;
import ru.spbhse.brainring.network.messages.messageTypes.ForbiddenToAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.TellYourTimeMessage;
import ru.spbhse.brainring.network.messages.messageTypes.TimeStartMessage;
import ru.spbhse.brainring.ui.LocalGameLocation;
import ru.spbhse.brainring.utils.Constants;
import ru.spbhse.brainring.utils.LocalGameRoles;
import ru.spbhse.brainring.utils.SoundPlayer;

import static java.lang.Math.max;

/** Class realizing admin's logic (counting time, switching locations etc) in local mode */
public class LocalGameAdminLogic {
    private SoundPlayer player = new SoundPlayer();
    private LocalAdminGameManager manager;
    private LocalGameLocation location = LocalGameLocation.GAME_WAITING_START;
    private UserScore green;
    private UserScore red;
    /** Id of user who is currently answering. null if no one is answering */
    private String answeringUserId;
    private int handshakeAccepted = 0;
    private long roundStartTime;

    private static final int REQUESTS_NUM = 100;

    private ArrayList<TimeUser> waitingList = new ArrayList<>();
    private CountDownTimer judgingTimer;

    private static final Message ALLOW_ANSWER = new AllowedToAnswerMessage();
    private static final Message FORBID_ANSWER = new ForbiddenToAnswerMessage();
    private static final Message FALSE_START = new FalseStartMessage();
    private static final Message TIME_START = new TimeStartMessage();

    /** Same as {@code FIRST_COUNTDOWN} in online mode, but editable here */
    private final int firstCountdown;
    /** Same as {@code SECOND_COUNTDOWN} in online mode, but editable here */
    private final int secondCountdown;
    /** Time when timer starts showing left time to think */
    private static final int SENDING_COUNTDOWN = 5;
    /** Time on delivering fault (millis) */
    private int fault;
    private int judgingDelay;

    /** Current timer */
    private LocalTimer timer;

    /** Creates new instance of LocalGameAdminLogic. Initializes timers */
    public LocalGameAdminLogic(int firstCountdown, int secondCountdown,
                               LocalAdminGameManager manager) {
        this.firstCountdown = firstCountdown;
        this.secondCountdown = secondCountdown;
        this.manager = manager;
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
            timer = new LocalTimer(secondCountdown, fault, SENDING_COUNTDOWN, this);
            roundStartTime = System.currentTimeMillis();
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
            timer = new LocalTimer(firstCountdown, fault, SENDING_COUNTDOWN, this);
            roundStartTime = System.currentTimeMillis();
            timer.start();
            player.play(manager.getActivity(), R.raw.start);
            manager.getNetwork().sendMessageToUsers(TIME_START);
            return true;
        }
        // Start of a new round
        if (location == LocalGameLocation.COUNTDOWN) {
            newQuestion();
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

    private void forbidAnswer(@NonNull String userId) {
        Log.d(Constants.APP_TAG, "Forbid answer " + userId);
        manager.getNetwork().sendMessageToConcreteUser(userId, FORBID_ANSWER);
    }

    private void allowAnswer(@NonNull String userId, String time) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        Log.d(Constants.APP_TAG, "Allow answer " + userId);
        UserScore user = getThisUser(userId);
        user.status.setAlreadyAnswered(true);
        answeringUserId = userId;
        player.play(manager.getActivity(), R.raw.answering);
        manager.getNetwork().sendMessageToConcreteUser(userId, ALLOW_ANSWER);
        manager.getActivity().onReceivingAnswer(getColor(userId), time);
        location = LocalGameLocation.ONE_IS_ANSWERING;
    }

    private long getAverageTimeInsideRound(UserScore user, long time) {
        long tempAns = user.dif + time - roundStartTime;
        if (tempAns < 0) {
            tempAns = 0;
        }
        if (tempAns >= timer.getTotalTime()) {
            tempAns = timer.getTotalTime() - 1;
        }
        return tempAns;
    }

    private long getMinimalTime(UserScore user, long time) {
        return time + user.dif - user.eps - roundStartTime;
    }

    private long getMaximalTime(UserScore user, long time) {
        return time + user.dif + user.eps - roundStartTime;
    }

    /**
     * Allows or forbids to answer team that pushed answer button
     * Determines false starts
     */
    public void onAnswerIsReady(@NonNull String userId, long time) {
        if (green == null || red == null) {
            return;
        }
        UserScore user = getThisUser(userId);
        Log.d(Constants.APP_TAG, "User is ready " +
                ((System.currentTimeMillis() - roundStartTime) - getAverageTimeInsideRound(user, time)));
        if (location == LocalGameLocation.READING_QUESTION ||
                (location == LocalGameLocation.COUNTDOWN && getMaximalTime(user, time) < 0
                        && !user.status.getAlreadyAnswered())) {
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
        if (user.status.getAlreadyAnswered() || location != LocalGameLocation.COUNTDOWN ||
                getMinimalTime(user, time) > timer.getTotalTime()) {
            forbidAnswer(userId);
        } else {
            Log.d(Constants.APP_TAG, "Average time is " + getAverageTimeInsideRound(user, time));
            waitingList.add(new TimeUser(getAverageTimeInsideRound(user, time), userId));
            if (judgingTimer == null) {
                judgingTimer = new CountDownTimer(judgingDelay, judgingDelay) {
                    @Override
                    public void onTick(long millisUntilFinished) {}

                    @Override
                    public void onFinish() {
                        if (judgingTimer == this) {
                            judge();
                        }
                    }
                };
                judgingTimer.start();
            }
        }
    }

    private void judge() {
        judgingTimer = null;
        if (waitingList.isEmpty()) {
            Log.wtf(Constants.APP_TAG, "judge() was called but list is empty");
            return;
        }
        Collections.sort(waitingList, (o1, o2) -> (int) (o1.time - o2.time));
        allowAnswer(waitingList.get(0).userId, toSeconds(waitingList.get(0).time));
        for (int i = 1; i < waitingList.size(); i++) {
            if (!waitingList.get(0).userId.equals(waitingList.get(i).userId)) {
                forbidAnswer(waitingList.get(i).userId);
            }
        }
        waitingList.clear();
    }

    private void startRound() {
        manager.getActivity().setGreenStatus("");
        manager.getActivity().setRedStatus("");
        toLocation(LocalGameLocation.NOT_STARTED);
    }

    private String toSeconds(long num) {
        String ans = "";
        ans += num / Constants.SECOND + ".";
        num %= Constants.SECOND;
        if (num < 100) {
            ans += "0";
        }
        if (num < 10) {
            ans += "0";
        }
        ans += num;
        return ans;
    }

    private void finishSpeedTest() {
        judgingDelay = (int) (max(red.toJudge, green.toJudge) * 1.5);
        fault = 2 * judgingDelay;
        Log.d(Constants.APP_TAG,
                "Finished speed test. Delay is " + judgingDelay + ", fault is " + fault);
        AlertDialog.Builder builder = new AlertDialog.Builder(manager.getActivity());
        builder.setMessage("Погрешность: " + toSeconds(red.eps + green.eps) + " sec\n" +
                "Задержка: " + toSeconds(judgingDelay) + " sec")
                .setPositiveButton("OK", (dialog, id) -> startRound())
                .setNegativeButton("Заново", (dialog, id) -> newQuestion());
        builder.create();
        builder.show();
    }

    public void onTimeReceived(@NonNull String userId, long timeOther, long currentTime) {
        if (location != LocalGameLocation.GAME_WAITING_START) {
            return;
        }
        //Log.d(Constants.APP_TAG, "Received time from " + userId + " " + timeOther + " " + currentTime);
        UserScore user = getThisUser(userId);
        long averageTime = (currentTime + user.lastSendTime) / 2;
        long newDif = averageTime - timeOther;
        long newEps = currentTime - averageTime;
        if (user.eps > newEps) {
            user.eps = newEps;
            user.dif = newDif;
            Log.d(Constants.APP_TAG, "Recalculated eps " + userId + " new eps is " + user.eps);
        }
        user.toJudge = (int) max(user.toJudge, System.currentTimeMillis() - user.lastSendTime);
        --user.needRequests;
        if (user.needRequests <= 0) {
            ++handshakeAccepted;
            if (getColor(userId) == LocalGameRoles.ROLE_RED) {
                manager.getActivity().setRedStatus(manager.getActivity().getString(R.string.connected));
            } else {
                manager.getActivity().setGreenStatus(manager.getActivity().getString(R.string.connected));
            }
            if (handshakeAccepted == 2) {
                finishSpeedTest();
            }
        } else {
            sendTimeMessage(user);
        }
    }

    /** Returns user's color by id */
    private LocalGameRoles getColor(@NonNull String userId) {
        if (green.status.getParticipantId().equals(userId)) {
            return LocalGameRoles.ROLE_GREEN;
        } else {
            return LocalGameRoles.ROLE_RED;
        }
    }

    /** Creates UserScore Objects for users and starts game */
    public void startGameCycle(@NonNull String green, @NonNull String red) {
        this.green = new UserScore(green);
        this.red = new UserScore(red);
        newQuestion();
    }

    /** Clears all information about previous question */
    public void newQuestion() {
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
        red.needRequests = REQUESTS_NUM;
        green.needRequests = REQUESTS_NUM;
        red.toJudge = 0;
        green.toJudge = 0;
        judgingTimer = null;
        waitingList.clear();
        startSpeedTest();
    }

    private void sendTimeMessage(@NonNull UserScore user) {
        user.lastSendTime = System.currentTimeMillis();
        manager.getNetwork().sendMessageToConcreteUser(user.status.getParticipantId(),
                new TellYourTimeMessage());
    }

    private void startSpeedTest() {
        sendTimeMessage(red);
        sendTimeMessage(green);
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
            manager.getActivity().makeToast(manager.getActivity().getString(R.string.cannot_change_score));
            return false;
        }
    }

    /** Determines if jury can change score now and pluses point if possible */
    public void plusPoint(LocalGameRoles user) {
        if (canChangeScore()) {
            (user == LocalGameRoles.ROLE_GREEN ? green : red).score++;
        }
    }

    /** Determines if jury can change score now and minuses point if possible */
    public void minusPoint(LocalGameRoles user) {
        if (canChangeScore()) {
            (user == LocalGameRoles.ROLE_GREEN ? green : red).score--;
        }
    }

    /** Cancels timer */
    public void finishGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        player.finish();
    }

    public LocalTimer getTimer() {
        return timer;
    }

    public LocalAdminGameManager getManager() {
        return manager;
    }

    public SoundPlayer getPlayer() {
        return player;
    }

    private static class TimeUser {
        private final long time;
        private final String userId;

        private TimeUser(long time, String userId) {
            this.time = time;
            this.userId = userId;
        }
    }

    /** Class to store current score and status of user */
    private static class UserScore {
        private int score;
        private UserStatus status;
        private long dif = Long.MAX_VALUE;
        private long eps = Long.MAX_VALUE;
        private long lastSendTime;
        private int needRequests;
        private int toJudge;

        private UserScore(String userId) {
            status = new UserStatus(userId);
        }
    }
}
