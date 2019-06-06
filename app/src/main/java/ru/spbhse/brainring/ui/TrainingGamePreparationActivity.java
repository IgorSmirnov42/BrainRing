package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.logic.TrainingPlayerLogic;

public class TrainingGamePreparationActivity extends AppCompatActivity {
    private int counter = 10;
    private String packageAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_training_packages);
//        String[] packageAddress = { null };
//        int counter = 0;

        Button startTrainingGameButton = findViewById(R.id.startTrainingGameButton);
        startTrainingGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingGamePreparationActivity.this, TrainingGameActivity.class);
                intent.putExtra(Intent.EXTRA_TITLE, packageAddress);
                intent.putExtra(Intent.EXTRA_TEXT, counter);
                startActivity(intent);
                packageAddress = null;
            }
        });

        EditText urlEditor = findViewById(R.id.urlEditor);
        Button urlWrittenButton = findViewById(R.id.urlWrittenButton);
        urlWrittenButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            URL hyperlink = new URL(urlEditor.getText().toString());
                            packageAddress = hyperlink.toString();
                            TextView currentPackage = findViewById(R.id.currentPackageName);
                            currentPackage.setText(hyperlink.getFile());
                            urlEditor.getText().clear();
                        } catch (MalformedURLException e) {
                            urlEditor.getText().clear();
                            Toast.makeText(TrainingGamePreparationActivity.this,
                                    "Пакет по данной ссылке не найден, попробуйте еще раз",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        SeekBar timeCounterBar = findViewById(R.id.trainingCounterBar);
        TextView timeCounterValue = findViewById(R.id.trainingCounterValue);
        timeCounterBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                counter = TrainingPlayerLogic.DEFAULT_READING_TIME + progress * 5;
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
