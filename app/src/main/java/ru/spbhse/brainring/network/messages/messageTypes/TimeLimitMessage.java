package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

public class TimeLimitMessage extends Message {
    private int roundNumber;

    public int getRoundNumber() {
        return roundNumber;
    }

    public TimeLimitMessage(int roundNumber) {
        super(MessageCodes.TIME_LIMIT);
        this.roundNumber = roundNumber;
    }

    public TimeLimitMessage(@NonNull DataInputStream inputStream) throws IOException {
        super(MessageCodes.TIME_LIMIT);
        roundNumber = inputStream.readInt();
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.writeInt(roundNumber).toByteArray();
    }
}
