package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import ru.spbhse.brainring.R;

/** This activity is shown when online game or training game is finished. Shows the score */
public class AfterGameActivity extends AppCompatActivity {

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_finished);

        String message = getIntent().getStringExtra(Intent.EXTRA_TEXT);

        TextView messageView = findViewById(R.id.gameResult);
        messageView.setText(message);

        Button toGameStart = findViewById(R.id.toGameStart);
        toGameStart.setOnClickListener(v -> finish());
    }

    /** Back button can't be pressed in this activity */
    @Override
    public void onBackPressed() {}
}
