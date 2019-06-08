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
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.GameController;
import ru.spbhse.brainring.controllers.TrainingController;
import ru.spbhse.brainring.database.DatabaseTable;
import ru.spbhse.brainring.database.QuestionDatabase;
import ru.spbhse.brainring.logic.TrainingPlayerLogic;

/** This activity maintains training game */
public class TrainingGameActivity extends GameActivity {
    private QuestionDatabase dataBase;
    private boolean toClear = false;
    private static DatabaseTable gameTable;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawLocation();

        TrainingController.setUI(TrainingGameActivity.this);

        dataBase = QuestionDatabase.getInstance(TrainingGameActivity.this);
        DatabaseController.setDatabase(dataBase);

        String packageAddress = getIntent().getStringExtra(Intent.EXTRA_TITLE);
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
        setMyNick(getString(R.string.right_answers));
        setOpponentNick(getString(R.string.wrong_answers));

        new LoadPackageTask(this, spinner).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        Log.d(Controller.APP_TAG, "Stop training game");
        super.onStop();
        TrainingController.TrainingLogicController.finishGame();
        if (toClear) {
            dataBase.deleteEntries(gameTable);
        }
        toClear = false;
        finish();
    }

    /** {@inheritDoc} */
    @Override
    public void onNewQuestion() {
        setOpponentAnswer("");
        setTime("");
        buttonText = getString(R.string.button_push_text);
        drawLocation();
        findViewById(R.id.textView).setVisibility(View.GONE);
    }

    /** {@inheritDoc} */
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

    /** Sets GameController */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /** Reacts on finishing the game */
    public void onGameFinished() {
        Intent intent = new Intent(this, AfterGameActivity.class);
        String total = String.valueOf(Integer.parseInt(opponentScore) + Integer.parseInt(myScore));
        String endGameMessage = getString(R.string.answered_right_on) +
                " " + myScore + " " +
                getGrammarCorrect(myScore) +
                " " + total + ".";
        intent.putExtra(Intent.EXTRA_TEXT, endGameMessage);
        startActivity(intent);
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
            TrainingController.createTrainingGame();
            int readingTime = trainingGameActivity.get().getIntent().
                    getIntExtra(Intent.EXTRA_TEXT, TrainingPlayerLogic.DEFAULT_READING_TIME);
            TrainingController.TrainingLogicController.setReadingTime(readingTime);
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

    private String getGrammarCorrect(String s) {
        int number = Integer.parseInt(s);
        number %= 100;
        String question = "вопрос";
        if (10 <= number && number <= 20 ) {
            return question + "ов";
        } else {
            number %= 10;
            switch (number) {
                case 0:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    return question + "ов";
                case 1:
                    return question;
                case 2:
                case 3:
                case 4:
                    return question + "а";
            }
        }
        return question;
    }
}
