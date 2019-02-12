package ru.spbhse.brainring.network.messages;

import java.io.DataInputStream;
import java.io.IOException;

public class Message {
    /*
    От клиента к серверу:
        0. Ответ готов (id клиента)
        1. Ответ написан (строка)
    От сервера к клиенту:
        2. Отвечать запрещено
        3. Отвечать разрешено
        4. Передача вопроса (строка)
        5. Передача неправильного ответа соперника (строка)
        6. Передача ответа и счёта (два инта, строка)
        7. Передача сообщения "Соперник отвечает"
     */

    // Messages from client to server
    public static final int ANSWER_IS_READY = 0;
    public static final int ANSWER_IS_WRITTEN = 1;
    // Messages from server to client
    public static final int FORBIDDEN_TO_ANSWER = 2;
    public static final int ALLOWED_TO_ANSWER = 3;
    public static final int SENDING_QUESTION = 4;
    public static final int SENDING_INCORRECT_OPPONENT_ANSWER = 5;
    public static final int SENDING_CORRECT_ANSWER_AND_SCORE = 6;
    public static final int OPPONENT_IS_ANSWERING = 7;

    public static boolean messageIsToServer(int identifier) {
        return identifier <= 1;
    }

    public static boolean messageIsToClient(int identifier) {
        return !messageIsToServer(identifier);
    }

    public static String readString(DataInputStream is) {
        byte[] string = null;
        try {
            string = new byte[is.available()];
            is.readFully(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(string);
    }
}
