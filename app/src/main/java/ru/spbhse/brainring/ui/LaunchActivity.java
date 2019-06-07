package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.database.QuestionDatabase;

public class LaunchActivity extends AppCompatActivity {
    private static final int SECOND = 1000;
    private static final int TIME_FOR_ONE_STRING = 5;
    private static final int DOTS_REDRAW_INTERVAL = 300;
    private static List<String> launchStrings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.launch_strings));
        while (scanner.hasNext()) {
            launchStrings.add(scanner.nextLine());
        }

        ProgressBar spinner = findViewById(R.id.launchProgressSpinner);
        spinner.setIndeterminate(true);
        spinner.setVisibility(View.VISIBLE);

        new LoadDatabaseTask(this, spinner).execute();
        startTimer(5 * TIME_FOR_ONE_STRING * SECOND);
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

    private void startTimer(long millis) {
        if (millis == 0) {
            TextView launchString = LaunchActivity.this.findViewById(R.id.launchText);
            launchString.setText("");
        }
        new CountDownTimer(TIME_FOR_ONE_STRING * SECOND, DOTS_REDRAW_INTERVAL) {
            private int dotsCount;
            private final Random RANDOM = new Random();
            private String stringToShow = launchStrings.get(RANDOM.nextInt(launchStrings.size()));
            @Override
            public void onTick(long millisUntilFinished) {
                dotsCount = (dotsCount + 1) % 4;
                String dots = "";
                switch (dotsCount) {
                    case 1:
                        dots = ".";
                        break;
                    case 2:
                        dots = "..";
                        break;
                    case 3:
                        dots = "...";
                        break;
                }

                TextView launchString = LaunchActivity.this.findViewById(R.id.launchText);
                launchString.setText(stringToShow + dots);
            }

            @Override
            public void onFinish() {
                startTimer(millis - TIME_FOR_ONE_STRING * SECOND);
            }
        }.start();
    }
}
