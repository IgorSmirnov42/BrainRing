package ru.spbhse.brainring.network.timers;

import android.os.CountDownTimer;
import android.util.Log;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.managers.OnlineGameManager;
import ru.spbhse.brainring.network.Network;
import ru.spbhse.brainring.network.messages.OnlineFinishCodes;
import ru.spbhse.brainring.network.messages.messageTypes.FinishMessage;

public class HandshakeTimer extends CountDownTimer {
    private Network network;

    public HandshakeTimer(Network network, int time) {
        super(time, time);
        this.network = network;
    }

    @Override
    public void onTick(long millisUntilFinished) {
    }

    @Override
    public void onFinish() {
        // check in case message was delivered right before finish
        if (network.getHandshakeTimer() == this) {
            Log.wtf(Controller.APP_TAG, "Unsuccessful handshake");
            network.sendMessageToAll(new FinishMessage(OnlineFinishCodes.UNSUCCESSFUL_HANDSHAKE));
        }
    }
}
