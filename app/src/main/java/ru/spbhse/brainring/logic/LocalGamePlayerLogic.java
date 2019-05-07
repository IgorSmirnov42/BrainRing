package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.widget.Toast;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.R;
import ru.spbhse.brainring.network.messages.Message;

/** Class realizing player's logic in local network mode */
public class LocalGamePlayerLogic {

    private static byte[] PUSHED_BUTTON;

    static {
        PUSHED_BUTTON = Message.generateMessage(Message.ANSWER_IS_READY, "");
    }

    public void onForbiddenToAnswer() {
        Toast.makeText(Controller.getPlayerActivity(), "Сервер запретил вам отвечать", Toast.LENGTH_LONG).show();
    }

    public void onAllowedToAnswer() {
        Toast.makeText(Controller.getPlayerActivity(), "Разрешено отвечать!", Toast.LENGTH_LONG).show();
    }

    public void onFalseStart() {
        Toast.makeText(Controller.getPlayerActivity(), "Фальстарт!", Toast.LENGTH_LONG).show();
    }

    public void onTimeStart() {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(Controller.getPlayerActivity(), R.raw.start);
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
        System.out.println("SEND ANSWER BUTTON PUSHED");
        Controller.LocalNetworkPlayerController.sendMessageToServer(PUSHED_BUTTON);
    }
}
