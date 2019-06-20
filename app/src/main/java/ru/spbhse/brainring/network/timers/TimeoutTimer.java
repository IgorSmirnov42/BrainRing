package ru.spbhse.brainring.network.timers;

import android.os.CountDownTimer;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.network.messages.OnlineFinishCodes;
import ru.spbhse.brainring.network.messages.messageTypes.FinishMessage;
import ru.spbhse.brainring.network.Network;
import ru.spbhse.brainring.utils.Constants;

/**
 * Timer used in online game for disconnecting if opponent is not found for a long time or
 *      haven't received messages from opponent for a long time
 * If finishes without cancel finishes game
 */
public class TimeoutTimer extends CountDownTimer {
    private Network network;
    public TimeoutTimer(int timeoutSec, Network network) {
        super(timeoutSec * Constants.SECOND,
                timeoutSec * Constants.SECOND);
        this.network = network;
    }

    @Override
    public void onTick(long millisUntilFinished) {
    }

    @Override
    public void onFinish() {
        if (network.getTimer() == this) {
            if (network.getConnections() == 0) {
                network.finishImmediately(network.getManager().getActivity()
                        .getString(R.string.opponent_not_found));
            } else {
                network.sendMessageToAll(new FinishMessage(OnlineFinishCodes.SERVER_TIMER_TIMEOUT));
            }
        }
    }
}
