package ru.spbhse.brainring;

import java.lang.ref.WeakReference;

import ru.spbhse.brainring.ui.TrainingGameActivity;

public class TrainingController {
    private static WeakReference<TrainingGameActivity> trainingGameActivity;

    public static void setUI(TrainingGameActivity ui) {
        trainingGameActivity = new WeakReference<>(ui);
    }
    
    public static TrainingGameActivity getTrainingGameActivity() {
        return trainingGameActivity.get();
    }


    public static void createTrainingGame() {

    }
}
