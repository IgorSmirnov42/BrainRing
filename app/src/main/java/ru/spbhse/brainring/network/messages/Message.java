package ru.spbhse.brainring.network.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message {
    public static final int ANSWER_IS_READY = 0;
    public static final int ANSWER_IS_WRITTEN = 1;
    public static final int FORBIDDEN_TO_ANSWER = 2;
    public static final int ALLOWED_TO_ANSWER = 3;
    public static final int SENDING_QUESTION = 4;
    public static final int SENDING_INCORRECT_OPPONENT_ANSWER = 5;
    public static final int SENDING_CORRECT_ANSWER_AND_SCORE = 6;
    public static final int OPPONENT_IS_ANSWERING = 7;
    public static final int TIME_LIMIT = 8;
    public static final int FALSE_START = 9;
    public static final int TIME_START = 10;
    public static final int FINISH = 11;
    public static final int HANDSHAKE = 12;
    public static final int CORRECT_ANSWER = 13;

    public static String readString(DataInputStream is) throws IOException {
        StringBuilder string = new StringBuilder();
        while (is.available() > 0) {
            string.append(is.readChar());
        }
        return string.toString();
    }

    /** Generates byte message by code and body */
    public static byte[] generateMessage(int code, String message) {
        byte[] buf;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeInt(code);
            dout.writeChars(message);
            dout.flush();
            buf = bout.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            buf = null;
        }
        return buf;
    }

    /** Generates byte message by code and long value in body*/
    public static byte[] generateMessageLongBody(int code, long message) {
        byte[] buf;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeInt(code);
            dout.writeLong(message);
            dout.flush();
            buf = bout.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            buf = null;
        }
        return buf;
    }
}
