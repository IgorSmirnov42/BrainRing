package ru.spbhse.brainring.messageProcessing;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.managers.LocalAdminGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageCodes;

public class LocalAdminMessageProcessor {
    private LocalAdminGameManager manager;

    public LocalAdminMessageProcessor(LocalAdminGameManager gameManager) {
        manager = gameManager;
    }

    public void process(@NonNull Message message, @NonNull String senderId) {
        switch(message.getMessageCode()) {
            case MessageCodes.I_AM_GREEN:
                manager.getNetwork().setGreenPlayer(senderId);
                break;
            case MessageCodes.I_AM_RED:
                manager.getNetwork().setRedPlayer(senderId);
                break;
            case MessageCodes.ANSWER_IS_READY:
                manager.getLogic().onAnswerIsReady(senderId);
                break;
            case MessageCodes.HANDSHAKE:
                manager.getLogic().onHandshakeAccept(senderId);
                break;
        }
    }
}
