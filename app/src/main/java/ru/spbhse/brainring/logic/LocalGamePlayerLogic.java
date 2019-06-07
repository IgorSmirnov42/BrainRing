package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.LocalController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageGenerator;

/** Class realizing player's logic in local network mode */
public class LocalGamePlayerLogic {

    private static byte[] PUSHED_BUTTON;

    static {
        PUSHED_BUTTON = MessageGenerator.create()
                .writeInt(Message.ANSWER_IS_READY)
                .toByteArray();
    }

    /** Shows toast with forbiddance to answer */
    public void onForbiddenToAnswer() {
        Toast.makeText(LocalController.getPlayerActivity(), 
                LocalController.getPlayerActivity().getString(R.string.forbidden_answer),
                Toast.LENGTH_LONG).show();
    }

    /** Shows toast with allowance to answer */
    public void onAllowedToAnswer() {
        Toast.makeText(LocalController.getPlayerActivity(),
                LocalController.getPlayerActivity().getString(R.string.allowed_answer),
                Toast.LENGTH_LONG).show();
    }

    /** Shows toast with false start message */
    public void onFalseStart() {
        Toast.makeText(LocalController.getPlayerActivity(),
                LocalController.getPlayerActivity().getString(R.string.false_start),
                Toast.LENGTH_LONG).show();
    }

    /** Plays sound of time start */
    public void onTimeStart() {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(LocalController.getPlayerActivity(), R.raw.start);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
    }

    /**
     * Sends message to server signalizing that team is ready to answer
     * Called when team pushed the button
     */
    public void answerButtonPushed() {
        LocalController.LocalNetworkPlayerController.sendMessageToServer(PUSHED_BUTTON);
    }
}
