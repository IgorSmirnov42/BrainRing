package ru.spbhse.brainring.controllers;

import android.app.Activity;

public class Controller {

    protected static void finishActivity(Activity activity) {
        if (activity != null) {
            activity.finish();
        }
    }
}
