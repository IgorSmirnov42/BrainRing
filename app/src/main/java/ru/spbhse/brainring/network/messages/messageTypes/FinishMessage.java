package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.OnlineFinishCodes;

public class FinishMessage extends Message {
    private int finishCode;

    public int getFinishCode() {
        return finishCode;
    }

    public FinishMessage(int finishCode) {
        super(MessageCodes.FINISH);
        this.finishCode = finishCode;
    }

    public FinishMessage(@NonNull DataInputStream inputStream) throws IOException {
        super(MessageCodes.FINISH);
        finishCode = inputStream.readInt();
    }

    public String getFinishMessage() {
        String message;
        switch (finishCode) {
            case OnlineFinishCodes.UNSUCCESSFUL_HANDSHAKE:
                message = OnlineController.getOnlineGameActivity()
                        .getString(R.string.slow_connection);
                break;
            case OnlineFinishCodes.SERVER_TIMER_TIMEOUT:
                message = OnlineController.getOnlineGameActivity()
                        .getString(R.string.timeout);
                break;
            case OnlineFinishCodes.GAME_FINISHED_CORRECTLY_LOST:
                message = OnlineController.getOnlineGameActivity()
                        .getString(R.string.lost);
                break;
            case OnlineFinishCodes.GAME_FINISHED_CORRECTLY_WON:
                message = OnlineController.getOnlineGameActivity()
                        .getString(R.string.win);
                break;
            default:
                message = OnlineController.getOnlineGameActivity()
                        .getString(R.string.unknown_error);
        }
        return message;
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.writeInt(finishCode).toByteArray();
    }
}
