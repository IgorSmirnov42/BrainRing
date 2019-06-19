package ru.spbhse.brainring.network.timers;

import android.os.CountDownTimer;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.managers.OnlineGameManager;
import ru.spbhse.brainring.network.messages.OnlineFinishCodes;
import ru.spbhse.brainring.network.messages.messageTypes.FinishMessage;
import ru.spbhse.brainring.network.Network;

public class TimeoutTimer extends CountDownTimer {
    /**
     * If server doesn't receive any messages during that time it panics and finishes game
     * This time is bigger than a longest time without messages from concrete user, so if timer
     *      panics then definitely something gone wrong
     */
    private static final int MAXIMUM_TIME_WITHOUT_MESSAGES = 80 * 1000;
    private Network network;
    private OnlineGameManager manager;

    public TimeoutTimer(Network network, OnlineGameManager manager) {
        super(MAXIMUM_TIME_WITHOUT_MESSAGES, MAXIMUM_TIME_WITHOUT_MESSAGES);
        this.network = network;
        this.manager = manager;
    }

    @Override
    public void onTick(long millisUntilFinished) {
    }

    @Override
    public void onFinish() {
        if (network.getTimer() == this) {
            if (network.getConnections() == 0) {
                network.finishImmediately(manager.getActivity()
                        .getString(R.string.opponent_not_found));
            } else {
                network.sendMessageToAll(new FinishMessage(OnlineFinishCodes.SERVER_TIMER_TIMEOUT));
            }
        }
    }
}
