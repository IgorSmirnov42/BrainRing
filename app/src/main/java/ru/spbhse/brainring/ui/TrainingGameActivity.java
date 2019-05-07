package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.R;
import ru.spbhse.brainring.TrainingController;
import ru.spbhse.brainring.database.QuestionDataBase;

public class TrainingGameActivity extends GameActivity {
    public QuestionDataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /////////////
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /////////////
        super.onCreate(savedInstanceState);

        currentLocation = GameActivityLocation.SHOW_QUESTION;

        TrainingController.setUI(TrainingGameActivity.this);
        dataBase = new QuestionDataBase(TrainingGameActivity.this);
        dataBase.openDataBase();

        drawLocation();

        TrainingController.createTrainingGame();
    }
}
