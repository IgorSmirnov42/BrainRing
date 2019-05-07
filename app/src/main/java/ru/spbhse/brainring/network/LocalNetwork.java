package ru.spbhse.brainring.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

import java.util.List;

/** Class for interaction with network in local network mode */
public abstract class LocalNetwork {
    static final int ROLE_ADMIN = 1;
    static final int ROLE_GREEN = 1 << 1;
    static final int ROLE_RED = 1 << 2;
    /** Flag to determine if handshake was done */
    protected volatile boolean handshaked = false;
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
    public OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            onMessageReceived(buf, realTimeMessage.getSenderParticipantId());
        }
    };

    /** Reacts on received message */
    protected abstract void onMessageReceived(byte[] buf, String userId);

    //public LocalNetwork() {}

    abstract public void startQuickGame();

    /** Sends message to user with given id */
    public void sendMessageToConcreteUser(String userId, byte[] message) {
        mRealTimeMultiplayerClient.sendUnreliableMessage(message, room.getRoomId(), userId);
    }

    abstract public void leaveRoom();
}
