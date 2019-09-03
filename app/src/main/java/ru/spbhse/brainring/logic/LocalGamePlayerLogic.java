package ru.spbhse.brainring.logic;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.managers.LocalPlayerGameManager;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerReadyMessage;
import ru.spbhse.brainring.utils.SoundPlayer;

/** Class realizing player's logic in local network mode */
public class LocalGamePlayerLogic {
    private LocalPlayerGameManager manager;
    private SoundPlayer player = new SoundPlayer();

    public LocalGamePlayerLogic(LocalPlayerGameManager manager) {
        this.manager = manager;
    }

    /** Shows toast with forbiddance to answer */
    public void onForbiddenToAnswer() {
        manager.getActivity().makeToast(manager.getActivity().getString(R.string.forbidden_answer));
    }

    /** Shows toast with allowance to answer */
    public void onAllowedToAnswer() {
        manager.getActivity().makeToast(manager.getActivity().getString(R.string.allowed_answer));
    }

    /** Shows toast with false start message */
    public void onFalseStart() {
        manager.getActivity().makeToast(manager.getActivity().getString(R.string.false_start));
    }

    /** Plays sound of time start */
    public void onTimeStart() {
        player.play(manager.getActivity(), R.raw.start);
    }

    public void finish() {
        player.finish();
    }

    /**
     * Sends message to server signalizing that team is ready to answer
     * Called when team pushed the button
     */
    public void answerButtonPushed() {
        manager.getNetwork().sendMessageToServer(new AnswerReadyMessage(System.currentTimeMillis()));
    }
}
