package ru.spbhse.brainring.network;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
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

    public RoomConfig mRoomConfig;
    public Room room;
    public boolean isServer;
    public GoogleSignInAccount googleSignInAccount;
    public RealTimeMultiplayerClient mRealTimeMultiplayerClient;
    public GoogleSignInClient mGoogleSignInClient;
    public RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        @Override
        public void onRoomConnecting(@Nullable Room room) {

        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {

        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {

        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {

        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onP2PConnected(@NonNull String s) {

        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {

        }
    };
    public RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int i, @Nullable Room room) {
            Controller.setLocation(1);
            Controller.setQuestionText("ROOM CREATED");
        }

        @Override
        public void onJoinedRoom(int i, @Nullable Room room) {
        }

        @Override
        public void onLeftRoom(int i, @NonNull String s) {

        }

        @Override
        public void onRoomConnected(int code, @Nullable Room room) {
            if (room == null) {
                Log.wtf("BrainRing", "onRoomConnected got null as room");
                return;
            }
            Controller.setLocation(1);
            Controller.setQuestionText("УРАААА! МЫ СКОННЕКТИЛИСЬ С КЕМ-ТО (onRoomConnected)!!!");
            Network.this.room = room;
            if (code == GamesCallbackStatusCodes.OK) {
                System.out.println("CONNECTED");
            } else {
                System.out.println("ERROR WHILE CONNECTING");
            }
            String minimalId = Collections.min(room.getParticipantIds());
            Games.getPlayersClient(Controller.gameActivity, googleSignInAccount)
                    .getCurrentPlayerId()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String myPlayerId) {
                            String myId = room.getParticipantId(myPlayerId);
                            Controller.setQuestionText(myId);
                            if (myId.equals(minimalId)) {
                                isServer = true;
                                Controller.setQuestionText("Я сервер\n");
                                // START GAME
                            }
                        }
                    });
        }
    };
    public OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            onMessageReceived(buf);
        }
    };

    private void onMessageReceived(byte[] buf) {
        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(buf))) {
            int identifier = is.readInt();

            if (Message.messageIsToServer(identifier) && !isServer) {
                Log.wtf("BrainRing", "Not server got message to server\n");
                return;
            }

            switch (identifier) {
                case Message.ANSWER_IS_READY:
                    String clientId = Message.readString(is);
                    Controller.LogicController.onAnswerIsReady(clientId);
                    break;
                case Message.ANSWER_IS_WRITTEN:
                    String answer = Message.readString(is);
                    Controller.LogicController.onAnswerIsWritten(answer);
                    break;
                case Message.FORBIDDEN_TO_ANSWER:
                    Controller.LogicController.onForbiddenToAnswer();
                    break;
                case Message.ALLOWED_TO_ANSWER:
                    Controller.LogicController.onAllowedToAnswer();
                    break;
                case Message.SENDING_QUESTION:
                    String question = Message.readString(is);
                    Controller.LogicController.onReceivingQuestion(question);
                    break;
                case Message.SENDING_INCORRECT_OPPONENT_ANSWER:
                    String opponentAnswer = Message.readString(is);
                    Controller.LogicController.onIncorrectOpponentAnswer(opponentAnswer);
                    break;
                case Message.SENDING_CORRECT_ANSWER_AND_SCORE:
                    int firstUserScore = is.readInt();
                    int secondUserScore = is.readInt();
                    String correctAnswer = Message.readString(is);
                    Controller.LogicController.onReceivingAnswer(firstUserScore, secondUserScore, correctAnswer);
                    break;
                case Message.OPPONENT_IS_ANSWERING:
                    Controller.LogicController.onOpponentIsAnswering();
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Network() {}

    public void startQuickGame() {
        isServer = false;
        // quick-start a game with 1 randomly selected opponent
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(Controller.gameActivity,
                googleSignInAccount);
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(Controller.gameActivity, googleSignInAccount)
                .create(mRoomConfig);
    }

    public void sendMessageToAll(byte[] message) {
        mRealTimeMultiplayerClient.sendUnreliableMessageToOthers(message, room.getRoomId());
    }

    public void sendMessageToConcreteUser(String userId, byte[] message) {
        mRealTimeMultiplayerClient.sendUnreliableMessage(message, room.getRoomId(), userId);
    }

    public void sendMessageToServer(byte[] message) {
        if (isServer) {
            onMessageReceived(message);
        } else {
            sendMessageToConcreteUser(room.getCreatorId(), message);
        }
    }
}
