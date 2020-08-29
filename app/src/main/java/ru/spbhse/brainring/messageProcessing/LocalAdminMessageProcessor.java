package ru.spbhse.brainring.messageProcessing;

import android.support.annotation.NonNull;
import android.util.Log;

import ru.spbhse.brainring.managers.LocalAdminGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerReadyMessage;
import ru.spbhse.brainring.network.messages.messageTypes.MyTimeIsMessage;
import ru.spbhse.brainring.utils.Constants;

public class LocalAdminMessageProcessor {
    private LocalAdminGameManager manager;

    public LocalAdminMessageProcessor(LocalAdminGameManager gameManager) {
        manager = gameManager;
    }

    public void process(@NonNull Message message, @NonNull String senderId, long timeReceived) {
        switch(message.getMessageCode()) {
            case MessageCodes.I_AM_GREEN:
                manager.getNetwork().setGreenPlayer(senderId);
                break;
            case MessageCodes.I_AM_RED:
                manager.getNetwork().setRedPlayer(senderId);
                break;
            case MessageCodes.ANSWER_IS_READY:
                manager.getLogic().onAnswerIsReady(senderId,
                        ((AnswerReadyMessage) message).getTime());
                break;
            case MessageCodes.MY_TIME_IS:
                manager.getLogic().onTimeReceived(senderId, ((MyTimeIsMessage) message).getTime(),
                        timeReceived);
                break;
            default:
                Log.e(Constants.APP_TAG, "Unknown message. Code is " + message.getMessageCode());
        }
    }
}
