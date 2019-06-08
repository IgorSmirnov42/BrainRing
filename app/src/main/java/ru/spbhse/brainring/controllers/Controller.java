package ru.spbhse.brainring.controllers;

import android.app.Activity;
import android.support.annotation.Nullable;

/** Provides interface for different classes to access each other information */
public class Controller {
    /** App tag, mainly used in logging */
    public static String APP_TAG = "BrainRing";

    /**
     * Finishes the given activity
     *
     * @param activity activity to finish
     */
    protected static void finishActivity(@Nullable Activity activity) {
        if (activity != null) {
            activity.finish();
        }
    }
}
