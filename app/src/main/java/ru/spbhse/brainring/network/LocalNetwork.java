package ru.spbhse.brainring.network;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.managers.Manager;
import ru.spbhse.brainring.network.callbacks.LocalRoomStatusUpdateCallback;
import ru.spbhse.brainring.network.messages.Message;

/** Class for interaction with network in local network mode */
public abstract class LocalNetwork {
    protected static final int ROLE_ADMIN = 1;
    protected static final int ROLE_GREEN = 1 << 1;
    protected static final int ROLE_RED = 1 << 2;
    /** Number of tries that should be done to deliver a message that was failed to deliver */
    private static final int TIMES_TO_SEND = 100;

    private Manager manager;
    /** Flag to determine if handshake was done */
    protected boolean handshaked = false;
    protected boolean gameIsFinished = false;
    /** Number of users that are p2pConnected.
     * Server should start online game only of both players are p2p connected
     */
    protected int p2pConnected = 0;
    /**
     * Flag to determine whether I am server and {@code onRoomConnected} had already been called
     * Always false for player
     */
    protected boolean serverRoomConnected = false;
    protected RoomConfig mRoomConfig;
    protected Room room;
    protected GoogleSignInAccount googleSignInAccount;
    protected RealTimeMultiplayerClient mRealTimeMultiplayerClient;
    protected String myParticipantId;
    protected RoomStatusUpdateCallback mRoomStatusUpdateCallback;
    protected RoomUpdateCallback mRoomUpdateCallback;

    protected LocalNetwork(Manager manager) {
        this.manager = manager;
        mRoomStatusUpdateCallback = new LocalRoomStatusUpdateCallback(this);
    }

    public GoogleSignInAccount getSignInAccount() {
        return googleSignInAccount;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setMyParticipantId(String participantId) {
        myParticipantId = participantId;
    }

    public int getP2PConnected() {
        return p2pConnected;
    }

    /** Gets message and resubmits it to {@code onMessageReceived} with sender id */
    protected OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = realTimeMessage -> {
        byte[] buf = realTimeMessage.getMessageData();
        onMessageReceived(buf, realTimeMessage.getSenderParticipantId());
    };

    /** Reacts on received message */
    protected abstract void onMessageReceived(@NonNull byte[] buf, @NonNull String userId);

    /** Starts quick game with 2 auto matched players */
    abstract public void startQuickGame();

    /**
     * Sends message to user by id reliably.
     * If sending is unsuccessful repeats it {@code TIMES_TO_SEND} times until success
     * If there was no success, panics
     * CANNOT send message to itself
     */
    public void sendMessageToConcreteUser(@NonNull String userId, @NonNull Message message) {
        Log.d(Controller.APP_TAG, "Start sending message to " + userId);
        sendMessageToConcreteUserNTimes(userId, message, TIMES_TO_SEND);
    }

    /**
     * Sends message to user by id reliably.
     * If sending is unsuccessful repeats it {@code timesToSend} times until success
     * If there was no success, panics
     * CANNOT send message to itself
     */
    private void sendMessageToConcreteUserNTimes(@NonNull String userId, @NonNull Message message,
                                                 int timesToSend) {
        if (gameIsFinished) {
            return;
        }
        if (timesToSend < 0) {
            Log.wtf(Controller.APP_TAG, "Failed to send message too many times. Finish game");
            manager.finishGame();
            manager.getActivity().finish();
            return;
        }
        mRealTimeMultiplayerClient.sendReliableMessage(message.toByteArray(), room.getRoomId(),
                userId, (i, i1, s) -> {
            if (i != GamesCallbackStatusCodes.OK) {
                Log.e(Controller.APP_TAG, "Failed to send message. Left " + timesToSend +
                        " tries\n" + "Error is " + GamesCallbackStatusCodes.getStatusCodeString(i));
                sendMessageToConcreteUserNTimes(userId, message, timesToSend - 1);
            } else {
                Log.d(Controller.APP_TAG, "Message to " + userId + " is delivered. Took " +
                        (TIMES_TO_SEND - timesToSend + 1) + " tries");
            }
        });
    }

    public boolean gameIsFinished() {
        return gameIsFinished;
    }

    public void plusP2PConnected() {
        ++p2pConnected;
    }

    public void minusP2PConnected() {
        --p2pConnected;
    }

    public boolean isServerRoomConnected() {
        return serverRoomConnected;
    }

    /** Closes connection with room */
    abstract protected void leaveRoom();

    /**
     * Server starts handshake process sending everybody initial handshake message
     * After that player should send message describing its table color
     */
    abstract public void handshake();

    /** Finishes network part of local game */
    public void finish() {
        if (!gameIsFinished) {
            gameIsFinished = true;
            leaveRoom();
        }
    }

    /** Sets Google account */
    public void signedIn(GoogleSignInAccount account) {
        googleSignInAccount = account;
    }

    /** Sends message to all users in a room except itself */
    public void sendMessageToOthers(@NonNull Message message) {
        for (String participantId : room.getParticipantIds()) {
            if (!participantId.equals(myParticipantId)) {
                sendMessageToConcreteUser(participantId, message);
            }
        }
    }

    public abstract Manager getManager();
}
