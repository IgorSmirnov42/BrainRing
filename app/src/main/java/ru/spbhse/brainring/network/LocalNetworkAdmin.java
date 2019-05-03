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

public class LocalNetworkAdmin extends LocalNetwork {
    private final Object handshakeBlock = new Object();
    private String redId;
    private String greenId;

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
                    System.out.println("CONNECTED");
                } else {
                    System.out.println("ERROR WHILE CONNECTING");
                }
                Games.getPlayersClient(Controller.getGameActivity(), googleSignInAccount)
                        .getCurrentPlayerId()
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String myPlayerId) {
                                myParticipantId = room.getParticipantId(myPlayerId);
                            }
                        });
                handshake();
            }
        };
    }


    @Override
    protected void onMessageReceived(byte[] buf, String userId) {
        System.out.println("RECEIVED MESSAGE!");
        if (!handshaked) {
            synchronized (handshakeBlock) {
                greenId = userId;
                handshaked = true;
                for (String id : room.getParticipantIds()) {
                    if (!id.equals(myParticipantId) && !id.equals(greenId)) {
                        redId = id;
                        break;
                    }
                }
                handshakeBlock.notifyAll();
            }
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

    @Override
    public void startQuickGame() {
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(Controller.getGameActivity(),
                googleSignInAccount);
        final int MIN_OPPONENTS = 2, MAX_OPPONENTS = 2;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, ROLE_ADMIN);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(Controller.getGameActivity(), googleSignInAccount)
                .create(mRoomConfig);
    }

    private void handshake() {
        byte[] message = new byte[0];
        mRealTimeMultiplayerClient.sendUnreliableMessageToOthers(message, room.getRoomId());

        waitHandshake();

        assert redId != null;
        Controller.LocalNetworkAdminController.startGameCycle();
    }

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
