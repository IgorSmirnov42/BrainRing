package ru.spbhse.brainring.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import ru.spbhse.brainring.managers.LocalPlayerGameManager;
import ru.spbhse.brainring.managers.Manager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.messageTypes.IAmGreenMessage;
import ru.spbhse.brainring.network.messages.messageTypes.IAmRedMessage;
import ru.spbhse.brainring.utils.Constants;
import ru.spbhse.brainring.utils.LocalGameRoles;

/**
 * Class with methods to interact with network
 * Used by player in a local network mode
 */
public class LocalNetworkPlayer extends LocalNetwork {
    private String serverId;
    /** Green or red table. Values are written in base class */
    private LocalGameRoles myColor;
    private LocalPlayerGameManager manager;

    /**
     * Creates new instance of LocalNetworkPlayer.
     * @param myColor string "red" or "green"
     */
    public LocalNetworkPlayer(@NonNull LocalGameRoles myColor, LocalPlayerGameManager manager) {
        super(manager);

        this.manager = manager;
        this.myColor = myColor;
    }

    /**
     * Decodes byte message received by player and calls needed functions in LocalController
     * If it is a first message to player, sends response if green
     */
    @Override
    protected void onMessageReceived(@NonNull byte[] buf, @NonNull String userId, long timeReceived) {
        Log.d(Constants.APP_TAG,"RECEIVED MESSAGE AS PLAYER!");
        if (gameIsFinished) {
            return;
        }

        try {
            Message message = Message.readMessage(buf);
            manager.getProcessor().process(message, userId, timeReceived);
        } catch (IOException e) {
            Log.e(Constants.APP_TAG, "Error while reading message");
            e.printStackTrace();
        }

    }

    public void doInitialHandshake(@NonNull String serverId) {
        this.serverId = serverId;
        handshaked = true;
        if (myColor == LocalGameRoles.ROLE_GREEN) {
            Log.d(Constants.APP_TAG, "I am green");
            sendMessageToConcreteUser(serverId, new IAmGreenMessage());
        } else {
            Log.d(Constants.APP_TAG, "I am red");
            sendMessageToConcreteUser(serverId, new IAmRedMessage());
        }
    }

    /**
     * Sends message to server
     * If server is not known, does nothing
     */
    public void sendMessageToServer(@NonNull Message message) {
        if (serverId == null) {
            Log.d(Constants.APP_TAG, "Sending message before handshake");
        } else {
            sendMessageToConcreteUser(serverId, message);
        }
    }

    public void connect(String serverIp) {
        executor.submit(() -> {
            try {
                Socket socket = new Socket(serverIp, Constants.LOCAL_PORT);
                if (socket.isConnected()) {
                    LocalMessageDealing messageDealing = new LocalMessageDealing(socket,
                            LocalNetworkPlayer.this, "server");
                    synchronized (contacts) {
                        contacts.put("server", socket);
                    }
                    executor.submit(messageDealing);
                    doInitialHandshake("server");
                } else {
                    throw new ConnectException();
                }
            } catch (IOException e) {
                Log.wtf(Constants.APP_TAG, "Cannot connect");
                e.printStackTrace();
                getUiHandler().post(() -> manager.finishGame());
            }
        });
    }

    @Override
    public void onDisconnected(String disconnectedId) {
        manager.finishGame();
    }

    @Override
    public Manager getManager() {
        return manager;
    }
}
