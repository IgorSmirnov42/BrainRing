package ru.spbhse.brainring.logic;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;

/** Class realizing player's logic in local network mode */
public class LocalGamePlayerLogic {

    private static byte[] PUSHED_BUTTON;

    static {
        PUSHED_BUTTON = Message.generateMessage(Message.ANSWER_IS_READY, "");
    }

    public void onForbiddenToAnswer() {
        // TODO : показать тост
    }

    public void onAllowedToAnswer() {
        // TODO : поменять что-то в активити
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
