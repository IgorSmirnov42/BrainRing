package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from client to server in local game signalizing that client has green colored table
 * Format: empty message
 */
public class IAmGreenMessage extends Message {
    public IAmGreenMessage() {
        super(MessageCodes.I_AM_GREEN);
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.toByteArray();
    }
}
