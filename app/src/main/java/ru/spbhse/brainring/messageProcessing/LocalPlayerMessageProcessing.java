package ru.spbhse.brainring.messageProcessing;

import android.support.annotation.NonNull;
import android.util.Log;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.LocalController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageCodes;

public class LocalPlayerMessageProcessing {
    public static void process(@NonNull Message message, @NonNull String senderId) {
        switch (message.getMessageCode()) {
            case MessageCodes.INITIAL_HANDSHAKE:
                LocalController.LocalNetworkPlayerController.doInitialHandshake(senderId);
                break;
            case MessageCodes.FORBIDDEN_TO_ANSWER:
                LocalController.LocalPlayerLogicController.onForbiddenToAnswer();
                break;
            case MessageCodes.ALLOWED_TO_ANSWER:
                LocalController.LocalPlayerLogicController.onAllowedToAnswer();
                break;
            case MessageCodes.FALSE_START:
                LocalController.LocalPlayerLogicController.onFalseStart();
                break;
            case MessageCodes.TIME_START:
                LocalController.LocalPlayerLogicController.onTimeStart();
                break;
            case MessageCodes.HANDSHAKE:
                LocalController.LocalNetworkController.sendMessageToConcreteUser(senderId, message);
                break;
            default:
                Log.wtf(Controller.APP_TAG, "Unexpected message received");
        }
    }
}
