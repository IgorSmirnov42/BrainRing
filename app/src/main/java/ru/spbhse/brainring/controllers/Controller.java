package ru.spbhse.brainring.controllers;

import android.app.Activity;
import android.support.annotation.Nullable;

public class Controller {
    public static final String APP_TAG = "BrainRing";

    protected static void finishActivity(@Nullable Activity activity) {
        if (activity != null) {
            activity.finish();
        }
    }
}
