package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.utils.LocalGameRoles;

/**
 * This is activity for judge in local game.
 * Contains info about who is answering now, and suggests judge to accept or reject the answer
 */
public class JudgingActivity extends AppCompatActivity {
    public static final int RESULT_ACCEPTED = 1;
    public static final int RESULT_REJECTED = 0;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judging);

        LocalGameRoles colorName = (LocalGameRoles) getIntent().getSerializableExtra("color");
        String playerName;
        TextView answering = findViewById(R.id.answeringId);
        int color;
        if (colorName == LocalGameRoles.ROLE_RED) {
            color = ContextCompat.getColor(this, R.color.colorCardinal);
            playerName = getString(R.string.red);
        } else {
            color = ContextCompat.getColor(this, R.color.colorJungleGreen);
            playerName = getString(R.string.green);
        }
        String text = getString(R.string.pressed) + " " + playerName + " " + getString(R.string.button);
        answering.setText(text);
        answering.setTextColor(color);

        Button acceptButton = findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(v -> {
            setResult(RESULT_ACCEPTED);
            JudgingActivity.this.finish();
        });

        Button rejectButton = findViewById(R.id.rejectButton);
        rejectButton.setOnClickListener(v -> {
            setResult(RESULT_REJECTED);
            JudgingActivity.this.finish();
        });
    }

    /** Back button is disabled in this activity */
    @Override
    public void onBackPressed() {
        // Disabled
    }
}
