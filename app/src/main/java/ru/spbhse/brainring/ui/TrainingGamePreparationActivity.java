package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.logic.TrainingPlayerLogic;

public class TrainingGamePreparationActivity extends AppCompatActivity {
    private int counter = 10;
    private String packageName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_training_packages);
//        String[] packageName = { null };
//        int counter = 0;

        Button startTrainingGameButton = findViewById(R.id.startTrainingGameButton);
        startTrainingGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingGamePreparationActivity.this, TrainingGameActivity.class);
                intent.putExtra(Intent.EXTRA_TITLE, packageName);
                intent.putExtra(Intent.EXTRA_TEXT, counter);
                startActivity(intent);
                packageName = null;
            }
        });

        EditText urlEditor = findViewById(R.id.urlEditor);
        Button urlWrittenButton = findViewById(R.id.urlWrittenButton);
        urlWrittenButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            URL tmp = new URL(urlEditor.getText().toString());
                            packageName = tmp.toString();
                            urlEditor.getText().clear();
                        } catch (MalformedURLException e) {
                            urlEditor.getText().clear();
                        }
                    }
                });

        SeekBar timeCounterBar = findViewById(R.id.trainingCounterBar);
        TextView timeCounterValue = findViewById(R.id.trainingCounterValue);
        timeCounterBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                counter = TrainingPlayerLogic.DEFAULT_ANSWER_TIME + progress * 5;
                timeCounterValue.setText(String.valueOf(counter));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}
