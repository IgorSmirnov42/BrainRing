package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.widget.Toast;

import ru.spbhse.brainring.controllers.LocalController;
import ru.spbhse.brainring.R;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageGenerator;

/** Class realizing player's logic in local network mode */
public class LocalGamePlayerLogic {

    private static byte[] PUSHED_BUTTON;

    static {
        PUSHED_BUTTON = MessageGenerator.create().writeInt(Message.ANSWER_IS_READY).toByteArray();
    }

    public void onForbiddenToAnswer() {
        Toast.makeText(LocalController.getPlayerActivity(), "Сервер запретил вам отвечать", Toast.LENGTH_LONG).show();
    }

    public void onAllowedToAnswer() {
        Toast.makeText(LocalController.getPlayerActivity(), "Разрешено отвечать!", Toast.LENGTH_LONG).show();
    }

    public void onFalseStart() {
        Toast.makeText(LocalController.getPlayerActivity(), "Фальстарт!", Toast.LENGTH_LONG).show();
    }

    public void onTimeStart() {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(LocalController.getPlayerActivity(), R.raw.start);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        // TODO : поменять надпись на кнопке
    }

    /**
     * Sends message to server signalizing that team is ready to answer
     * Called when team pushed the button
     */
    public void answerButtonPushed() {
        LocalController.LocalNetworkPlayerController.sendMessageToServer(PUSHED_BUTTON);
    }
}
