package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.TrainingController;
import ru.spbhse.brainring.database.QuestionDataBase;
import ru.spbhse.brainring.utils.DataBaseTableEntry;

import static ru.spbhse.brainring.ui.GameActivityLocation.GAME_WAITING_START;
import static ru.spbhse.brainring.ui.GameActivityLocation.OPPONENT_IS_ANSWERING;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_ANSWER;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_QUESTION;
import static ru.spbhse.brainring.ui.GameActivityLocation.WRITE_ANSWER;

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
        gameController = new TrainingController.TrainingLogicController();

        TrainingController.createTrainingGame();
    }

    /*public String getWhatWritten() {
        EditText answerEditor = findViewById(R.id.answerEditor);
        if (answerEditor != null) {
            return answerEditor.getText().toString();
        } else {
            Log.wtf("BrainRing", "Answer editing wasn't open but should");
            return "";
        }
    }*/

    @Override
    protected void onStop() {
        Log.d("BrainRing", "Stop training game");
        super.onStop();
        TrainingController.TrainingLogicController.finishGame();
        //dataBase.deleteEntries(table);
    }
}
