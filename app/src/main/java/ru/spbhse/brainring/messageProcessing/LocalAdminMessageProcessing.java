package ru.spbhse.brainring.messageProcessing;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.controllers.LocalController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageCodes;

public class LocalAdminMessageProcessing {
    public static void process(@NonNull Message message, @NonNull String senderId) {
        switch(message.getMessageCode()) {
            case MessageCodes.I_AM_GREEN:
                LocalController.LocalNetworkAdminController.setGreenPlayer(senderId);
                break;
            case MessageCodes.I_AM_RED:
                LocalController.LocalNetworkAdminController.setRedPlayer(senderId);
                break;
            case MessageCodes.ANSWER_IS_READY:
                LocalController.LocalAdminLogicController.onAnswerIsReady(senderId);
                break;
            case MessageCodes.HANDSHAKE:
                LocalController.LocalAdminLogicController.onHandshakeAccept(senderId);
                break;
        }
    }
}
