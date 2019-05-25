package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.MalformedURLException;
import java.net.URL;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.utils.DataBaseTableEntry;

public class TrainingGamePreparationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_training_packages);
        String[] packageName = { "" };

        Button startTrainingGameButton = findViewById(R.id.startTrainingGameButton);
        startTrainingGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingGamePreparationActivity.this, TrainingGameActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, packageName[0]);
                startActivity(intent);
            }
        });

        EditText urlEditor = findViewById(R.id.urlEditor);
        Button urlWrittenButton = findViewById(R.id.urlWrittenButton);
        urlWrittenButton.setOnClickListener(
                v -> {
                    try {
                        URL tmp = new URL(urlEditor.getText().toString());
                        packageName[0] = tmp.toString();
                        urlEditor.getText().clear();
                    } catch (MalformedURLException e) {
                        urlEditor.getText().clear();
                    }
                });
    }
}
