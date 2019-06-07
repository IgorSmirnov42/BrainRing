package ru.spbhse.brainring.network;

import android.os.Bundle;
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
 * Used by player in a local network mode
 */
public class LocalNetworkPlayer extends LocalNetwork {
    private String serverId;
    /** Green or red table. Values are written in base class */
    private int myColor;

    /**
     * Creates new instance of LocalNetworkPlayer.
     * @param myColor string "red" or "green"
     */
    public LocalNetworkPlayer(@NonNull String myColor) {
        super();

        if (myColor.equals("green")) {
            this.myColor = ROLE_GREEN;
        } else {
            this.myColor = ROLE_RED;
        }

        mRoomUpdateCallback = new RoomUpdateCallback() {
            @Override
            public void onRoomCreated(int i, @Nullable Room room) {
                Log.d(Controller.APP_TAG, "Room was created");
                LocalNetworkPlayer.this.room = room;
            }

            @Override
            public void onJoinedRoom(int i, @Nullable Room room) {
                Log.d(Controller.APP_TAG, "Joined room");
                LocalNetworkPlayer.this.room = room;
            }

            @Override
            public void onLeftRoom(int i, @NonNull String s) {
                Log.d(Controller.APP_TAG, "Left room");
                if (!gameIsFinished) {
                    LocalController.finishLocalGame(true);
                }
            }

            @Override
            public void onRoomConnected(int code, @Nullable Room room) {
                Log.d(Controller.APP_TAG, "Connected to room");
                if (room == null) {
                    Log.wtf(Controller.APP_TAG, "onRoomConnected got null as room");
                    return;
                }
                LocalNetworkPlayer.this.room = room;
                if (code == GamesCallbackStatusCodes.OK) {
                    Log.d(Controller.APP_TAG,"Connected");
                } else {
                    Log.d(Controller.APP_TAG,"Error during connecting");
                }
            }
        };
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

        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(buf))) {
            int identifier = is.readInt();
            Log.d(Controller.APP_TAG,"Identifier is " + identifier);

            switch (identifier) {
                case Message.INITIAL_HANDSHAKE:
                    doInitialHandshake(userId);
                    break;
                case Message.FORBIDDEN_TO_ANSWER:
                    LocalController.LocalPlayerLogicController.onForbiddenToAnswer();
                    break;
                case Message.ALLOWED_TO_ANSWER:
                    LocalController.LocalPlayerLogicController.onAllowedToAnswer();
                    break;
                case Message.FALSE_START:
                    LocalController.LocalPlayerLogicController.onFalseStart();
                    break;
                case Message.TIME_START:
                    LocalController.LocalPlayerLogicController.onTimeStart();
                    break;
                case Message.HANDSHAKE:
                    sendMessageToConcreteUser(userId, buf);
                    break;
                default:
                    Log.wtf(Controller.APP_TAG, "Unexpected message received");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doInitialHandshake(@NonNull String serverId) {
        this.serverId = serverId;
        handshaked = true;
        if (myColor == ROLE_GREEN) {
            Log.d(Controller.APP_TAG, "I am green");
            sendMessageToConcreteUser(serverId,
                    MessageGenerator.create()
                    .writeInt(Message.I_AM_GREEN)
                    .toByteArray()
            );
        } else {
            Log.d(Controller.APP_TAG, "I am red");
            sendMessageToConcreteUser(serverId,
                    MessageGenerator.create()
                            .writeInt(Message.I_AM_RED)
                            .toByteArray()
            );
        }
    }

    /** Starts quick game with auto matched server and player */
    @Override
    public void startQuickGame() {
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(
                LocalController.getPlayerActivity(),
                googleSignInAccount);
        final int MIN_OPPONENTS = 2, MAX_OPPONENTS = 2;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, myColor);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(LocalController.getPlayerActivity(), googleSignInAccount)
                .create(mRoomConfig);
    }

    /**
     * Sends message to server
     * If server is not known, does nothing
     */
    public void sendMessageToServer(@NonNull byte[] message) {
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
            Games.getRealTimeMultiplayerClient(LocalController.getPlayerActivity(),
                    googleSignInAccount).leave(mRoomConfig, room.getRoomId());
            room = null;
        }
    }

    @Override
    protected void handshake() {
        Log.wtf(Controller.APP_TAG, "Handshake was called for player");
    }
}
