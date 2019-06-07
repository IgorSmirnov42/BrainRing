package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import ru.spbhse.brainring.R;

public class OnlineGameFinished extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game_finished);

        String message = getIntent().getStringExtra("message");

        TextView messageView = findViewById(R.id.gameResult);
        messageView.setText(message);

        Button toSelectOnlineOpponentButton = findViewById(R.id.toSelectOnlineOpponent);
        toSelectOnlineOpponentButton.setOnClickListener(v -> {
            // Back to select opponent activity
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Disabled
    }
}
