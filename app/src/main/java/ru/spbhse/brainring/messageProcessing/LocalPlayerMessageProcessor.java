package ru.spbhse.brainring.messageProcessing;

import android.support.annotation.NonNull;
import android.util.Log;

import ru.spbhse.brainring.managers.LocalPlayerGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.utils.Constants;

public class LocalPlayerMessageProcessor {
    private LocalPlayerGameManager manager;

    public LocalPlayerMessageProcessor(LocalPlayerGameManager manager) {
        this.manager = manager;
    }

    public void process(@NonNull Message message, @NonNull String senderId) {
        switch (message.getMessageCode()) {
            case MessageCodes.INITIAL_HANDSHAKE:
                manager.getNetwork().doInitialHandshake(senderId);
                break;
            case MessageCodes.FORBIDDEN_TO_ANSWER:
                manager.getLogic().onForbiddenToAnswer();
                break;
            case MessageCodes.ALLOWED_TO_ANSWER:
                manager.getLogic().onAllowedToAnswer();
                break;
            case MessageCodes.FALSE_START:
                manager.getLogic().onFalseStart();
                break;
            case MessageCodes.TIME_START:
                manager.getLogic().onTimeStart();
                break;
            case MessageCodes.HANDSHAKE:
                manager.getNetwork().sendMessageToConcreteUser(senderId, message);
                break;
            default:
                Log.wtf(Constants.APP_TAG, "Unexpected message received");
        }
    }
}
