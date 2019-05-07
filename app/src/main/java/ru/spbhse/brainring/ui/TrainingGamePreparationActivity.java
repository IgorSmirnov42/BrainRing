package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ru.spbhse.brainring.R;

public class TrainingGamePreparationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_training_packages);

        Button startTrainingGameButton = findViewById(R.id.startTrainingGameButton);
        startTrainingGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingGamePreparationActivity.this, TrainingGameActivity.class);
                startActivity(intent);
            }
        });
    }
}
