package ru.spbhse.brainring.network;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.network.messages.Message;

/** Class for working with network in online mode */
public class Network {
    private RoomConfig mRoomConfig;
    private Room room;
    private boolean isServer;
    private String serverId;
    public GoogleSignInAccount googleSignInAccount;
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient;
    private String myParticipantId;
    private CountDownTimer handshakeTimer;
    private static final int HANDSHAKE_TIME = 5000;
    private static final int MAXIMUM_TIME_WITHOUT_MESSAGES = 80 * 1000;
    private static CountDownTimer timer;
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
            leaveRoom();
        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            Log.d("BrainRing", "onConnectedToRoom");
        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            Log.d("BrainRing", "onDisconnectedFromRoom");
            leaveRoom();
        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeersConnected");
        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeersDisconnected");
            leaveRoom();
        }

        @Override
        public void onP2PConnected(@NonNull String s) {
            Log.d("BrainRing", "onP2PConnected");
        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {
            Log.d("BrainRing", "onP2PDisconnected " + s);
            leaveRoom();
        }
    };
    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int i, @Nullable Room room) {
            Log.d("BrainRing", "Room was created");
            startNewTimer();
        }

        @Override
        public void onJoinedRoom(int i, @Nullable Room room) {
            Log.d("BrainRing", "Joined room");
        }

        @Override
        public void onLeftRoom(int i, @NonNull String s) {
            Log.d("BrainRing", "Left room");
            OnlineController.finishOnlineGame();
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
                Log.d("BrainRing","Connected");
            } else {
                Log.d("BrainRing","Error during connecting");
            }
            serverId = Collections.min(room.getParticipantIds());

            Games.getPlayersClient(OnlineController.getOnlineGameActivity(), googleSignInAccount)
                    .getCurrentPlayerId()
                    .addOnSuccessListener(myPlayerId -> {
                        myParticipantId = room.getParticipantId(myPlayerId);
                        Log.d("BrainRing", "Received participant id");
                        if (myParticipantId.equals(serverId)) {
                            isServer = true;
                            Log.d("BrainRing", "I am server");
                            OnlineController.startOnlineGame();
                        }
                    });
        }
    };
    private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = realTimeMessage -> {
        Log.d("BrainRing","Received message");
        byte[] buf = realTimeMessage.getMessageData();
        onMessageReceived(buf, realTimeMessage.getSenderParticipantId());
    };

    public void leaveRoom() {
        if (room != null) {
            Games.getRealTimeMultiplayerClient(OnlineController.getOnlineGameActivity(),
                    googleSignInAccount).leave(mRoomConfig, room.getRoomId());
            room = null;
        }
    }

    /** Reacts on received message */
    private void onMessageReceived(byte[] buf, @NonNull String userId) {
        if (timer != null && !userId.equals(myParticipantId)) {
            timer.cancel();
            startNewTimer();
        }
        Log.d("BrainRing","Received message! User id is " + userId);
        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(buf))) {
            int identifier = is.readInt();
            Log.d("BrainRing","Identifier is " + identifier);

            switch (identifier) {
                case Message.ANSWER_IS_READY:
                    long time = is.readLong();
                    OnlineController.OnlineAdminLogicController.onAnswerIsReady(userId, time);
                    break;
                case Message.ANSWER_IS_WRITTEN:
                    String answer = Message.readString(is);
                    OnlineController.OnlineAdminLogicController.onAnswerIsWritten(answer, userId);
                    break;
                case Message.FORBIDDEN_TO_ANSWER:
                    OnlineController.OnlineUserLogicController.onForbiddenToAnswer();
                    break;
                case Message.ALLOWED_TO_ANSWER:
                    OnlineController.OnlineUserLogicController.onAllowedToAnswer();
                    break;
                case Message.SENDING_QUESTION:
                    String question = Message.readString(is);
                    OnlineController.OnlineUserLogicController.onReceivingQuestion(question);
                    break;
                case Message.SENDING_INCORRECT_OPPONENT_ANSWER:
                    String opponentAnswer = Message.readString(is);
                    OnlineController.OnlineUserLogicController.onIncorrectOpponentAnswer(opponentAnswer);
                    break;
                case Message.SENDING_CORRECT_ANSWER_AND_SCORE:
                    int firstUserScore = is.readInt();
                    int secondUserScore = is.readInt();
                    String correctAnswer = Message.readString(is);
                    OnlineController.OnlineUserLogicController.onReceivingAnswer(firstUserScore, secondUserScore, correctAnswer);
                    break;
                case Message.OPPONENT_IS_ANSWERING:
                    OnlineController.OnlineUserLogicController.onOpponentIsAnswering();
                    break;
                case Message.TIME_START:
                    OnlineController.OnlineUserLogicController.onTimeStart();
                    break;
                case Message.FALSE_START:
                    OnlineController.OnlineAdminLogicController.onFalseStart(userId);
                    break;
                case Message.HANDSHAKE:
                    if (isServer) {
                        continueGame();
                    } else {
                        Log.wtf("BrainRing", "Unexpected message");
                    }
                    break;
                case Message.TIME_LIMIT:
                    long roundNumber = is.readLong();
                    OnlineController.OnlineAdminLogicController.onTimeLimit(roundNumber, userId);
                    break;
                case Message.FINISH:
                    OnlineController.finishOnlineGame();
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startNewTimer() {
        timer = new CountDownTimer(MAXIMUM_TIME_WITHOUT_MESSAGES, MAXIMUM_TIME_WITHOUT_MESSAGES) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (timer == this) {
                    sendReliableMessageToAll(Message.generateMessage(Message.FINISH, ""));
                    OnlineController.finishOnlineGame();
                }
            }
        };
        timer.start();
    }

    private void continueGame() {
        if (handshakeTimer != null) {
            handshakeTimer.cancel();
            handshakeTimer = null;
            OnlineController.OnlineAdminLogicController.publishing();
        }
    }

    /** Starts quick game with 1 auto matched player */
    public void startQuickGame() {
        isServer = false;
        // quick-start a game with 1 randomly selected opponent
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(OnlineController.getOnlineGameActivity(),
                googleSignInAccount);
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        Games.getRealTimeMultiplayerClient(OnlineController.getOnlineGameActivity(), googleSignInAccount)
                .create(mRoomConfig);
    }

    /** Sends message to all users in a room (and to itself). Guarantees delivering. May be slow... */
    public void sendReliableMessageToAll(byte[] message) {
        for (String participantId : room.getParticipantIds()) {
            sendReliableMessageToConcreteUser(participantId, message);
        }
    }


    /** Sends message to user with given id. Guarantees delivering. May be slow... */
    public void sendReliableMessageToConcreteUser(String userId, byte[] message) {
        if (myParticipantId == null || userId == null || room == null) {
            Log.e("BrainRing", "Cannot send message before initialization");
            return;
        }
        if (userId.equals(myParticipantId)) {
            onMessageReceived(message, myParticipantId);
        } else {
            mRealTimeMultiplayerClient.sendReliableMessage(message, room.getRoomId(), userId, (i, i1, s) -> {
            });
        }
    }

    public void sendQuestion(byte[] message) {
        handshakeTimer = new CountDownTimer(HANDSHAKE_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (handshakeTimer == this) {
                    sendReliableMessageToAll(message);
                }
            }

            @Override
            public void onFinish() {
                if (handshakeTimer == this) {
                    Log.wtf("BrainRing", "Unsuccessful handshake");
                    OnlineController.finishOnlineGame();
                }
            }
        };
        handshakeTimer.start();
    }

    public void sendReliableMessageToServer(byte[] message) {
        if (isServer) {
            onMessageReceived(message, myParticipantId);
        } else {
            sendReliableMessageToConcreteUser(serverId, message);
        }
    }

    public String getMyParticipantId() {
        return myParticipantId;
    }

    public boolean iAmServer() {
        return isServer;
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
