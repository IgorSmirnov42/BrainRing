package ru.spbhse.brainring.network;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.multiplayer.Participant;
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

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.network.messages.Message;

import static com.google.android.gms.games.leaderboard.LeaderboardVariant.COLLECTION_PUBLIC;
import static com.google.android.gms.games.leaderboard.LeaderboardVariant.TIME_SPAN_ALL_TIME;

/** Class for working with network in online mode */
public class Network {
    private RoomConfig mRoomConfig;
    private Room room;
    private boolean isServer;
    private String serverId;
    private GoogleSignInAccount googleSignInAccount;
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient;
    private String myParticipantId;
    private CountDownTimer handshakeTimer;
    private LeaderboardsClient leaderboardsClient;
    private long scoreSum;
    private static final int HANDSHAKE_TIME = 1000;
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
            printRoomMembers();
        }
    };
    private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = realTimeMessage -> {
        Log.d("BrainRing","Received message");
        byte[] buf = realTimeMessage.getMessageData();
        onMessageReceived(buf, realTimeMessage.getSenderParticipantId());
    };

    private void printRoomMembers() {
        Log.d("BrainRing", "Start printing room members");
        if (room != null) {
            for (Participant participant : room.getParticipants()) {
                Log.d("BrainRing", participant.getDisplayName());
            }
        }
        Log.d("BrainRing", "Finish printing room members");
    }

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
                case Message.CORRECT_ANSWER:
                    ++scoreSum;
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateRating() {
        if (leaderboardsClient == null || scoreSum == -1) {
            return;
        }
        Log.d("BrainRing", "Updating rating");
        leaderboardsClient.submitScoreImmediate(
                OnlineController.getOnlineGameActivity().getString(R.string.leaderboard), scoreSum);
    }

    private void startNewTimer() {
        timer = new CountDownTimer(MAXIMUM_TIME_WITHOUT_MESSAGES, MAXIMUM_TIME_WITHOUT_MESSAGES) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (timer == this) {
                    sendMessageToAll(Message.generateMessage(Message.FINISH, ""));
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

    public void onSignedIn(GoogleSignInAccount signInAccount) {
        googleSignInAccount = signInAccount;
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(OnlineController.getOnlineGameActivity(),
                googleSignInAccount);
        leaderboardsClient = Games.getLeaderboardsClient(OnlineController.getOnlineGameActivity(),
                googleSignInAccount);
        leaderboardsClient.loadCurrentPlayerLeaderboardScore(
                OnlineController.getOnlineGameActivity().getString(R.string.leaderboard),
                TIME_SPAN_ALL_TIME, COLLECTION_PUBLIC).addOnSuccessListener(leaderboardScoreAnnotatedData -> {
                    Log.d("BrainRing", "Got score");
                    LeaderboardScore score = leaderboardScoreAnnotatedData.get();
                    if (score != null) {
                        scoreSum = score.getRawScore();
                    } else {
                        scoreSum = 0;
                    }
                    Log.d("BrainRing", "Score is " + scoreSum);
                    OnlineController.NetworkController.startGame();
                });
    }

    /** Starts quick game with 1 auto matched player */
    public void startQuickGame() {
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    /** Sends message to all users in a room (and to itself). Guarantees delivering. May be slow... */
    public void sendMessageToAll(byte[] message) {
        for (String participantId : room.getParticipantIds()) {
            sendMessageToConcreteUser(participantId, message);
        }
    }

    /** Sends message to user with given id. Guarantees delivering. May be slow... */
    public void sendMessageToConcreteUser(String userId, byte[] message) {
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
        sendMessageToAll(message);
        handshakeTimer = new CountDownTimer(HANDSHAKE_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                //if (handshakeTimer == this) {
                    Log.wtf("BrainRing", "Unsuccessful handshake");
                    OnlineController.finishOnlineGame();
                //}
            }
        };
        handshakeTimer.start();
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
