package ru.spbhse.brainring.logic;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;

public class LocalGamePlayerLogic {

    private static byte[] PUSHED_BUTTON;

    static {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeInt(Message.ANSWER_IS_READY);
            PUSHED_BUTTON = bout.toByteArray();
        } catch (IOException e) {
            PUSHED_BUTTON = null;
            e.printStackTrace();
        }
    }

    public void onForbiddenToAnswer() {
        // TODO : показать тост
    }

    public void onAllowedToAnswer() {
        // TODO : поменять что-то в активити
    }

    public void answerButtonPushed() {
        System.out.println("SEND ANSWER BUTTON PUSHED");
        Controller.LocalNetworkPlayerController.sendMessageToServer(PUSHED_BUTTON);
    }
}
