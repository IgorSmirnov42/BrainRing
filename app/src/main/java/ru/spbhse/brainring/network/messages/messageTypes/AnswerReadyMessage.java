package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from client to server signalizing that client pushed answer button in both local and
 *      online games
 * Format: [long : time]
 *      time -- time from the beginning of the round when button was pushed. In local game just current time
 */
public class AnswerReadyMessage extends Message {
    private long time;

    public AnswerReadyMessage(@NonNull DataInputStream inputStream) throws IOException {
        super(MessageCodes.ANSWER_IS_READY);
        time = inputStream.readLong();
    }

    public AnswerReadyMessage(long time) {
        super(MessageCodes.ANSWER_IS_READY);
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.writeLong(time).toByteArray();
    }
}
