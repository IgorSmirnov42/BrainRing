package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from server to client signalizing that server forbidden client answering now
 * Format: empty message
 */
public class ForbiddenToAnswerMessage extends Message {
    public ForbiddenToAnswerMessage() {
        super(MessageCodes.FORBIDDEN_TO_ANSWER);
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.toByteArray();
    }
}
