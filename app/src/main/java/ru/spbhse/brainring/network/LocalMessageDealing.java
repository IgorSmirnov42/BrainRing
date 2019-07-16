package ru.spbhse.brainring.network;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

import ru.spbhse.brainring.utils.Constants;

public class LocalMessageDealing implements Runnable {
    private final Socket socket;
    private final LocalNetwork network;
    private final String senderId;

    public LocalMessageDealing(Socket socket, LocalNetwork network, String senderId) {
        this.socket = socket;
        this.network = network;
        this.senderId = senderId;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            byte[] buf = new byte[1024];
            try {
                int read = socket.getInputStream().read(buf);
                Log.d(Constants.APP_TAG, "Read " + read + " bytes from " + senderId);
                if (read < 0) {
                    network.getUiHandler().post(() -> network.onDisconnected(senderId));
                    break;
                }
                if (read != 0) {
                    network.getUiHandler().post(() -> network.onMessageReceived(buf, senderId));
                }
            } catch (IOException e) {
                Log.wtf(Constants.APP_TAG, "Error while reading from input stream socket");
                e.printStackTrace();
                network.getUiHandler().post(() -> network.onDisconnected(senderId));
                break;
            }
        }
    }
}
