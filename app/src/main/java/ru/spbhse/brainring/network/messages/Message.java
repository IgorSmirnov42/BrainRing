package ru.spbhse.brainring.network.messages;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.network.messages.messageTypes.AllowedToAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerReadyMessage;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerWrittenMessage;
import ru.spbhse.brainring.network.messages.messageTypes.CorrectAnswerAndScoreMessage;
import ru.spbhse.brainring.network.messages.messageTypes.CorrectAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.FalseStartMessage;
import ru.spbhse.brainring.network.messages.messageTypes.FinishMessage;
import ru.spbhse.brainring.network.messages.messageTypes.ForbiddenToAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.HandshakeMessage;
import ru.spbhse.brainring.network.messages.messageTypes.IAmGreenMessage;
import ru.spbhse.brainring.network.messages.messageTypes.IAmRedMessage;
import ru.spbhse.brainring.network.messages.messageTypes.IncorrectOpponentAnswerMessage;
import ru.spbhse.brainring.network.messages.messageTypes.InitialHandshakeMessage;
import ru.spbhse.brainring.network.messages.messageTypes.OpponentIsAnsweringMessage;
import ru.spbhse.brainring.network.messages.messageTypes.QuestionMessage;
import ru.spbhse.brainring.network.messages.messageTypes.ReadyForQuestionMessage;
import ru.spbhse.brainring.network.messages.messageTypes.TimeLimitMessage;
import ru.spbhse.brainring.network.messages.messageTypes.TimeStartMessage;

public abstract class Message {
    private int messageCode;

    public int getMessageCode() {
        return messageCode;
    }

    protected Message(int messageCode) {
        this.messageCode = messageCode;
    }

    /**
     * Serializes message.
     * This method creates {@code MessageGenerator}, writes message code and then
     * transfers control to overridden version with generator as an argument
     */
    public byte[] toByteArray() {
        return toByteArray(MessageGenerator.create().writeInt(messageCode));
    }

    protected abstract byte[] toByteArray(@NonNull MessageGenerator generator);

    public static Message readMessage(@NonNull byte[] message) throws IOException {
        try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(message))) {
            int messageCode = inputStream.readInt();
            Log.d(Controller.APP_TAG, "Received message. Identifier is " + messageCode);
            switch (messageCode) {
                case MessageCodes.ANSWER_IS_READY:
                    return new AnswerReadyMessage(inputStream);
                case MessageCodes.ANSWER_IS_WRITTEN:
                    return new AnswerWrittenMessage(inputStream);
                case MessageCodes.FORBIDDEN_TO_ANSWER:
                    return new ForbiddenToAnswerMessage();
                case MessageCodes.ALLOWED_TO_ANSWER:
                    return new AllowedToAnswerMessage();
                case MessageCodes.SENDING_QUESTION:
                    return new QuestionMessage(inputStream);
                case MessageCodes.SENDING_INCORRECT_OPPONENT_ANSWER:
                    return new IncorrectOpponentAnswerMessage(inputStream);
                case MessageCodes.SENDING_CORRECT_ANSWER_AND_SCORE:
                    return new CorrectAnswerAndScoreMessage(inputStream);
                case MessageCodes.OPPONENT_IS_ANSWERING:
                    return new OpponentIsAnsweringMessage();
                case MessageCodes.TIME_START:
                    return new TimeStartMessage();
                case MessageCodes.FALSE_START:
                    return new FalseStartMessage();
                case MessageCodes.HANDSHAKE:
                    return new HandshakeMessage();
                case MessageCodes.TIME_LIMIT:
                    return new TimeLimitMessage(inputStream);
                case MessageCodes.FINISH:
                    return new FinishMessage(inputStream);
                case MessageCodes.CORRECT_ANSWER:
                    return new CorrectAnswerMessage();
                case MessageCodes.READY_FOR_QUESTION:
                    return new ReadyForQuestionMessage();
                case MessageCodes.I_AM_GREEN:
                    return new IAmGreenMessage();
                case MessageCodes.I_AM_RED:
                    return new IAmRedMessage();
                case MessageCodes.INITIAL_HANDSHAKE:
                    return new InitialHandshakeMessage();
            }
        }
        throw new IllegalArgumentException("Unexpected message code");
    }
}
