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

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;

public class LocalNetworkPlayer extends LocalNetwork {
    private String serverId;
    private int myColor;

    public LocalNetworkPlayer(String myColor) {
        super();

        if (myColor.equals("green")) {
            this.myColor = ROLE_GREEN;
        } else {
            this.myColor = ROLE_RED;
        }

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
                LocalNetworkPlayer.this.room = room;
                if (code == GamesCallbackStatusCodes.OK) {
                    System.out.println("CONNECTED");
                } else {
                    System.out.println("ERROR WHILE CONNECTING");
                }
            }
        };
    }

    @Override
    protected void onMessageReceived(byte[] buf, String userId) {
        if (!handshaked) {
            handshaked = true;
            serverId = userId;
            if (myColor == ROLE_GREEN) {
                sendMessageToConcreteUser(userId, buf);
            }
            return;
        }

        System.out.println("RECEIVED MESSAGE!");
        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(buf))) {
            int identifier = is.readInt();
            System.out.println("IDENTIFIER IS" + identifier);

            if (Message.messageIsToServer(identifier)) {
                Log.wtf("BrainRing", "Client got message to server\n");
                return;
            }

            switch (identifier) {
                case Message.FORBIDDEN_TO_ANSWER:
                    Controller.UserLogicController.onForbiddenToAnswer();
                    break;
                case Message.ALLOWED_TO_ANSWER:
                    Controller.UserLogicController.onAllowedToAnswer();
                    break;
                default:
                    Log.wtf("BrainRing", "Unexpected message received");
            }

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: нормальная обработка
        }
    }

    @Override
    public void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(Controller.getGameActivity(),
                googleSignInAccount);
        final int MIN_OPPONENTS = 2, MAX_OPPONENTS = 2;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, myColor);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(Controller.getGameActivity(), googleSignInAccount)
                .create(mRoomConfig);
    }

    public void sendMessageToServer(byte[] message) {
        sendMessageToConcreteUser(serverId, message);
    }
}
