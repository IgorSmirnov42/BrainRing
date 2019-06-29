package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from server to client in online game signalizing that opponent has allowance to answer
 * Format: empty message
 */
public class OpponentIsAnsweringMessage extends Message {
    public OpponentIsAnsweringMessage() {
        super(MessageCodes.OPPONENT_IS_ANSWERING);
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.toByteArray();
    }
}
