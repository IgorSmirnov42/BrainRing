package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.GameController;
import ru.spbhse.brainring.controllers.TrainingController;
import ru.spbhse.brainring.database.DatabaseTable;
import ru.spbhse.brainring.database.QuestionDatabase;

public class LaunchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ProgressBar spinner = findViewById(R.id.progressSpinner1);
        spinner.setIndeterminate(true);
        spinner.setVisibility(View.VISIBLE);
        new LoadDatabaseTask(this, spinner).execute();
    }

    private static class LoadDatabaseTask extends AsyncTask<Void, Void, String> {
        private WeakReference<LaunchActivity> launchActivity;
        private QuestionDatabase dataBase;
        private ProgressBar spinner;

        private LoadDatabaseTask(LaunchActivity activity, ProgressBar spinner) {
            launchActivity = new WeakReference<>(activity);
            this.spinner = spinner;
        }

        @Override
        protected String doInBackground(Void... voids) {
            dataBase = new QuestionDatabase(launchActivity.get());
            dataBase.createTable(dataBase.getBaseTable());
            return "finished";
        }

        @Override
        protected void onPostExecute(String result) {
            spinner.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(launchActivity.get(), MainActivity.class);
            launchActivity.get().startActivity(intent);
            launchActivity.get().finish();
        }
    }
}
