package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.managers.LocalAdminGameManager;
import ru.spbhse.brainring.utils.Constants;
import ru.spbhse.brainring.utils.LocalGameRoles;

/** This activity is for maintaining score of teams in local game */
public class JuryActivity extends AppCompatActivity {
    private TextView statusText;
    private Button mainButton;
    private TextView redTeamScore;
    private TextView greenTeamScore;
    private TextView greenStatus;
    private TextView redStatus;
    private LocalAdminGameManager manager;
    private boolean judging = false;
    private LocalGameLocation currentLocation = LocalGameLocation.GAME_WAITING_START;

    private static final int RC_ANSWER_JUDGED = 43;

    private final View.OnClickListener longerClick = v -> {
        Toast toast = Toast.makeText(JuryActivity.this, getString(R.string.press_longer),
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    };

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jury);
        int firstTimer = getIntent().getIntExtra("firstTimer", 20);
        int secondTimer = getIntent().getIntExtra("secondTimer", 20);
        manager = new LocalAdminGameManager(this, firstTimer, secondTimer);

        ImageView redTeam = findViewById(R.id.redTeamStatus);
        redTeam.setOnClickListener(v -> {
            if (currentLocation == LocalGameLocation.NOT_STARTED &&
                    !manager.getNetwork().hasSpeedTest()) {
                manager.getNetwork().speedTest(LocalGameRoles.ROLE_RED);
            } else {
                makeToast("Невозможно провести тестирование скорости в данный момент.");
            }
        });

        ImageView greenTeam = findViewById(R.id.greenTeamStatus);
        greenTeam.setOnClickListener(v -> {
            if (currentLocation == LocalGameLocation.NOT_STARTED &&
                    !manager.getNetwork().hasSpeedTest()) {
                manager.getNetwork().speedTest(LocalGameRoles.ROLE_GREEN);
            } else {
                makeToast("Невозможно провести тестирование скорости в данный момент.");
            }
        });

        statusText = findViewById(R.id.gameStatusInfo);
        mainButton = findViewById(R.id.mainButton);
        mainButton.setOnLongClickListener(v -> {
            if (!manager.getLogic().toNextState() || manager.getNetwork().hasSpeedTest()) {
                Toast.makeText(JuryActivity.this, getString(R.string.cannot_switch),
                        Toast.LENGTH_LONG).show();
            }
            return true;
        });
        mainButton.setOnClickListener(longerClick);

        Button minusGreenButton = findViewById(R.id.minusGreenTeamButton);
        minusGreenButton.setOnLongClickListener(v -> {
            manager.getLogic().minusPoint(1);
            return true;
        });
        minusGreenButton.setOnClickListener(longerClick);

        Button plusGreenButton = findViewById(R.id.plusGreenTeamButton);
        plusGreenButton.setOnLongClickListener(v -> {
            manager.getLogic().plusPoint(1);
            return true;
        });
        plusGreenButton.setOnClickListener(longerClick);

        Button minusRedButton = findViewById(R.id.minusRedTeamButton);
        minusRedButton.setOnLongClickListener(v -> {
            manager.getLogic().minusPoint(2);
            return true;
        });
        minusRedButton.setOnClickListener(longerClick);

        Button plusRedButton = findViewById(R.id.plusRedTeamButton);
        plusRedButton.setOnLongClickListener(v -> {
            manager.getLogic().plusPoint(2);
            return true;
        });
        plusRedButton.setOnClickListener(longerClick);

        greenTeamScore = findViewById(R.id.greenTeamScore);
        redTeamScore = findViewById(R.id.redTeamScore);

        greenStatus = findViewById(R.id.greenStatus);
        redStatus = findViewById(R.id.redStatus);

        redrawLocation();

        manager.getNetwork().getIp();
    }

    public void onIpReceived(String ip) {
        String text = "Ваш IP:\n" + ip;
        statusText.setText(text);
        manager.getNetwork().startServer();
    }

    /** Sets current location */
    public void setLocation(LocalGameLocation location) {
        runOnUiThread(() -> {
            currentLocation = location;
            redrawLocation();
        });
    }

    /** Redraws activity, based on current location */
    public void redrawLocation() {
        runOnUiThread(() -> {
            greenTeamScore.setText(manager.getLogic().getGreenScore());
            redTeamScore.setText(manager.getLogic().getRedScore());
            if (currentLocation == LocalGameLocation.GAME_WAITING_START) {
                statusText.setText(getString(R.string.check_connection));
                mainButton.setVisibility(View.GONE);
            }
            if (currentLocation == LocalGameLocation.NOT_STARTED) {
                statusText.setText(getString(R.string.both_connected));
                mainButton.setText(getString(R.string.begin_reading));
                mainButton.setVisibility(View.VISIBLE);
            }
            if (currentLocation == LocalGameLocation.READING_QUESTION) {
                statusText.setText(getString(R.string.reading_question));
                mainButton.setText(getString(R.string.start_timer));
                mainButton.setVisibility(View.VISIBLE);
            }
            if (currentLocation == LocalGameLocation.COUNTDOWN) {
                mainButton.setText(getString(R.string.stop_timer));
                mainButton.setVisibility(View.VISIBLE);
            }
            if (currentLocation == LocalGameLocation.ONE_IS_ANSWERING) {
                statusText.setText(getString(R.string.something_wrong));
                mainButton.setVisibility(View.GONE);
            }
        });
    }

    /** Reacts on pressing the answer button from some team*/
    public void onReceivingAnswer(LocalGameRoles color) {
        runOnUiThread(() -> {
            judging = true;
            Intent intent = new Intent(JuryActivity.this, JudgingActivity.class);
            intent.putExtra("color", color);
            startActivityForResult(intent, RC_ANSWER_JUDGED);
        });
    }

    /** Sets green team status */
    public void setGreenStatus(String status) {
        runOnUiThread(() -> greenStatus.setText(status));
    }

    /** Sets red team status */
    public void setRedStatus(String status) {
        runOnUiThread(() -> redStatus.setText(status));
    }

    /** Sets remaining time */
    public void showTime(long time) {
        runOnUiThread(() -> statusText.setText(String.valueOf(time)));
    }

    public void makeToast(String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_LONG).show());
    }


    /** {@inheritDoc} */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ANSWER_JUDGED) {
            judging = false;
            if (resultCode == JudgingActivity.RESULT_REJECTED) {
                manager.getLogic().onRejectAnswer();
            } else if (resultCode == JudgingActivity.RESULT_ACCEPTED) {
                manager.getLogic().onAcceptAnswer();
            } else {
                Log.wtf(Constants.APP_TAG, "Unexpected result code");
            }
        }
    }

    /** {@inheritDoc} */
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

    @Override
    public void onStop() {
        if (!judging) {
            manager.finishGame();
        }
        super.onStop();
    }
}
