package ru.spbhse.brainring.network;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;

public class Network {

    private RoomConfig mRoomConfig;
    private Room room;
    private boolean isServer;
    private String serverId;
    public GoogleSignInAccount googleSignInAccount;
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient;
    private String myParticipantId;
    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        @Override
        public void onRoomConnecting(@Nullable Room room) {
            Log.d("BrainRing", "onRoomConnecting");
        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {
            Log.d("BrainRing", "onRoomAutoMatching");
        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeerInvitedToRoom");
        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeerDeclined");
        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeerJoined");
        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeerLeft");
        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            Log.d("BrainRing", "onConnectedToRoom");
        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            Log.d("BrainRing", "onDisconnectedFromRoom");
        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeersConnected");
        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeersDisconnected");
        }

        @Override
        public void onP2PConnected(@NonNull String s) {
            Log.d("BrainRing", "onP2PConnected");
        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {
            Log.d("BrainRing", "onP2PDisconnected");
        }
    };
    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
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
            Network.this.room = room;
            if (code == GamesCallbackStatusCodes.OK) {
                System.out.println("CONNECTED");
            } else {
                System.out.println("ERROR WHILE CONNECTING");
            }
            String minimalId = Collections.min(room.getParticipantIds());
            serverId = minimalId;
            Games.getPlayersClient(Controller.getGameActivity(), googleSignInAccount)
                    .getCurrentPlayerId()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String myPlayerId) {
                            myParticipantId = room.getParticipantId(myPlayerId);
                            if (myParticipantId.equals(minimalId)) {
                                isServer = true;
                                Controller.startOnlineGame();
                            }
                        }
                    });
        }
    };
    public OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            onMessageReceived(buf, realTimeMessage.getSenderParticipantId());
        }
    };

    private void onMessageReceived(byte[] buf, String userId) {
        System.out.println("RECEIVED MESSAGE!");
        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(buf))) {
            int identifier = is.readInt();
            System.out.println("IDENTIFIER IS" + identifier);

            if (Message.messageIsToServer(identifier) && !isServer) {
                Log.wtf("BrainRing", "Not server got message to server\n");
                return;
            }

            switch (identifier) {
                case Message.ANSWER_IS_READY:
                    Controller.OnlineAdminLogicController.onAnswerIsReady(userId);
                    break;
                case Message.ANSWER_IS_WRITTEN:
                    String answer = Message.readString(is);
                    Controller.OnlineAdminLogicController.onAnswerIsWritten(answer);
                    break;
                case Message.FORBIDDEN_TO_ANSWER:
                    Controller.OnlineUserLogicController.onForbiddenToAnswer();
                    break;
                case Message.ALLOWED_TO_ANSWER:
                    Controller.OnlineUserLogicController.onAllowedToAnswer();
                    break;
                case Message.SENDING_QUESTION:
                    String question = Message.readString(is);
                    Controller.OnlineUserLogicController.onReceivingQuestion(question);
                    break;
                case Message.SENDING_INCORRECT_OPPONENT_ANSWER:
                    String opponentAnswer = Message.readString(is);
                    Controller.OnlineUserLogicController.onIncorrectOpponentAnswer(opponentAnswer);
                    break;
                case Message.SENDING_CORRECT_ANSWER_AND_SCORE:
                    int firstUserScore = is.readInt();
                    int secondUserScore = is.readInt();
                    String correctAnswer = Message.readString(is);
                    Controller.OnlineUserLogicController.onReceivingAnswer(firstUserScore, secondUserScore, correctAnswer);
                    break;
                case Message.OPPONENT_IS_ANSWERING:
                    Controller.OnlineUserLogicController.onOpponentIsAnswering();
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: нормальная обработка
        }
    }

    public Network() {}

    public void startQuickGame() {
        isServer = false;
        // quick-start a game with 1 randomly selected opponent
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(Controller.getGameActivity(),
                googleSignInAccount);
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(Controller.getGameActivity(), googleSignInAccount)
                .create(mRoomConfig);
    }

    public void sendMessageToAll(byte[] message) {
        mRealTimeMultiplayerClient.sendUnreliableMessageToOthers(message, room.getRoomId());
        onMessageReceived(message, myParticipantId);
    }

    public void sendMessageToConcreteUser(String userId, byte[] message) {
        if (userId.equals(myParticipantId)) {
            onMessageReceived(message, myParticipantId);
        } else {
            mRealTimeMultiplayerClient.sendUnreliableMessage(message, room.getRoomId(), userId);
        }
    }

    public void sendMessageToServer(byte[] message) {
        if (isServer) {
            onMessageReceived(message, myParticipantId);
        } else {
            sendMessageToConcreteUser(serverId, message);
        }
    }

    public String getMyParticipantId() {
        return myParticipantId;
    }

    public String getOpponentParticipantId() {
        for (String participantId : room.getParticipantIds()) {
            if (!participantId.equals(myParticipantId)) {
                return participantId;
            }
        }
        Log.wtf("BrainRing", "Opponent id was not found.");
        return null;
    }
}
