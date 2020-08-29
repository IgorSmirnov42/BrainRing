package ru.spbhse.brainring.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import ru.spbhse.brainring.managers.LocalAdminGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.utils.Constants;

/**
 * Class with methods to interact with network
 * Used by admin in a local network mode
 */
public class LocalNetworkAdmin extends LocalNetwork {
    private LocalAdminGameManager manager;
    private String redId;
    private String greenId;
    private ServerSocket serverSocket;

    /**
     * Creates new instance. Fills {@code mRoomUpdateCallback} with an instance that
     *      on connected room starts game
     */
    public LocalNetworkAdmin(LocalAdminGameManager manager) {
        super(manager);
        this.manager = manager;
    }

    /** Decodes byte message received by server and calls needed functions in LocalController */
    @Override
    protected void onMessageReceived(@NonNull byte[] buf, @NonNull String userId, long timeReceived) {
        if (gameIsFinished) {
            return;
        }
        Log.d(Constants.APP_TAG, "Received message as admin!");
        try {
            Message message = Message.readMessage(buf);
            manager.getProcessor().process(message, userId, timeReceived);
        } catch (IOException e) {
            Log.e(Constants.APP_TAG, "Error while reading message");
            e.printStackTrace();
        }
    }

    /** Sets green player id. If both players shared their ids starts game cycle */
    public void setGreenPlayer(@NonNull String userId) {
        if (greenId != null) { // TODO: fix and close connection
            return;
        }
        if (handshaked) {
            Log.d(Constants.APP_TAG, "Handshake is done");
            return;
        }
        greenId = userId;
        manager.getActivity().setGreenStatus("Connected");
        if (redId != null) {
            handshaked = true;
            manager.getLogic().startGameCycle(greenId, redId);
        }
    }

    /** Sets red player id. If both players shared their ids starts game cycle */
    public void setRedPlayer(@NonNull String userId) {
        if (redId != null) { // TODO : fix and close connection
            return;
        }
        if (handshaked) {
            Log.d(Constants.APP_TAG, "Handshake is done");
            return;
        }
        redId = userId;
        manager.getActivity().setRedStatus("Connected");
        if (greenId != null) {
            handshaked = true;
            manager.getLogic().startGameCycle(greenId, redId);
        }
    }

    public void sendMessageToUsers(@NonNull Message message) {
        String[] keys;

        synchronized (contacts) {
            keys = contacts.keySet().toArray(new String[0]);
        }

        for (String key : keys) {
            sendMessageToConcreteUser(key, message);
        }
    }

    public void getIp() {
        executor.submit(() -> {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            String ip = inetAddress.getHostAddress();
                            Log.d(Constants.APP_TAG, "ip is " + ip + " " + inetAddress.isAnyLocalAddress() + " " + inetAddress.isLinkLocalAddress() + " " + inetAddress.isLoopbackAddress() + " " + (inetAddress instanceof Inet4Address));
                            manager.getActivity().runOnUiThread(() -> manager.getActivity().onIpReceived(ip));
                            return;
                        }
                    }
                }
            } catch (SocketException ex) {
                Log.e(Constants.APP_TAG, ex.toString());
            }
        });
    }

    public void startServer() {
        executor.submit(() -> {
            try {
                serverSocket = new ServerSocket(Constants.LOCAL_PORT);
                while (!Thread.interrupted()) {
                    Socket socket = serverSocket.accept();
                    if (socket != null) {
                        socket.setTcpNoDelay(true);
                        String senderId = String.valueOf(new Random().nextInt());
                        LocalMessageDealing messageDealing = new LocalMessageDealing(socket,
                                LocalNetworkAdmin.this, senderId);
                        synchronized (contacts) {
                            contacts.put(senderId, socket);
                            executor.submit(messageDealing);
                            if (contacts.size() == 2) {
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.wtf(Constants.APP_TAG, "IO exception starting server");
                e.printStackTrace();
                getUiHandler().post(() -> manager.finishGame());
            }
        });
    }

    @Override
    public void finish() {
        if (!gameIsFinished) {
            super.finish();
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDisconnected(String disconnectedId) {
        manager.finishGame();
    }

    public LocalAdminGameManager getManager() {
        return manager;
    }
}
