package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.utils.LocalGameRoles;

/** This activity is for selecting user role in local game mode, and selecting time spent on answer */
public class SelectLocalGameModeActivity extends AppCompatActivity {
    private int firstValue = 20;
    private int secondValue = 20;
    private static final int BAR_GRADE = 10;
    private static final int BAR_START = 10;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_game_settings);

        SeekBar firstBar = findViewById(R.id.firstCounterBar);
        firstBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                firstValue = BAR_START + progress * BAR_GRADE;
                reloadValues();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar secondBar = findViewById(R.id.secondCounterBar);
        secondBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                secondValue = BAR_START + progress * BAR_GRADE;
                reloadValues();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button juryButton = findViewById(R.id.readyButton);
        juryButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectLocalGameModeActivity.this,
                    JuryActivity.class);
            intent.putExtra("firstTimer", firstValue);
            intent.putExtra("secondTimer", secondValue);
            startActivity(intent);
        });

        Button greenButton = findViewById(R.id.greenPlayerButton);
        greenButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectLocalGameModeActivity.this,
                    WriteIPActivity.class);
            intent.putExtra("color", LocalGameRoles.ROLE_GREEN);
            startActivity(intent);
        });

        Button redButton = findViewById(R.id.redPlayerButton);
        redButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectLocalGameModeActivity.this,
                    WriteIPActivity.class);
            intent.putExtra("color", LocalGameRoles.ROLE_RED);
            startActivity(intent);
        });
    }

    private void reloadValues() {
        TextView first = findViewById(R.id.firstCounterValue);
        first.setText(String.valueOf(firstValue));
        TextView second = findViewById(R.id.secondCounterValue);
        second.setText(String.valueOf(secondValue));
    }
}
