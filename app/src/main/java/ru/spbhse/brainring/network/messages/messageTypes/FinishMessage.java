package ru.spbhse.brainring.network.messages.messageTypes;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.OnlineFinishCodes;

/**
 * Message that can be sent by any user to any in online game signalizing that game is finished
 * Format: [int : finishCode]
 *      finishCode -- see {@code OnlineFinishCodes}
 */
public class FinishMessage extends Message {
    private int finishCode;

    public FinishMessage(int finishCode) {
        super(MessageCodes.FINISH);
        this.finishCode = finishCode;
    }

    public FinishMessage(@NonNull DataInputStream inputStream) throws IOException {
        super(MessageCodes.FINISH);
        finishCode = inputStream.readInt();
    }


    /** Returns human readable description of finish code */
    public String getFinishMessage(Context context) {
        String message;
        switch (finishCode) {
            case OnlineFinishCodes.UNSUCCESSFUL_HANDSHAKE:
                message = context.getString(R.string.slow_connection);
                break;
            case OnlineFinishCodes.SERVER_TIMER_TIMEOUT:
                message = context.getString(R.string.timeout);
                break;
            case OnlineFinishCodes.GAME_FINISHED_CORRECTLY_LOST:
                message = context.getString(R.string.lost);
                break;
            case OnlineFinishCodes.GAME_FINISHED_CORRECTLY_WON:
                message = context.getString(R.string.win);
                break;
            default:
                message = context.getString(R.string.unknown_error);
        }
        return message;
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.writeInt(finishCode).toByteArray();
    }
}
