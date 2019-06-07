package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import ru.spbhse.brainring.R;

public class GameFinished extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_finished);

        String message = getIntent().getStringExtra("message");

        TextView messageView = findViewById(R.id.gameResult);
        messageView.setText(message);

        Button toGameStart = findViewById(R.id.toGameStart);
        toGameStart.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Disabled
    }
}
