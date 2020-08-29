package ru.spbhse.brainring.network;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.spbhse.brainring.managers.Manager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.utils.Constants;

/** Class for interaction with network in local network mode */
public abstract class LocalNetwork {
    protected final HashMap<String, Socket> contacts = new HashMap<>();
    private Manager manager;
    private Handler uiHandler = new Handler();
    protected final ExecutorService executor = Executors.newFixedThreadPool(5);
    /** Flag to determine if handshake was done */
    protected boolean handshaked = false;
    protected boolean gameIsFinished = false;

    protected LocalNetwork(Manager manager) {
        this.manager = manager;
    }

    /** Reacts on received message */
    protected abstract void onMessageReceived(@NonNull byte[] buf, @NonNull String userId, long timeReceived);

    public void sendMessageToConcreteUser(@NonNull String userId, @NonNull Message message) {
        executor.submit(() -> {
            Log.d(Constants.APP_TAG, "Start sending message to " + userId + " at " +
                    System.currentTimeMillis());
            if (!contacts.containsKey(userId)) {
                Log.e(Constants.APP_TAG, "unexpected user id");
                return;
            }
            try {
                synchronized (contacts) {
                    contacts.get(userId).getOutputStream().write(message.toByteArray());
                    contacts.get(userId).getOutputStream().flush();
                }
                Log.d(Constants.APP_TAG, "Message sent at " + System.currentTimeMillis());
            } catch (IOException e) {
                Log.e(Constants.APP_TAG, "Error while sending");
                e.printStackTrace();
            }
        });
    }

    /** Finishes network part of local game */
    public void finish() {
        if (!gameIsFinished) {
            gameIsFinished = true;
            synchronized (contacts) {
                for (String id : contacts.keySet()) {
                    try {
                        Objects.requireNonNull(contacts.get(id)).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                executor.shutdownNow();
            }
        }
    }

    public abstract Manager getManager();

    public abstract void onDisconnected(String disconnectedId);

    public Handler getUiHandler() {
        return uiHandler;
    }
}
