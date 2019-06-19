package ru.spbhse.brainring.network;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;

import java.io.IOException;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.managers.LocalAdminGameManager;
import ru.spbhse.brainring.network.callbacks.LocalAdminRoomUpdateCallback;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.messageTypes.HandshakeMessage;
import ru.spbhse.brainring.network.messages.messageTypes.InitialHandshakeMessage;

/**
 * Class with methods to interact with network
 * Used by admin in a local network mode
 */
public class LocalNetworkAdmin extends LocalNetwork {
    private LocalAdminGameManager manager;
    private String redId;
    private String greenId;
    private static final Message HANDSHAKE = new HandshakeMessage();
    private static final int HANDSHAKE_DELAY = 1000;

    /**
     * Creates new instance. Fills {@code mRoomUpdateCallback} with an instance that
     *      on connected room starts game
     */
    public LocalNetworkAdmin(LocalAdminGameManager manager) {
        super(manager);
        this.manager = manager;
        mRoomUpdateCallback = new LocalAdminRoomUpdateCallback(this);
    }

    /** Decodes byte message received by server and calls needed functions in LocalController */
    @Override
    protected void onMessageReceived(@NonNull byte[] buf, @NonNull String userId) {
        if (gameIsFinished) {
            return;
        }
        Log.d(Controller.APP_TAG, "Received message as admin!");
        try {
            Message message = Message.readMessage(buf);
            manager.getProcessor().process(message, userId);
        } catch (IOException e) {
            Log.e(Controller.APP_TAG, "Error while reading message");
            e.printStackTrace();
        }
    }

    /** Sets green player id. If both players shared their ids starts game cycle */
    public void setGreenPlayer(@NonNull String userId) {
        if (handshaked) {
            Log.d(Controller.APP_TAG, "Handshake is done");
            return;
        }
        greenId = userId;
        if (redId != null) {
            handshaked = true;
            manager.getLogic().startGameCycle(greenId, redId);
        }
    }

    /** Sets red player id. If both players shared their ids starts game cycle */
    public void setRedPlayer(@NonNull String userId) {
        if (handshaked) {
            Log.d(Controller.APP_TAG, "Handshake is done");
            return;
        }
        redId = userId;
        if (greenId != null) {
            handshaked = true;
            manager.getLogic().startGameCycle(greenId, redId);
        }
    }

    /** Starts quick game with two auto matched players */
    @Override
    public void startQuickGame() {
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(manager.getActivity(),
                googleSignInAccount);
        final int MIN_OPPONENTS = 2, MAX_OPPONENTS = 2;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, ROLE_ADMIN);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(manager.getActivity(), googleSignInAccount)
                .create(mRoomConfig);
    }

    /**
     * Sends empty message to players in order to determine which of them is green/red
     * Waits while the answer isn't received
     * After execution starts game cycle
     */
    @Override
    public void handshake() {
        if (handshaked) {
            return;
        }
        Log.d(Controller.APP_TAG, "Start handshake");
        sendMessageToOthers(new InitialHandshakeMessage());
        // Sometimes first message doesn't reach opponent for some reason
        // so we have to send it one more time
        new Handler().postDelayed(this::handshake, HANDSHAKE_DELAY);
    }

    /**
     * Sends {@code HANDSHAKE} message to others to check that both players are connected
     * Called before each question
     */
    public void regularHandshake() {
        sendMessageToOthers(HANDSHAKE);
    }

    @Override
    protected void leaveRoom() {
        if (room != null) {
            Log.d(Controller.APP_TAG,"Leaving room");
            mRealTimeMultiplayerClient.leave(mRoomConfig, room.getRoomId());
            room = null;
        }
    }

    public void serverRoomIsConnected() {
        serverRoomConnected = true;
    }

    public LocalAdminGameManager getManager() {
        return manager;
    }
}
