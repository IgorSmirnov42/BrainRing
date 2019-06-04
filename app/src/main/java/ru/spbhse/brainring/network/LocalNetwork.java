package ru.spbhse.brainring.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

import java.util.List;

import ru.spbhse.brainring.controllers.OnlineController;

/** Class for interaction with network in local network mode */
public abstract class LocalNetwork {
    protected static final int ROLE_ADMIN = 1;
    protected static final int ROLE_GREEN = 1 << 1;
    protected static final int ROLE_RED = 1 << 2;
    private static final int TIMES_TO_SEND = 10000;
    /** Flag to determine if handshake was done */
    protected boolean handshaked = false;
    protected RoomConfig mRoomConfig;
    protected Room room;
    public GoogleSignInAccount googleSignInAccount;
    protected RealTimeMultiplayerClient mRealTimeMultiplayerClient;
    protected String myParticipantId;
    protected RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
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
            Log.d("BrainRing", "onP2PDisconnected");
            leaveRoom();
        }
    };

    protected RoomUpdateCallback mRoomUpdateCallback;

    /** Gets message and resubmits it to {@code onMessageReceived} with sender id*/
    public OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = realTimeMessage -> {
        byte[] buf = realTimeMessage.getMessageData();
        onMessageReceived(buf, realTimeMessage.getSenderParticipantId());
    };

    /** Reacts on received message */
    protected abstract void onMessageReceived(byte[] buf, String userId);


    abstract public void startQuickGame();

    /** Sends message to user with given id */
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
                Log.d("BrainRing", "Message to " + userId + " is delivered");
            }
        });
    }

    abstract public void leaveRoom();

    /** Sends message to all users in a room except itself */
    public void sendMessageToOthers(byte[] message) {
        for (String participantId : room.getParticipantIds()) {
            if (!participantId.equals(myParticipantId)) {
                sendMessageToConcreteUser(participantId, message);
            }
        }
    }
}
