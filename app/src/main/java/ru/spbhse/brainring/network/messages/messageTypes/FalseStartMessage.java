package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from client to server in online game and from server to client in local game signalizing
 *         that client pushed button before time start
 * Format: empty message
 */
public class FalseStartMessage extends Message {
    public FalseStartMessage() {
        super(MessageCodes.FALSE_START);
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.toByteArray();
    }
}
