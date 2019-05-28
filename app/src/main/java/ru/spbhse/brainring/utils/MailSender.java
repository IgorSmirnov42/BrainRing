package ru.spbhse.brainring.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

public class MailSender {
    public static void sendMail(@NonNull Context context,
                                @NonNull String subject,
                                @NonNull String message) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + "ismirnov.testing@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "Не найдено приложений для отправки электронных сообщений.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
