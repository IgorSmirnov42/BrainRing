package ru.spbhse.brainring.managers;

import ru.spbhse.brainring.logic.LocalGamePlayerLogic;
import ru.spbhse.brainring.messageProcessing.LocalPlayerMessageProcessor;
import ru.spbhse.brainring.network.LocalNetworkPlayer;
import ru.spbhse.brainring.ui.PlayerActivity;

/**
 * Storage of data needed for communication between packages in local mode by player.
 *
 * Stores links to network, activity, players's logic and message processor
 * Has function to finish game that finishes all running processes connected with game
 */
public class LocalPlayerGameManager implements Manager {
    private LocalGamePlayerLogic logic;
    private PlayerActivity activity;
    private LocalNetworkPlayer network;
    private LocalPlayerMessageProcessor processor;

    public LocalPlayerGameManager(PlayerActivity activity, String colorName) {
        this.activity = activity;
        logic = new LocalGamePlayerLogic(this);
        network = new LocalNetworkPlayer(colorName, this);
        processor = new LocalPlayerMessageProcessor(this);
    }

    public LocalGamePlayerLogic getLogic() {
        return logic;
    }

    public PlayerActivity getActivity() {
        return activity;
    }

    public LocalNetworkPlayer getNetwork() {
        return network;
    }

    public LocalPlayerMessageProcessor getProcessor() {
        return processor;
    }

    public void finishGame() {
        network.finish();
        logic.finish();
    }
}
