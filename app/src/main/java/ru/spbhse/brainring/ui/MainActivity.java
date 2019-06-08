package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;

import java.util.Objects;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;

/** This activity contains menu fields */
public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 42;
    private static final int RC_LEADERBOARD_UI = 23;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button trainButton = findViewById(R.id.trainButton);
        trainButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrainingGamePreparationActivity.class);
            startActivity(intent);
        });

        Button netButton = findViewById(R.id.netButton);
        netButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectOnlineOpponentActivity.class);
            startActivity(intent);
        });

        Button localButton = findViewById(R.id.localButton);
        localButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectLocalGameModeActivity.class);
            startActivity(intent);
        });

        Button complainButton = findViewById(R.id.complainButton);
        complainButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ComplainActivity.class);
            startActivity(intent);
        });

        Button ratingButton = findViewById(R.id.ratingButton);
        ratingButton.setOnClickListener(v -> signIn());

        Button infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InfoActivity.class);
            startActivity(intent);
        });
    }

    private void showRating() {
        Games.getLeaderboardsClient(this,
                Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .getLeaderboardIntent(getString(R.string.leaderboard))
                .addOnSuccessListener(intent -> startActivityForResult(intent, RC_LEADERBOARD_UI));
    }

    /** Signs in to GooglePlay */
    public void signIn() {
        GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            showRating();
        } else {
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                    GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                showRating();
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.login_fail);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        } else if (requestCode == RC_LEADERBOARD_UI) {
            Log.d(Controller.APP_TAG, "Showed leaderboard");
        }
    }
}