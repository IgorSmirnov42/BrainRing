package ru.spbhse.brainring.network.callbacks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

import ru.spbhse.brainring.network.LocalNetworkAdmin;
import ru.spbhse.brainring.utils.Constants;

/** Reacts on room update. Used in {@code LocalNetworkAdmin} */
public class LocalAdminRoomUpdateCallback extends RoomUpdateCallback {
    private LocalNetworkAdmin network;

    public LocalAdminRoomUpdateCallback(LocalNetworkAdmin network) {
        this.network = network;
    }

    @Override
    public void onRoomCreated(int i, @Nullable Room room) {
        Log.d(Constants.APP_TAG, "Room was created");
        network.setRoom(room);
    }

    @Override
    public void onJoinedRoom(int i, @Nullable Room room) {
        Log.d(Constants.APP_TAG, "Joined room");
        network.setRoom(room);
    }

    @Override
    public void onLeftRoom(int i, @NonNull String s) {
        Log.d(Constants.APP_TAG, "Left room");
        if (!network.gameIsFinished()) {
            network.getManager().finishGame();
            network.getManager().getActivity().finish();
        }
    }

    /** Gets participant id. If both players are p2p connected starts handshake process */
    @Override
    public void onRoomConnected(int code, @Nullable Room room) {
        Log.d(Constants.APP_TAG, "Connected to room");
        if (room == null) {
            Log.wtf(Constants.APP_TAG, "onRoomConnected got null as room");
            return;
        }
        network.setRoom(room);
        if (code == GamesCallbackStatusCodes.OK) {
            Log.d(Constants.APP_TAG, "Connected");
        } else {
            Log.d(Constants.APP_TAG, "Connecting error");
        }
        Games.getPlayersClient(network.getManager().getActivity(), network.getSignInAccount())
                .getCurrentPlayerId()
                .addOnSuccessListener(myPlayerId -> {
                    network.setMyParticipantId(room.getParticipantId(myPlayerId));
                    network.serverRoomIsConnected();
                    if (network.getP2PConnected() == 2) {
                        network.handshake();
                    }
                });
    }
}
