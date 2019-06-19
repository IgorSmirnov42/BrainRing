package ru.spbhse.brainring.network.callbacks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

import java.util.Collections;

import ru.spbhse.brainring.network.Network;
import ru.spbhse.brainring.utils.Constants;

public class OnlineRoomUpdateCallback extends RoomUpdateCallback {
    private Network network;

    public OnlineRoomUpdateCallback(Network network) {
        this.network = network;
    }

    @Override
    public void onRoomCreated(int i, @Nullable Room room) {
        Log.d(Constants.APP_TAG, "Room was created");
        network.setRoom(room);
        network.startNewTimer();
    }

    @Override
    public void onJoinedRoom(int i, @Nullable Room room) {
        Log.d(Constants.APP_TAG, "Joined room");
        network.setRoom(room);
    }

    @Override
    public void onLeftRoom(int i, @NonNull String s) {
        Log.d(Constants.APP_TAG, "Left room");
        network.waitOrFinish();
    }

    /**
     * Determines which player is a server (with minimal participantId)
     * Gets {@code myParticipantId}
     * If I am server and {@code onP2PConnected} with opponent was already called
     * then starts game
     */
    @Override
    public void onRoomConnected(int code, @Nullable Room room) {
        Log.d(Constants.APP_TAG, "Connected to room");
        if (room == null) {
            Log.wtf(Constants.APP_TAG, "onRoomConnected got null as room");
            return;
        }
        network.setRoom(room);
        if (code == GamesCallbackStatusCodes.OK) {
            Log.d(Constants.APP_TAG,"Connected");
        } else {
            Log.d(Constants.APP_TAG,"Error during connecting");
        }
        network.setServerId(Collections.min(room.getParticipantIds()));

        Games.getPlayersClient(network.getManager().getActivity(), network.getSignInAccount())
                .getCurrentPlayerId()
                .addOnSuccessListener(myPlayerId -> {
                    network.setMyParticipantId(room.getParticipantId(myPlayerId));
                    Log.d(Constants.APP_TAG, "Received participant id");
                    network.walkRoomMembers();
                    if (network.getMyParticipantId().equals(network.getServerId())) {
                        network.setIAmServer();
                        Log.d(Constants.APP_TAG, "I am server");
                        network.plusConnection();
                        if (network.getConnections() == 2) {
                            network.getManager().startOnlineGame();
                        }
                    }
                });
    }
}
