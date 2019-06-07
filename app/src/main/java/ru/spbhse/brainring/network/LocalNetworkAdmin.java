package ru.spbhse.brainring.network;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.LocalController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageGenerator;

/**
 * Class with methods to interact with network
 * Used by admin in a local network mode
 */
public class LocalNetworkAdmin extends LocalNetwork {
    private String redId;
    private String greenId;
    private static final byte[] HANDSHAKE;
    private static final int HANDSHAKE_DELAY = 1000;

    static {
        HANDSHAKE = MessageGenerator.create()
                .writeInt(Message.HANDSHAKE)
                .toByteArray();
    }

    /**
     * Creates new instance. Fills {@code mRoomUpdateCallback} with an instance that
     *      on connected room starts game
     */
    public LocalNetworkAdmin() {
        super();
        mRoomUpdateCallback = new RoomUpdateCallback() {
            @Override
            public void onRoomCreated(int i, @Nullable Room room) {
                Log.d(Controller.APP_TAG, "Room was created");
                LocalNetworkAdmin.this.room = room;
            }

            @Override
            public void onJoinedRoom(int i, @Nullable Room room) {
                Log.d(Controller.APP_TAG, "Joined room");
                LocalNetworkAdmin.this.room = room;
            }

            @Override
            public void onLeftRoom(int i, @NonNull String s) {
                Log.d(Controller.APP_TAG, "Left room");
                if (!gameIsFinished) {
                    LocalController.finishLocalGame(true);
                }
            }

            /** Gets participant id. If both players are p2p connected starts handshake process */
            @Override
            public void onRoomConnected(int code, @Nullable Room room) {
                Log.d(Controller.APP_TAG, "Connected to room");
                if (room == null) {
                    Log.wtf(Controller.APP_TAG, "onRoomConnected got null as room");
                    return;
                }
                LocalNetworkAdmin.this.room = room;
                if (code == GamesCallbackStatusCodes.OK) {
                    Log.d(Controller.APP_TAG,"Connected");
                } else {
                    Log.d(Controller.APP_TAG,"Connecting error");
                }
                Games.getPlayersClient(LocalController.getJuryActivity(), googleSignInAccount)
                        .getCurrentPlayerId()
                        .addOnSuccessListener(myPlayerId -> {
                            myParticipantId = room.getParticipantId(myPlayerId);
                            serverRoomConnected = true;
                            if (p2pConnected == 2) {
                                handshake();
                            }
                        });
            }
        };
    }

    /** Decodes byte message received by server and calls needed functions in LocalController */
    @Override
    protected void onMessageReceived(@NonNull byte[] buf, @NonNull String userId) {
        if (gameIsFinished) {
            return;
        }
        Log.d(Controller.APP_TAG, "Received message as admin!");
        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(buf))) {
            int identifier = is.readInt();
            Log.d(Controller.APP_TAG, "Identifier is " + identifier);

            switch(identifier) {
                case Message.I_AM_GREEN:
                    setGreenPlayer(userId);
                    break;
                case Message.I_AM_RED:
                    setRedPlayer(userId);
                    break;
                case Message.ANSWER_IS_READY:
                    LocalController.LocalAdminLogicController.onAnswerIsReady(userId);
                    break;
                case Message.HANDSHAKE:
                    LocalController.LocalAdminLogicController.onHandshakeAccept(userId);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Sets green player id. If both players shared their ids starts game cycle */
    private void setGreenPlayer(@NonNull String userId) {
        if (handshaked) {
            Log.d(Controller.APP_TAG, "Handshake is done");
            return;
        }
        greenId = userId;
        if (redId != null) {
            handshaked = true;
            LocalController.LocalNetworkAdminController.startGameCycle();
        }
    }

    /** Sets red player id. If both players shared their ids starts game cycle */
    private void setRedPlayer(@NonNull String userId) {
        if (handshaked) {
            Log.d(Controller.APP_TAG, "Handshake is done");
            return;
        }
        redId = userId;
        if (greenId != null) {
            handshaked = true;
            LocalController.LocalNetworkAdminController.startGameCycle();
        }
    }

    /** Starts quick game with two auto matched players */
    @Override
    public void startQuickGame() {
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(
                LocalController.getJuryActivity(), googleSignInAccount);
        final int MIN_OPPONENTS = 2, MAX_OPPONENTS = 2;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, ROLE_ADMIN);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(LocalController.getJuryActivity(), googleSignInAccount)
                .create(mRoomConfig);
    }

    /**
     * Sends empty message to players in order to determine which of them is green/red
     * Waits while the answer isn't received
     * After execution starts game cycle
     */
    @Override
    protected void handshake() {
        if (handshaked) {
            return;
        }
        Log.d(Controller.APP_TAG, "Start handshake");
        byte[] message = MessageGenerator.create()
                .writeInt(Message.INITIAL_HANDSHAKE)
                .toByteArray();
        sendMessageToOthers(message);
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
            Games.getRealTimeMultiplayerClient(LocalController.getJuryActivity(),
                    googleSignInAccount).leave(mRoomConfig, room.getRoomId());
            room = null;
        }
    }

    public String getGreenId() {
        return greenId;
    }

    public String getRedId() {
        return redId;
    }
}
