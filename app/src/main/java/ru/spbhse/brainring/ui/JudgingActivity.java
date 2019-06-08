package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.LocalController;
import ru.spbhse.brainring.logic.LocalGameAdminLogic;

/**
 * This is activity for judge in local game.
 * Contains info about who is answering now, and suggests judge to accept or reject the answer
 */
public class JudgingActivity extends AppCompatActivity {
    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judging);

        String colorName = getIntent().getStringExtra("color");
        String playerName;
        TextView answering = findViewById(R.id.answeringId);
        int color;
        if (colorName.equals(LocalGameAdminLogic.RED)) {
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
            LocalController.LocalAdminLogicController.onAcceptAnswer();
            JudgingActivity.this.finish();
        });

        Button rejectButton = findViewById(R.id.rejectButton);
        rejectButton.setOnClickListener(v -> {
            LocalController.LocalAdminLogicController.onRejectAnswer();
            JudgingActivity.this.finish();
        });
    }

    /** Back button is disabled in this activity */
    @Override
    public void onBackPressed() {
        // Disabled
    }
}
