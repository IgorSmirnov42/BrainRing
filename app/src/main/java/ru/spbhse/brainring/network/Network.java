package ru.spbhse.brainring.network;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.managers.OnlineGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.OnlineFinishCodes;
import ru.spbhse.brainring.network.messages.messageTypes.FinishMessage;
import ru.spbhse.brainring.network.messages.messageTypes.QuestionMessage;

import static com.google.android.gms.games.leaderboard.LeaderboardVariant.COLLECTION_PUBLIC;
import static com.google.android.gms.games.leaderboard.LeaderboardVariant.TIME_SPAN_ALL_TIME;

/** Class for working with network in online mode */
public class Network {
    private OnlineGameManager manager;
    private RoomConfig mRoomConfig;
    private Room room;
    private boolean isServer;
    private String serverId;
    private GoogleSignInAccount googleSignInAccount;
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient;
    private String myParticipantId;
    /**
     * Timer that counts {@code HANDSHAKE_TIME} ms (or {@code FIRST_HANDSHAKE_TIME} if it is first
     *      handshake in a game and panics if haven't received handshake message by that time.
     * Handshake messages are sent by server before each round to check if opponent is connected
     */
    private CountDownTimer handshakeTimer;
    private LeaderboardsClient leaderboardsClient;
    /**
     * Number of points in all online games earned by this player.
     * Increments on every right answer in current game and then sends to leaderboard
     * In the beginning of the game downloaded from the leaderboard
     */
    private long scoreSum;
    /** Flag that determines whether any message was already sent */
    private boolean firstMessage = true;
    /**
     * Time that handshake longs.
     * It must be the time that message may go from server to client and back
     * Reasonably to make it {@code DELIVERING_FAULT_MILLIS} (from {@code OnlineGameAdminLogic})
     *          multiplied by 2
     */
    private static final int HANDSHAKE_TIME = 2000;
    /**
     * First message takes much longer time if server and client are connected in different networks
     * So time for first handshake should be longer too
     */
    private static final int FIRST_HANDSHAKE_TIME = 5000; // first message may take longer time
    /**
     * If server doesn't receive any messages during that time it panics and finishes game
     * This time is bigger than a longest time without messages from concrete user, so if timer
     *      panics then definitely something gone wrong
     */
    private static final int MAXIMUM_TIME_WITHOUT_MESSAGES = 80 * 1000;
    /**
     * Timer that checks if last message from opponent was too much time ago
     * Also determines whether online opponent was not found
     */
    private static CountDownTimer timer;
    private boolean gameIsFinished = false;
    /**
     * Game should start only if {@code onRoomConnected} and {@code onP2PConnected were called}
     * This is the counter to determine if both of them were called
     */
    private int counterOfConnections = 0;
    /** Number of tries that should be done to deliver a message that was failed to deliver */
    private static final int TIMES_TO_SEND = 100;
    /**
     * We can receive some of room disconnected messages before the message with real game finish
     *      reason comes.
     * So we have to wait {@code WAIT_FOR_MESSAGE} to check whether a message with cause of finish
     *      was sent
     */
    private static final int WAIT_FOR_MESSAGE = 2000;
    /** Time when last handshake started to determine time it takes */
    private long handshakeStartTime;

    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        @Override
        public void onRoomConnecting(@Nullable Room room) {
            Log.d(Controller.APP_TAG, "onRoomConnecting");
            Network.this.room = room;
        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {
            Log.d(Controller.APP_TAG, "onRoomAutoMatching");
            Network.this.room = room;
        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
            Log.d(Controller.APP_TAG, "onPeerInvitedToRoom");
            Network.this.room = room;
        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
            Log.d(Controller.APP_TAG, "onPeerDeclined");
            Network.this.room = room;
        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
            Log.d(Controller.APP_TAG, "onPeerJoined");
            Network.this.room = room;
        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
            Log.d(Controller.APP_TAG, "onPeerLeft");
            Network.this.room = room;
            waitOrFinish();
        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            Log.d(Controller.APP_TAG, "onConnectedToRoom");
            Network.this.room = room;
        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            Log.d(Controller.APP_TAG, "onDisconnectedFromRoom");
            Network.this.room = room;
            waitOrFinish();
        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d(Controller.APP_TAG, "onPeersConnected");
            Network.this.room = room;
        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d(Controller.APP_TAG, "onPeersDisconnected");
            Network.this.room = room;
            waitOrFinish();
        }

        /**
         * Checks whether {@code onRoomConnected} had already been called.
         * If so and I am server, starts game
         */
        @Override
        public void onP2PConnected(@NonNull String s) {
            Log.d(Controller.APP_TAG, "onP2PConnected " + s);
            ++counterOfConnections;
            if (counterOfConnections == 2 && isServer) {
                manager.startOnlineGame();
            }
        }


        @Override
        public void onP2PDisconnected(@NonNull String s) {
            Log.d(Controller.APP_TAG, "onP2PDisconnected " + s);
            waitOrFinish();
        }
    };
    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int i, @Nullable Room room) {
            Log.d(Controller.APP_TAG, "Room was created");
            startNewTimer();
        }

        @Override
        public void onJoinedRoom(int i, @Nullable Room room) {
            Log.d(Controller.APP_TAG, "Joined room");
            Network.this.room = room;
        }

        @Override
        public void onLeftRoom(int i, @NonNull String s) {
            Log.d(Controller.APP_TAG, "Left room");
            waitOrFinish();
        }

        /**
         * Determines which player is a server (with minimal participantId)
         * Gets {@code myParticipantId}
         * If I am server and {@code onP2PConnected} with opponent was already called
         * then starts game
         */
        @Override
        public void onRoomConnected(int code, @Nullable Room room) {
            Log.d(Controller.APP_TAG, "Connected to room");
            if (room == null) {
                Log.wtf(Controller.APP_TAG, "onRoomConnected got null as room");
                return;
            }
            Network.this.room = room;
            if (code == GamesCallbackStatusCodes.OK) {
                Log.d(Controller.APP_TAG,"Connected");
            } else {
                Log.d(Controller.APP_TAG,"Error during connecting");
            }
            serverId = Collections.min(room.getParticipantIds());

            Games.getPlayersClient(manager.getActivity(), googleSignInAccount)
                    .getCurrentPlayerId()
                    .addOnSuccessListener(myPlayerId -> {
                        myParticipantId = room.getParticipantId(myPlayerId);
                        Log.d(Controller.APP_TAG, "Received participant id");
                        walkRoomMembers();
                        if (myParticipantId.equals(serverId)) {
                            isServer = true;
                            Log.d(Controller.APP_TAG, "I am server");
                            ++counterOfConnections;
                            if (counterOfConnections == 2) {
                                manager.startOnlineGame();
                            }
                        }
                    });
        }
    };
    /**
     * Parses message and starts message processing
     * Reloads timer if needed
     */
    private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = realTimeMessage -> {
        Log.d(Controller.APP_TAG,"Received message");
        byte[] buf = realTimeMessage.getMessageData();
        String userId = realTimeMessage.getSenderParticipantId();
        if (gameIsFinished) {
            Log.e(Controller.APP_TAG, "received message but game is over");
            return;
        }
        if (timer != null && !userId.equals(myParticipantId)) {
            timer.cancel();
            startNewTimer();
        }
        try {
            Message message = Message.readMessage(buf);
            manager.getMessageProcessor().process(message, userId);
        } catch (IOException e) {
            Log.e(Controller.APP_TAG, "Error during reading message");
            e.printStackTrace();
        }
    };

    public Network(OnlineGameManager onlineGameManager) {
        manager = onlineGameManager;
    }

    /** Prints all room members' names, sets nicknames to score counter */
    private void walkRoomMembers() {
        Log.d(Controller.APP_TAG, "Start printing room members");
        if (room != null) {
            for (Participant participant : room.getParticipants()) {
                if (participant.getParticipantId().equals(myParticipantId)) {
                   manager.getActivity().setMyNick(participant.getDisplayName());
                } else {
                    manager.getActivity().setOpponentNick(participant.getDisplayName());
                }
                Log.d(Controller.APP_TAG, participant.getDisplayName());
            }
        }
        Log.d(Controller.APP_TAG, "Finish printing room members");
    }

    /** Closes connection to room */
    private void leaveRoom() {
        if (room != null && mRealTimeMultiplayerClient != null) {
            mRealTimeMultiplayerClient.leave(mRoomConfig, room.getRoomId());
        }
        room = null;
    }

    public void updateScore() {
        ++scoreSum;
    }

    /**
     * Finishes network part of online game
     * Cancels all timers, leaves room, updates rating
     */
    public void finish() {
        if (gameIsFinished) {
            return;
        }
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

    /** Send immediate update o\to rating */
    private void updateRating() {
        if (leaderboardsClient == null || scoreSum == -1) {
            return;
        }
        Log.d(Controller.APP_TAG, "Updating rating");
        leaderboardsClient.submitScoreImmediate(manager.getActivity()
                .getString(R.string.leaderboard), scoreSum);
    }

    /** Reloads {@code timer} */
    private void startNewTimer() {
        timer = new CountDownTimer(MAXIMUM_TIME_WITHOUT_MESSAGES, MAXIMUM_TIME_WITHOUT_MESSAGES) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (timer == this) {
                    if (counterOfConnections == 0) {
                        finishImmediately(manager.getActivity()
                                .getString(R.string.opponent_not_found));
                    } else {
                        sendMessageToAll(new FinishMessage(OnlineFinishCodes.SERVER_TIMER_TIMEOUT));
                    }
                }
            }
        };
        timer.start();
    }

    /** Continues game cycle after successful handshake */
    public void continueGame() {
        if (handshakeTimer != null) {
            handshakeTimer.cancel();
            handshakeTimer = null;
            Log.d(Controller.APP_TAG, "Successful handshake. Took "
                    + (System.currentTimeMillis() - handshakeStartTime) + "ms");
            manager.getAdminLogic().publishing();
        }
    }

    /** Saves Google account, loads current score of user. On success starts searching for opponents */
    public void onSignedIn(@NonNull GoogleSignInAccount signInAccount) {
        Log.d(Controller.APP_TAG, "Logged in");
        googleSignInAccount = signInAccount;
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(manager.getActivity(),
                googleSignInAccount);
        leaderboardsClient = Games.getLeaderboardsClient(manager.getActivity(),
                googleSignInAccount);
        leaderboardsClient.loadCurrentPlayerLeaderboardScore(
                manager.getActivity().getString(R.string.leaderboard),
                TIME_SPAN_ALL_TIME, COLLECTION_PUBLIC).addOnSuccessListener(
                        leaderboardScoreAnnotatedData -> {
                            Log.d(Controller.APP_TAG, "Got score");
                            LeaderboardScore score = leaderboardScoreAnnotatedData.get();
                            if (score != null) {
                                scoreSum = score.getRawScore();
                            } else {
                                scoreSum = 0;
                            }
                            Log.d(Controller.APP_TAG, "Score is " + scoreSum);
                            startQuickGame();
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

    /**
     * Waits {@code WAIT_FOR_MESSAGE} time. If no message with cause of finish was delivered sets
     * default cause
     */
    private void waitOrFinish() {
        if (gameIsFinished) {
            return;
        }
        new Handler().postDelayed(() ->
                finishImmediately(manager.getActivity().getString(R.string.default_error)),
                WAIT_FOR_MESSAGE);
    }

    /**
     * Finishes game
     * @param message description of reason of finishing game
     */
    public void finishImmediately(@NonNull String message) {
        if (gameIsFinished) {
            return;
        }
        manager.finishOnlineGame();
        manager.getActivity().showGameFinishedActivity(message);
    }

    /** Sends message to all users in a room (and to itself) */
    public void sendMessageToAll(@NonNull Message message) {
        // The order of sending is critical!
        if (getOpponentParticipantId() != null) {
            sendMessageToConcreteUser(getOpponentParticipantId(), message);
        }
        manager.getMessageProcessor().process(message, myParticipantId);
    }

    /**
     * Sends message to user by id reliably.
     * If sending is unsuccessful repeats it {@code TIMES_TO_SEND} times until success
     * If there was no success, panics
     * Can send message to itself
     */
    public void sendMessageToConcreteUser(@NonNull String userId, @NonNull Message message) {
        if (myParticipantId == null || room == null) {
            Log.e(Controller.APP_TAG, "Cannot send message before initialization");
            return;
        }
        if (userId.equals(myParticipantId)) {
            Log.d(Controller.APP_TAG, "Sending message to myself");
            manager.getMessageProcessor().process(message, myParticipantId);
        } else {
            Log.d(Controller.APP_TAG, "Start sending message to " + userId);
            sendMessageToConcreteUserNTimes(userId, message, TIMES_TO_SEND);
        }
    }

    /**
     * Sends message to user by id reliably.
     * If sending is unsuccessful repeats it {@code timesToSend} times until success
     * If there was no success, panics
     */
    private void sendMessageToConcreteUserNTimes(@NonNull String userId, @NonNull Message message,
                                          int timesToSend) {
        if (gameIsFinished) {
            return;
        }
        if (timesToSend < 0) {
            Log.wtf(Controller.APP_TAG, "Failed to send message too many times. Finish game");
            finishImmediately(manager.getActivity().getString(R.string.default_error));
            return;
        }
        mRealTimeMultiplayerClient.sendReliableMessage(message.toByteArray(), room.getRoomId(),
                userId, (i, i1, s) -> {
            if (i != GamesCallbackStatusCodes.OK) {

                Log.e(Controller.APP_TAG, "Failed to send message. Left " + timesToSend + " tries\n" +
                                "Error is " + GamesCallbackStatusCodes.getStatusCodeString(i));
                sendMessageToConcreteUserNTimes(userId, message, timesToSend - 1);
            } else {
                Log.d(Controller.APP_TAG, "Message to " + userId + " is delivered. Took " +
                        (TIMES_TO_SEND - timesToSend + 1) + " tries");
            }
        });
    }

    /** Sends question. Starts {@code handshakeTimer} */
    public void sendQuestion(@NonNull QuestionMessage message) {
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
                // check in case message was delivered right before finish
                if (handshakeTimer == this) {
                    Log.wtf(Controller.APP_TAG, "Unsuccessful handshake");
                    sendMessageToAll(new FinishMessage(OnlineFinishCodes.UNSUCCESSFUL_HANDSHAKE));
                }
            }
        };
        handshakeStartTime = System.currentTimeMillis();
        handshakeTimer.start();
    }

    /** Returns participant's name by his/her id */
    public String getParticipantName(@NonNull String userId) {
        for (Participant participant : room.getParticipants()) {
            if (participant.getParticipantId().equals(userId)) {
                return participant.getDisplayName();
            }
        }
        return null;
    }

    /** Sends message directly to server */
    public void sendMessageToServer(@NonNull Message message) {
        if (isServer) {
            manager.getMessageProcessor().process(message, myParticipantId);
        } else {
            sendMessageToConcreteUser(serverId, message);
        }
    }

    public String getMyParticipantId() {
        return myParticipantId;
    }

    /** Determines whether server of this game is on current device */
    public boolean iAmServer() {
        return isServer;
    }

    /**
     * Returns opponent's participant id
     * Returns null if was not found
     */
    public String getOpponentParticipantId() {
        if (room == null) {
            return null;
        }
        for (String participantId : room.getParticipantIds()) {
            if (!participantId.equals(myParticipantId)) {
                return participantId;
            }
        }
        Log.wtf(Controller.APP_TAG, "Opponent id was not found.");
        return null;
    }
}
