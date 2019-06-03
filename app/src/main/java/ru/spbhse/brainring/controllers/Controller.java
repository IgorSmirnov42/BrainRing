package ru.spbhse.brainring.controllers;

import android.app.Activity;
import android.support.annotation.Nullable;

public class Controller {

    protected static void finishActivity(@Nullable Activity activity) {
        if (activity != null) {
            activity.finish();
        }
    }
}
