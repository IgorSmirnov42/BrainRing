package ru.spbhse.brainring.controllers;

import android.app.Activity;

import java.util.Random;

import ru.spbhse.brainring.database.QuestionDataBase;
import ru.spbhse.brainring.utils.Question;

public class Controller {

    protected static void finishActivity(Activity activity) {
        if (activity != null) {
            activity.finish();
        }
    }
}
