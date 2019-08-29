package ru.spbhse.brainring.network.timers;

import android.os.CountDownTimer;
import android.util.Log;

import ru.spbhse.brainring.network.Network;
import ru.spbhse.brainring.network.messages.OnlineFinishCodes;
import ru.spbhse.brainring.network.messages.messageTypes.FinishMessage;
import ru.spbhse.brainring.utils.Constants;

/**
 * Timer used in {@code Network} to check connection with opponent.
 * If finished without cancel, finishes game
 */
public class HandshakeTimer extends CountDownTimer {
    private Network network;

    public HandshakeTimer(int handshakeTimeSec, Network network) {
        super(handshakeTimeSec * Constants.SECOND,
                handshakeTimeSec * Constants.SECOND);
        this.network = network;
    }

    @Override
    public void onTick(long millisUntilFinished) {
    }

    @Override
    public void onFinish() {
        // check in case message was delivered right before finish
        if (network.getHandshakeTimer() == this) {
            Log.wtf(Constants.APP_TAG, "Unsuccessful handshake");
            network.sendMessageToAll(new FinishMessage(OnlineFinishCodes.UNSUCCESSFUL_HANDSHAKE));
        }
    }
}
