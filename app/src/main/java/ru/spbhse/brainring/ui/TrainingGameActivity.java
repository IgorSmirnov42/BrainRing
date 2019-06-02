package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import java.net.MalformedURLException;
import java.net.URL;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.TrainingController;
import ru.spbhse.brainring.database.QuestionDatabase;
import ru.spbhse.brainring.database.DatabaseTable;
import ru.spbhse.brainring.logic.TrainingPlayerLogic;

public class TrainingGameActivity extends GameActivity {
    public QuestionDatabase dataBase;
    private boolean toClear = false;
    private static DatabaseTable gameTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /////////////
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /////////////
        super.onCreate(savedInstanceState);

        TrainingController.setUI(TrainingGameActivity.this);

        dataBase = new QuestionDatabase(TrainingGameActivity.this);
        DatabaseController.setDatabase(dataBase);

        String packageAddress = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        int answerTime = getIntent().getIntExtra(Intent.EXTRA_TEXT, TrainingPlayerLogic.DEFAULT_ANSWER_TIME);
        try {
            if (packageAddress == null) {
                gameTable = dataBase.getBaseTable();
            } else {
                toClear = true;
                gameTable = new DatabaseTable(new URL(packageAddress));
            }
        } catch (MalformedURLException ignored) {
        }
        dataBase.setGameTable(gameTable);
        dataBase.createTable(gameTable);
        gameController = TrainingController.TrainingLogicController.getInstance();
        setMyNick("Правильных ответов");
        setOpponentNick("Неправильных ответов");

        TrainingController.createTrainingGame();
        TrainingController.TrainingLogicController.setAnswerTime(answerTime);
    }

    @Override
    protected void onStop() {
        Log.d("BrainRing", "Stop training game");
        super.onStop();
        TrainingController.TrainingLogicController.finishGame();
        if (toClear) {
            dataBase.deleteEntries(gameTable);
        }
        toClear = false;
        finish();
    }

    @Override
    public void onNewQuestion() {
        setOpponentAnswer("");
        setTime("");
        buttonText = "ЖМЯК!!";
        drawLocation();
        findViewById(R.id.textView).setVisibility(View.GONE);
    }
}
