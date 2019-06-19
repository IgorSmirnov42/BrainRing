package ru.spbhse.brainring.network;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;

import java.io.IOException;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.managers.LocalPlayerGameManager;
import ru.spbhse.brainring.network.callbacks.LocalPlayerRoomUpdateCallback;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.messageTypes.IAmGreenMessage;
import ru.spbhse.brainring.network.messages.messageTypes.IAmRedMessage;

/**
 * Class with methods to interact with network
 * Used by player in a local network mode
 */
public class LocalNetworkPlayer extends LocalNetwork {
    private String serverId;
    /** Green or red table. Values are written in base class */
    private int myColor;
    private LocalPlayerGameManager manager;

    /**
     * Creates new instance of LocalNetworkPlayer.
     * @param myColor string "red" or "green"
     */
    public LocalNetworkPlayer(@NonNull String myColor, LocalPlayerGameManager manager) {
        super(manager);

        this.manager = manager;

        if (myColor.equals("green")) {
            this.myColor = ROLE_GREEN;
        } else {
            this.myColor = ROLE_RED;
        }

        mRoomUpdateCallback = new LocalPlayerRoomUpdateCallback(this, manager);
    }

    /**
     * Decodes byte message received by player and calls needed functions in LocalController
     * If it is a first message to player, sends response if green
     */
    @Override
    protected void onMessageReceived(@NonNull byte[] buf, @NonNull String userId) {
        if (gameIsFinished) {
            return;
        }
        Log.d(Controller.APP_TAG,"RECEIVED MESSAGE AS PLAYER!");

        try {
            Message message = Message.readMessage(buf);
            manager.getProcessor().process(message, userId);
        } catch (IOException e) {
            Log.e(Controller.APP_TAG, "Error while reading message");
            e.printStackTrace();
        }

    }

    public void doInitialHandshake(@NonNull String serverId) {
        this.serverId = serverId;
        handshaked = true;
        if (myColor == ROLE_GREEN) {
            Log.d(Controller.APP_TAG, "I am green");
            sendMessageToConcreteUser(serverId, new IAmGreenMessage());
        } else {
            Log.d(Controller.APP_TAG, "I am red");
            sendMessageToConcreteUser(serverId, new IAmRedMessage());
        }
    }

    /** Starts quick game with auto matched server and player */
    @Override
    public void startQuickGame() {
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(manager.getActivity(),
                googleSignInAccount);
        final int MIN_OPPONENTS = 2, MAX_OPPONENTS = 2;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, myColor);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(manager.getActivity(), googleSignInAccount)
                .create(mRoomConfig);
    }

    /**
     * Sends message to server
     * If server is not known, does nothing
     */
    public void sendMessageToServer(@NonNull Message message) {
        if (serverId == null) {
            Log.d(Controller.APP_TAG, "Sending message before handshake");
        } else {
            sendMessageToConcreteUser(serverId, message);
        }
    }

    @Override
    protected void leaveRoom() {
        if (room != null) {
            Log.d("RainRing","Leaving room");
            mRealTimeMultiplayerClient.leave(mRoomConfig, room.getRoomId());
            room = null;
        }
    }

    @Override
    public void handshake() {
        Log.wtf(Controller.APP_TAG, "Handshake was called for player");
    }
}
