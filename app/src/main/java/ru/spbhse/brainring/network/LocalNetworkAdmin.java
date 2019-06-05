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

    static {
        HANDSHAKE = MessageGenerator.create().writeInt(Message.HANDSHAKE).toByteArray();
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
                Log.d("BrainRing", "Room was created");
            }

            @Override
            public void onJoinedRoom(int i, @Nullable Room room) {
                Log.d("BrainRing", "Joined room");
            }

            @Override
            public void onLeftRoom(int i, @NonNull String s) {
                Log.d("BrainRing", "Left room");
                if (!gameIsFinished) {
                    LocalController.finishLocalGame(true);
                }
            }

            @Override
            public void onRoomConnected(int code, @Nullable Room room) {
                Log.d("BrainRing", "Connected to room");
                if (room == null) {
                    Log.wtf("BrainRing", "onRoomConnected got null as room");
                    return;
                }
                LocalNetworkAdmin.this.room = room;
                if (code == GamesCallbackStatusCodes.OK) {
                    Log.d("BrainRing","Connected");
                } else {
                    Log.d("BrainRing","Connecting error");
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


    /**
     * Decodes byte message received by server and calls needed functions in LocalController
     * If it is a first message to server fills player's ids
     */
    @Override
    protected void onMessageReceived(@NonNull byte[] buf, @NonNull String userId) {
        if (gameIsFinished) {
            return;
        }
        Log.d("BrainRing", "Received message as admin!");
        if (!handshaked) {
            greenId = userId;
            handshaked = true;
            for (String id : room.getParticipantIds()) {
                if (!id.equals(myParticipantId) && !id.equals(greenId)) {
                    redId = id;
                    break;
                }
            }
            Log.d("BrainRing","Successful handshake");

            assert redId != null;
            LocalController.LocalNetworkAdminController.startGameCycle();
            return;
        }
        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(buf))) {
            int identifier = is.readInt();
            Log.d("BrainRing", "Identifier is " + identifier);

            switch(identifier) {
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

    /** Starts quick game with two auto matched players */
    @Override
    public void startQuickGame() {
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(LocalController.getJuryActivity(),
                googleSignInAccount);
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
        Log.d("BrainRing", "Start handshake");
        byte[] message = new byte[0];
        Log.d("BrainRing", "Writing message");
        sendMessageToOthers(message);
        Log.d("BrainRing", "Message sent");
    }

    public void regularHandshake() {
        sendMessageToOthers(HANDSHAKE);
    }

    @Override
    protected void leaveRoom() {
        if (room != null) {
            Log.d("BrainRing","Leaving room");
            Games.getRealTimeMultiplayerClient(LocalController.getJuryActivity(),
                    googleSignInAccount).leave(mRoomConfig, room.getRoomId());
            room = null;
        }
    }

    public String getGreenId() {
        //waitHandshake();
        return greenId;
    }

    public String getRedId() {
        //waitHandshake();
        return redId;
    }
}
