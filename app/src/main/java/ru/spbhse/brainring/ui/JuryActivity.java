package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.LocalController;

public class JuryActivity extends AppCompatActivity {
    private TextView statusText;
    private Button mainButton;
    private TextView redTeamScore;
    private TextView greenTeamScore;
    private TextView greenStatus;
    private TextView redStatus;

    private LocalGameLocation currentLocation = LocalGameLocation.GAME_WAITING_START;

    private static final int RC_SIGN_IN = 42;

    private final View.OnClickListener longerClick = v -> {
        Toast toast = Toast.makeText(JuryActivity.this, getString(R.string.press_longer),
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jury);
        LocalController.setUI(this);
        LocalController.initializeLocalGame(getIntent().getIntExtra("firstTimer", 20),
                getIntent().getIntExtra("secondTimer", 20));
        statusText = findViewById(R.id.gameStatusInfo);
        mainButton = findViewById(R.id.mainButton);
        mainButton.setOnLongClickListener(v -> {
            if (!LocalController.LocalAdminLogicController.toNextState()) {
                Toast.makeText(JuryActivity.this, getString(R.string.cannot_switch),
                        Toast.LENGTH_LONG).show();
            }
            return true;
        });
        mainButton.setOnClickListener(longerClick);

        Button minusGreenButton = findViewById(R.id.minusGreenTeamButton);
        minusGreenButton.setOnLongClickListener(v -> {
            LocalController.LocalAdminLogicController.minusPoint(1);
            return true;
        });
        minusGreenButton.setOnClickListener(longerClick);

        Button plusGreenButton = findViewById(R.id.plusGreenTeamButton);
        plusGreenButton.setOnLongClickListener(v -> {
            LocalController.LocalAdminLogicController.plusPoint(1);
            return true;
        });
        plusGreenButton.setOnClickListener(longerClick);

        Button minusRedButton = findViewById(R.id.minusRedTeamButton);
        minusRedButton.setOnLongClickListener(v -> {
            LocalController.LocalAdminLogicController.minusPoint(2);
            return true;
        });
        minusRedButton.setOnClickListener(longerClick);

        Button plusRedButton = findViewById(R.id.plusRedTeamButton);
        plusRedButton.setOnLongClickListener(v -> {
            LocalController.LocalAdminLogicController.plusPoint(2);
            return true;
        });
        plusRedButton.setOnClickListener(longerClick);

        greenTeamScore = findViewById(R.id.greenTeamScore);
        redTeamScore = findViewById(R.id.redTeamScore);

        greenStatus = findViewById(R.id.greenStatus);
        redStatus = findViewById(R.id.redStatus);

        redrawLocation();
        statusText.setText("Ожидаем подключение игроков");

        LocalController.LocalNetworkAdminController.createLocalGame();
    }

    public void setLocation(LocalGameLocation location) {
        currentLocation = location;
        redrawLocation();
    }

    public void redrawLocation() {
        greenTeamScore.setText(LocalController.LocalAdminLogicController.getGreenScore());
        redTeamScore.setText(LocalController.LocalAdminLogicController.getRedScore());
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
    }

    public void onReceivingAnswer(String color) {
        Intent intent = new Intent(JuryActivity.this, Judging.class);
        intent.putExtra("color", color);
        startActivity(intent);
    }

    public void setGreenStatus(String status) {
        greenStatus.setText(status);
    }

    public void setRedStatus(String status) {
        redStatus.setText(status);
    }

    public void showTime(long time) {
        statusText.setText(String.valueOf(time));
    }

    /* I know that this function is out of content here,
       but it is linked with onActivityResult that can be placed only here */
    public void signIn() {
        GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            LocalController.LocalNetworkController.loggedIn(account);
        } else {
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                    GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                LocalController.LocalNetworkController.loggedIn(result.getSignInAccount());
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.login_fail);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.out_of_local))
                .setMessage(getString(R.string.want_out))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    LocalController.finishLocalGame(false);
                    super.onBackPressed();
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                })
                .show();
    }
}
