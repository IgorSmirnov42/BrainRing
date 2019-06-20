package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from server to client in local game to show who server is
 * Format: empty message
 */
public class InitialHandshakeMessage extends Message {
    public InitialHandshakeMessage() {
        super(MessageCodes.INITIAL_HANDSHAKE);
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.toByteArray();
    }
}
