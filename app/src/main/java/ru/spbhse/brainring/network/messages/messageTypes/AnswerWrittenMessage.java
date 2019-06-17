package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

public class AnswerWrittenMessage extends Message {
    private String answer;

    public String getAnswer() {
        return answer;
    }

    public AnswerWrittenMessage(@NonNull String answer) {
        super(MessageCodes.ANSWER_IS_WRITTEN);
        this.answer = answer;
    }

    public AnswerWrittenMessage(@NonNull DataInputStream inputStream) throws IOException {
        super(MessageCodes.ANSWER_IS_WRITTEN);
        answer = inputStream.readUTF();
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.writeString(answer).toByteArray();
    }
}
