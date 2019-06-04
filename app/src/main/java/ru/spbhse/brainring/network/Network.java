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
import ru.spbhse.brainring.network.messages.MessageGenerator;

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
    private boolean firstMessage = true;
    private static final int HANDSHAKE_TIME = 2000;
    private static final int FIRST_HANDSHAKE_TIME = 5000; // first message may take longer time
    private static final int MAXIMUM_TIME_WITHOUT_MESSAGES = 80 * 1000;
    private static CountDownTimer timer;
    private static final byte[] FINISH;
    private boolean gameIsFinished = false;
    private boolean gameIsStarted = false;
    private int counterOfConnections = 0;
    private static final int TIMES_TO_SEND = 10000;
    private long handshakeStartTime;

    static {
        FINISH = MessageGenerator.create().writeInt(Message.FINISH).toByteArray();
    }

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
            if (!gameIsFinished) {
                OnlineController.finishOnlineGame(true);
            }
        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            Log.d("BrainRing", "onConnectedToRoom");
        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            Log.d("BrainRing", "onDisconnectedFromRoom");
            if (!gameIsFinished) {
                OnlineController.finishOnlineGame(true);
            }
        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeersConnected");
        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d("BrainRing", "onPeersDisconnected");
            if (!gameIsFinished) {
                OnlineController.finishOnlineGame(true);
            }
        }

        @Override
        public void onP2PConnected(@NonNull String s) {
            Log.d("BrainRing", "onP2PConnected " + s);
            ++counterOfConnections;
            if (counterOfConnections == 2 && isServer) {
                OnlineController.startOnlineGame();
            }
        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {
            Log.d("BrainRing", "onP2PDisconnected " + s);
            if (!gameIsFinished) {
                OnlineController.finishOnlineGame(true);
            }
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
            if (!gameIsFinished) {
                OnlineController.finishOnlineGame(true);
            }
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
                        walkRoomMembers();
                        if (myParticipantId.equals(serverId)) {
                            isServer = true;
                            Log.d("BrainRing", "I am server");
                            ++counterOfConnections;
                            if (counterOfConnections == 2) {
                                OnlineController.startOnlineGame();
                            }
                        }
                    });
        }
    };
    private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = realTimeMessage -> {
        Log.d("BrainRing","Received message");
        byte[] buf = realTimeMessage.getMessageData();
        onMessageReceived(buf, realTimeMessage.getSenderParticipantId());
    };

    private void walkRoomMembers() {
        Log.d("BrainRing", "Start printing room members");
        if (room != null) {
            for (Participant participant : room.getParticipants()) {
                if (participant.getParticipantId().equals(myParticipantId)) {
                    OnlineController.OnlineUIController.setMyNick(participant.getDisplayName());
                } else {
                    OnlineController.OnlineUIController.setOpponentNick(participant.getDisplayName());
                }
                Log.d("BrainRing", participant.getDisplayName());
            }
        }
        Log.d("BrainRing", "Finish printing room members");
    }

    private void leaveRoom() {
        if (room != null) {
            Games.getRealTimeMultiplayerClient(OnlineController.getOnlineGameActivity(),
                    googleSignInAccount).leave(mRoomConfig, room.getRoomId());
            room = null;
        }
    }

    public void finish() {
        gameIsFinished = true;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (handshakeTimer != null) {
            handshakeTimer.cancel();
            handshakeTimer = null;
        }
        leaveRoom();
        updateRating();
    }

    /** Reacts on received message */
    private void onMessageReceived(@NonNull byte[] buf, @NonNull String userId) {
        if (gameIsFinished) {
            Log.e("BrainRing", "received message but game is over");
            return;
        }
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
                    String answer = is.readUTF();
                    OnlineController.OnlineAdminLogicController.onAnswerIsWritten(answer, userId);
                    break;
                case Message.FORBIDDEN_TO_ANSWER:
                    OnlineController.OnlineUserLogicController.onForbiddenToAnswer();
                    break;
                case Message.ALLOWED_TO_ANSWER:
                    OnlineController.OnlineUserLogicController.onAllowedToAnswer();
                    break;
                case Message.SENDING_QUESTION:
                    int questionId = is.readInt();
                    String question = is.readUTF();
                    OnlineController.OnlineUserLogicController.onReceivingQuestion(questionId, question);
                    break;
                case Message.SENDING_INCORRECT_OPPONENT_ANSWER:
                    String opponentAnswer = is.readUTF();
                    OnlineController.OnlineUserLogicController.onIncorrectOpponentAnswer(opponentAnswer);
                    break;
                case Message.SENDING_CORRECT_ANSWER_AND_SCORE:
                    String correctAnswer = is.readUTF();
                    String comment = is.readUTF();
                    int firstUserScore = is.readInt();
                    int secondUserScore = is.readInt();
                    String questionMessage = is.readUTF();
                    OnlineController.OnlineUserLogicController.onReceivingAnswer(firstUserScore,
                            secondUserScore, correctAnswer, comment, questionMessage);
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
                    int roundNumber = is.readInt();
                    OnlineController.OnlineAdminLogicController.onTimeLimit(roundNumber, userId);
                    break;
                case Message.FINISH:
                    if (!gameIsFinished) {
                        OnlineController.finishOnlineGame(true);
                    }
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
                    sendMessageToAll(FINISH);
                }
            }
        };
        timer.start();
    }

    private void continueGame() {
        if (handshakeTimer != null) {
            handshakeTimer.cancel();
            handshakeTimer = null;
            Log.d("BrainRing", "Successful handshake. Took "
                    + (System.currentTimeMillis() - handshakeStartTime) + "ms");
            OnlineController.OnlineAdminLogicController.publishing();
        }
    }

    public void onSignedIn(@NonNull GoogleSignInAccount signInAccount) {
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

    /** Sends message to all users in a room (and to itself). Guarantees delivering */
    public void sendMessageToAll(@NonNull byte[] message) {
        sendMessageToConcreteUser(getOpponentParticipantId(), message);
        onMessageReceived(message, getMyParticipantId());
    }

    public void sendMessageToConcreteUser(@NonNull String userId, @NonNull byte[] message) {
        if (myParticipantId == null || room == null) {
            Log.e("BrainRing", "Cannot send message before initialization");
            return;
        }
        if (userId.equals(myParticipantId)) {
            Log.d("BrainRing", "Sending message to myself");
            onMessageReceived(message, myParticipantId);
        } else {
            Log.d("BrainRing", "Start sending message to " + userId);
            sendMessageToConcreteUserNTimes(userId, message, TIMES_TO_SEND);
        }
    }

    private void sendMessageToConcreteUserNTimes(@NonNull String userId, @NonNull byte[] message,
                                          int timesToSend) {
        if (gameIsFinished) {
            return;
        }
        if (timesToSend < 0) {
            Log.wtf("BrainRing", "Failed to send message too many times. Finish game");
            OnlineController.finishOnlineGame(true);
            return;
        }
        mRealTimeMultiplayerClient.sendReliableMessage(message, room.getRoomId(), userId, (i, i1, s) -> {
            if (i != GamesCallbackStatusCodes.OK) {

                Log.e("BrainRing", "Failed to send message. Left " + timesToSend + " tries\n" +
                                "Error is " + GamesCallbackStatusCodes.getStatusCodeString(i));
                sendMessageToConcreteUserNTimes(userId, message, timesToSend - 1);
            } else {
                Log.d("BrainRing", "Message to " + userId + " is delivered. Took " +
                        (TIMES_TO_SEND - timesToSend + 1) + " tries");
            }
        });
    }

    public void sendQuestion(@NonNull byte[] message) {
        sendMessageToAll(message);
        int handshakeTime = HANDSHAKE_TIME;
        if (firstMessage) {
            handshakeTime = FIRST_HANDSHAKE_TIME;
            firstMessage = false;
        }
        handshakeTimer = new CountDownTimer(handshakeTime, handshakeTime) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (handshakeTimer == this) { // check in case message was delivered right before finish
                    Log.wtf("BrainRing", "Unsuccessful handshake");
                    OnlineController.finishOnlineGame(true);
                }
            }
        };
        handshakeStartTime = System.currentTimeMillis();
        handshakeTimer.start();
    }

    public String getParticipantName(@NonNull String userId) {
        for (Participant participant : room.getParticipants()) {
            if (participant.getParticipantId().equals(userId)) {
                return participant.getDisplayName();
            }
        }
        return null;
    }

    public void sendMessageToServer(@NonNull byte[] message) {
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
