package ru.spbhse.brainring.network.messages;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessageGenerator {

    private DataOutputStream outputStream;
    private ByteArrayOutputStream byteArrayOutputStream;

    private MessageGenerator() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        outputStream = new DataOutputStream(byteArrayOutputStream);
    }

    @NonNull
    public static MessageGenerator create() {
        return new MessageGenerator();
    }

    @NonNull
    public MessageGenerator writeString(@NonNull String string) {
        try {
            outputStream.writeUTF(string);
        } catch (IOException e) {
            Log.wtf("BrainRing", "Error on writing");
            e.printStackTrace();
        }
        return this;
    }

    @NonNull
    public MessageGenerator writeLong(long longValue) {
        try {
            outputStream.writeLong(longValue);
        } catch (IOException e) {
            Log.wtf("BrainRing", "Error on writing");
            e.printStackTrace();
        }
        return this;
    }

    @NonNull
    public MessageGenerator writeInt(int intValue) {
        try {
            outputStream.writeInt(intValue);
        } catch (IOException e) {
            Log.wtf("BrainRing", "Error on writing");
            e.printStackTrace();
        }
        return this;
    }

    @NonNull
    public byte[] toByteArray() {
        try {
            outputStream.flush();
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            Log.wtf("BrainRing", "Error on flushing");
            e.printStackTrace();
        }
        byte[] result = byteArrayOutputStream.toByteArray();
        try {
            outputStream.close();
        } catch (IOException e) {
            Log.wtf("BrainRing", "Error on closing");
            e.printStackTrace();
        }
        return result;
    }
}
