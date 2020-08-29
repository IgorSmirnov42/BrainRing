package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;

public class MyTimeIsMessage extends Message {
    private long time;

    public MyTimeIsMessage(long time) {
        super(MessageCodes.MY_TIME_IS);
        this.time = time;
    }

    public MyTimeIsMessage(@NonNull DataInputStream inputStream) throws IOException {
        super(MessageCodes.MY_TIME_IS);
        time = inputStream.readLong();
    }

    public long getTime() {
        return time;
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.writeLong(time).toByteArray();
    }
}
