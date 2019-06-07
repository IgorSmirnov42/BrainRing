package ru.spbhse.brainring.network.messages;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ru.spbhse.brainring.controllers.Controller;

/**
 * Wrapper above {@code DataOutputStream} and {@code ByteArrayOutputStream} that allows to combine
 * commands to print data to byte arrays.
 * Commands may be used one by one
 * For instance to write int and string to byte array you should write following:
 * byte[] out = MessageGenerator.create().writeInt(someInt).writeString(someString).toByteArray();
 */
public class MessageGenerator {
    private DataOutputStream outputStream;
    private ByteArrayOutputStream byteArrayOutputStream;

    /** Creates new data output stream and byte array output stream */
    private MessageGenerator() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        outputStream = new DataOutputStream(byteArrayOutputStream);
    }

    /** Creates new MessageGenerator */
    @NonNull
    public static MessageGenerator create() {
        return new MessageGenerator();
    }

    /** Appends string to current stream */
    @NonNull
    public MessageGenerator writeString(@NonNull String string) {
        try {
            outputStream.writeUTF(string);
        } catch (IOException e) {
            Log.wtf(Controller.APP_TAG, "Error on writing");
            e.printStackTrace();
        }
        return this;
    }

    /** Appends long to current stream */
    @NonNull
    public MessageGenerator writeLong(long longValue) {
        try {
            outputStream.writeLong(longValue);
        } catch (IOException e) {
            Log.wtf(Controller.APP_TAG, "Error on writing");
            e.printStackTrace();
        }
        return this;
    }

    /** Appends int to current stream */
    @NonNull
    public MessageGenerator writeInt(int intValue) {
        try {
            outputStream.writeInt(intValue);
        } catch (IOException e) {
            Log.wtf(Controller.APP_TAG, "Error on writing");
            e.printStackTrace();
        }
        return this;
    }

    /** Transforms output stream to byte array. Closes output streams */
    @NonNull
    public byte[] toByteArray() {
        try {
            outputStream.flush();
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            Log.wtf(Controller.APP_TAG, "Error on flushing");
            e.printStackTrace();
        }
        byte[] result = byteArrayOutputStream.toByteArray();
        try {
            outputStream.close();
        } catch (IOException e) {
            Log.wtf(Controller.APP_TAG, "Error on closing");
            e.printStackTrace();
        }
        return result;
    }
}
