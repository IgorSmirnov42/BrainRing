package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

public class QuestionMessage extends Message {
    private int questionId;
    private String question;

    public int getQuestionId() {
        return questionId;
    }

    public String getQuestion() {
        return question;
    }

    public QuestionMessage(int questionId, String question) {
        super(MessageCodes.SENDING_QUESTION);
        this.questionId = questionId;
        this.question = question;
    }

    public QuestionMessage(@NonNull DataInputStream inputStream) throws IOException {
        super(MessageCodes.SENDING_QUESTION);
        questionId = inputStream.readInt();
        question = inputStream.readUTF();
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.writeInt(questionId).writeString(question).toByteArray();
    }
}
