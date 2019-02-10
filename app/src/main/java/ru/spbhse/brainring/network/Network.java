package ru.spbhse.brainring.network;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

import java.util.List;

import ru.spbhse.brainring.Controller;

public class Network {

    public RoomConfig mRoomConfig;
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
            Controller.setLocation(1);
            Controller.setQuestionText("УРАААА! МЫ СКОННЕКТИЛИСЬ С КЕМ-ТО!!!");
        }

        @Override
        public void onLeftRoom(int i, @NonNull String s) {

        }

        @Override
        public void onRoomConnected(int i, @Nullable Room room) {
            Controller.setLocation(1);
            Controller.setQuestionText("УРАААА! МЫ СКОННЕКТИЛИСЬ С КЕМ-ТО!!!");
        }
    };
    public OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            String sender = realTimeMessage.getSenderParticipantId();
            if (buf.length > 0) {
                Controller.setLocation(1);
                Controller.setQuestionText(Character.valueOf((char) buf[0]).toString() + "\n" + sender);
            }
        }
    };

    public Network() {
    }

    public void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(Controller.gameActivity, googleSignInAccount);
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);

        System.out.println("STARTING CREATING ROOM\n");
        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        System.out.println("HER1E\n");
        Games.getRealTimeMultiplayerClient(Controller.gameActivity, googleSignInAccount)
                .create(mRoomConfig);
        System.out.println("GG\n");
    }

}
