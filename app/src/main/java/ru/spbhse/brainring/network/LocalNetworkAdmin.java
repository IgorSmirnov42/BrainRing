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
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Class with methods to interact with network
 * Used by admin in a local network mode
 */
public class LocalNetworkAdmin extends LocalNetwork {
    private final Object handshakeBlock = new Object();
    private String redId;
    private String greenId;

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
                    Log.d("BrainRing","CONNECTED");
                } else {
                    Log.d("BrainRing","CONNECTING ERROR");
                }
                Games.getPlayersClient(Controller.getJuryActivity(), googleSignInAccount)
                        .getCurrentPlayerId()
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String myPlayerId) {
                                myParticipantId = room.getParticipantId(myPlayerId);
                            }
                        });
                Log.d("BrainRing", "Start handshake");
                handshake();
            }
        };
    }


    /**
     * Decodes byte message received by server and calls needed functions in Controller
     * If it is a first message to server fills player's ids
     */
    @Override
    protected void onMessageReceived(byte[] buf, String userId) {
        Log.d("BrainRing", "RECEIVED MESSAGE AS ADMIN!");
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
            Controller.LocalNetworkAdminController.startGameCycle();
            return;
        }
        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(buf))) {
            int identifier = is.readInt();
            System.out.println("IDENTIFIER IS" + identifier);

            if (identifier == Message.ANSWER_IS_READY) {
                Controller.LocalAdminLogicController.onAnswerIsReady(userId);
            } else {
                Log.wtf("BrainRing", "Unexpected message received");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: нормальная обработка
        }
    }

    /** Starts quick game with two auto matched players */
    @Override
    public void startQuickGame() {
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(Controller.getJuryActivity(),
                googleSignInAccount);
        final int MIN_OPPONENTS = 2, MAX_OPPONENTS = 2;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, ROLE_ADMIN);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(Controller.getJuryActivity(), googleSignInAccount)
                .create(mRoomConfig);
    }

    /**
     * Sends empty message to players in order to determine which of them is green/red
     * Waits while the answer isn't received
     * After execution starts game cycle
     */
    private void handshake() {
        byte[] message = new byte[20];
        Log.d("BrainRing", "Writing message");
        mRealTimeMultiplayerClient.sendUnreliableMessageToOthers(message, room.getRoomId());
        Log.d("BrainRing", "Message sent");
    }

    /** Blocking waiter for one's handshake response */
    private void waitHandshake() {
        if (!handshaked) {
            synchronized (handshakeBlock) {
                while (!handshaked) {
                    try {
                        handshakeBlock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
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
