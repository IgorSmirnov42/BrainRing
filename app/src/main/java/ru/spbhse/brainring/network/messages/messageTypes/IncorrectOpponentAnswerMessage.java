package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from server to client in online game signalizing that opponent answered wrong
 *         with opponent's answer inside
 * Format: [String : opponentAnswer]
 *      opponentAnswer -- wrong answer that opponent wrote
 */
public class IncorrectOpponentAnswerMessage extends Message {
    private String opponentAnswer;

    public String getOpponentAnswer() {
        return opponentAnswer;
    }

    public IncorrectOpponentAnswerMessage(String opponentAnswer) {
        super(MessageCodes.SENDING_INCORRECT_OPPONENT_ANSWER);
        this.opponentAnswer = opponentAnswer;
    }

    public IncorrectOpponentAnswerMessage(@NonNull DataInputStream inputStream) throws IOException {
        super(MessageCodes.SENDING_INCORRECT_OPPONENT_ANSWER);
        opponentAnswer = inputStream.readUTF();
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.writeString(opponentAnswer).toByteArray();
    }
}
