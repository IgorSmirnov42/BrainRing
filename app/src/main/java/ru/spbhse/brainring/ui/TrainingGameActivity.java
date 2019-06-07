package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.GameController;
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
        super.onCreate(savedInstanceState);
        drawLocation();

        TrainingController.setUI(TrainingGameActivity.this);

        dataBase = new QuestionDatabase(TrainingGameActivity.this);
        DatabaseController.setDatabase(dataBase);

        String packageAddress = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        int readingTime = getIntent().getIntExtra(Intent.EXTRA_TEXT, TrainingPlayerLogic.DEFAULT_READING_TIME);
        try {
            if (packageAddress.equals(getResources().getString(R.string.base_package))) {
                gameTable = dataBase.getBaseTable();
            } else {
                toClear = true;
                gameTable = new DatabaseTable(new URL(packageAddress));
            }
        } catch (MalformedURLException ignored) {
        }
        dataBase.setGameTable(gameTable);
        ProgressBar spinner = findViewById(R.id.progressSpinner);
        if (spinner == null) {
            throw new IllegalStateException();
        }
        GameController gameController = TrainingController.TrainingLogicController.getInstance();
        setGameController(gameController);
        setMyNick("Правильных ответов");
        setOpponentNick("Неправильных ответов");
        TrainingController.createTrainingGame();
        TrainingController.TrainingLogicController.setReadingTime(readingTime);
        new LoadPackageTask(this, spinner).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    @Override
    protected void drawLocation() {
        super.drawLocation();

        if (currentLocation == GameActivityLocation.GAME_WAITING_START) {
            TextView waitingForOpponent = findViewById(R.id.waitingStartInfo);
            waitingForOpponent.setVisibility(View.INVISIBLE);
        }

        if (currentLocation == GameActivityLocation.SHOW_ANSWER) {
            Button continueGameButton = findViewById(R.id.continueGameButton);
            continueGameButton.setOnClickListener(v ->
                    TrainingController.TrainingLogicController.newQuestion());
        }
    }

    public void setGameController(GameController newGameController) {
        gameController = newGameController;
    }

    private static class LoadPackageTask extends AsyncTask<Void, Void, String> {
        private WeakReference<TrainingGameActivity> trainingGameActivity;
        private QuestionDatabase dataBase;
        private ProgressBar spinner;

        private LoadPackageTask(TrainingGameActivity activity, ProgressBar spinner) {
            trainingGameActivity = new WeakReference<>(activity);
            dataBase = trainingGameActivity.get().dataBase;
            this.spinner = spinner;
        }

        @Override
        protected void onPreExecute() {
            spinner.setIndeterminate(true);
        }

        @Override
        protected String doInBackground(Void... voids) {
            DatabaseTable gameTable = trainingGameActivity.get().getGameTable();
            dataBase.createTable(gameTable);
            return "finished";
        }

        @Override
        protected void onPostExecute(String result) {
            spinner.setVisibility(View.INVISIBLE);
            TrainingController.TrainingLogicController.newQuestion();
        }
    }

    private DatabaseTable getGameTable() {
        return gameTable;
    }
}
