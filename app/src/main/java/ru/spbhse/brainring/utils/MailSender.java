package ru.spbhse.brainring.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import ru.spbhse.brainring.R;

/** Class with static function to send mails */
public class MailSender {
    /** Allows user to choose app to send mail with. If there is no such app, shows toast */
    public static void sendMail(@NonNull Context context,
                                @NonNull String subject,
                                @NonNull String message) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + "ismirnov.testing@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        try {
            context.startActivity(Intent.createChooser(emailIntent,
                    context.getString(R.string.send_using)));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.email_not_found),
                    Toast.LENGTH_LONG).show();
        }
    }
}
