package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.TrainingController;
import ru.spbhse.brainring.database.QuestionDataBase;
import ru.spbhse.brainring.utils.DataBaseTableEntry;

public class TrainingGameActivity extends GameActivity {
    public QuestionDataBase dataBase;
    private static DataBaseTableEntry table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /////////////
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /////////////
        super.onCreate(savedInstanceState);

        TrainingController.setUI(TrainingGameActivity.this);

        dataBase = new QuestionDataBase(TrainingGameActivity.this);
        DatabaseController.setDatabase(dataBase);

        String packageAddress = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        try {
            if (packageAddress == null) {
                table = dataBase.getBaseTable();
            } else {
                table = new DataBaseTableEntry(new URL(packageAddress));
            }
        } catch (MalformedURLException ignored) {
        }
        dataBase.setBaseTable(table);
        dataBase.createTable(table);
        gameController = TrainingController.TrainingLogicController.getInstance();

        TrainingController.createTrainingGame();
    }

    @Override
    protected void onStop() {
        Log.d("BrainRing", "Stop training game");
        super.onStop();
        TrainingController.TrainingLogicController.finishGame();
        //TODO: придумать что с этим делать
        // dataBase.deleteEntries(table);
    }

    @Override
    public void onNewQuestion() {
        setOpponentAnswer("");
        setTime("");
        buttonText = "ЖМЯК!!";
    }
}
