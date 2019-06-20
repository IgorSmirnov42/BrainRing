package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from client to server in online game signalizing that client is ready to continue a game
 * Format: empty message
 */
public class ReadyForQuestionMessage extends Message {
    public ReadyForQuestionMessage() {
        super(MessageCodes.READY_FOR_QUESTION);
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.toByteArray();
    }
}
