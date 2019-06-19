package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.managers.LocalPlayerGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerReadyMessage;

/** Class realizing player's logic in local network mode */
public class LocalGamePlayerLogic {
    private LocalPlayerGameManager manager;
    private static final Message PUSHED_BUTTON = new AnswerReadyMessage(0);

    public LocalGamePlayerLogic(LocalPlayerGameManager manager) {
        this.manager = manager;
    }

    /** Shows toast with forbiddance to answer */
    public void onForbiddenToAnswer() {
        Toast.makeText(manager.getActivity(),
                manager.getActivity().getString(R.string.forbidden_answer),
                Toast.LENGTH_LONG).show();
    }

    /** Shows toast with allowance to answer */
    public void onAllowedToAnswer() {
        Toast.makeText(manager.getActivity(),
                manager.getActivity().getString(R.string.allowed_answer),
                Toast.LENGTH_LONG).show();
    }

    /** Shows toast with false start message */
    public void onFalseStart() {
        Toast.makeText(manager.getActivity(),
                manager.getActivity().getString(R.string.false_start),
                Toast.LENGTH_LONG).show();
    }

    /** Plays sound of time start */
    public void onTimeStart() {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(manager.getActivity(), R.raw.start);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
    }

    /**
     * Sends message to server signalizing that team is ready to answer
     * Called when team pushed the button
     */
    public void answerButtonPushed() {
        manager.getNetwork().sendMessageToServer(PUSHED_BUTTON);
    }
}
