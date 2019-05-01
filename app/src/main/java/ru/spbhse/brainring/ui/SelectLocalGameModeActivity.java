package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ru.spbhse.brainring.R;

public class SelectLocalGameModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_game_settings);

        Button juryButton = findViewById(R.id.juryModeButton);
        juryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectLocalGameModeActivity.this, JuryActivity.class);
                startActivity(intent);
            }
        });

        Button playerButton = findViewById(R.id.playerModeButton);
        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectLocalGameModeActivity.this, PlayerActivity.class);
                startActivity(intent);
            }
        });
    }
}
