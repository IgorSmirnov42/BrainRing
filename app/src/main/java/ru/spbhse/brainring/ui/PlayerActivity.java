package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.managers.LocalPlayerGameManager;
import ru.spbhse.brainring.utils.LocalGameRoles;

/** This activity is for maintaining player in local mode */
public class PlayerActivity extends AppCompatActivity {
    private LocalPlayerGameManager manager;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        LocalGameRoles colorName = (LocalGameRoles) getIntent().getSerializableExtra("color");
        String serverIp = getIntent().getStringExtra("serverIp");
        manager = new LocalPlayerGameManager(this, colorName);

        Button button = findViewById(R.id.buttonPush);
        button.setOnClickListener(v -> manager.getLogic().answerButtonPushed());
        if (colorName == LocalGameRoles.ROLE_RED) {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCardinal));
        } else {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorJungleGreen));
        }

        manager.getNetwork().connect(serverIp);
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        manager.finishGame();
        super.onStop();
    }

    public void makeToast(String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_LONG).show());
    }

    /** When back button is pressed, asks if user is sure to leave the game and did not pressed it accidentally */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.out_of_local))
                .setMessage(getString(R.string.want_out))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    manager.finishGame();
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                })
                .show();
    }
}
