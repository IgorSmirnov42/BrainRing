package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from server to client signalizing that server is ready to accept an answer
 *      in both local and online games
 * Format: empty message
 */
public class AllowedToAnswerMessage extends Message {
    public AllowedToAnswerMessage() {
        super(MessageCodes.ALLOWED_TO_ANSWER);
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.toByteArray();
    }
}
