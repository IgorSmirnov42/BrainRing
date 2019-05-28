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

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 42;
    private static final int RC_LEADERBOARD_UI = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button trainBtn = findViewById(R.id.trainButton);
        trainBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrainingGamePreparationActivity.class);
            startActivity(intent);
        });

        Button netBtn = findViewById(R.id.netButton);
        netBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectOnlineOpponentActivity.class);
            startActivity(intent);
        });

        Button localBtn = findViewById(R.id.localButton);
        localBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectLocalGameModeActivity.class);
            startActivity(intent);
        });

        Button complainBtn = findViewById(R.id.complainButton);
        complainBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ComplainActivity.class);
            startActivity(intent);
        });

        Button ratingBtn = findViewById(R.id.ratingButton);
        ratingBtn.setOnClickListener(v -> signIn());

        Button infoBtn = findViewById(R.id.infoButton);
        infoBtn.setOnClickListener(v -> {

        });
    }

    private void showRating() {
        Games.getLeaderboardsClient(this,
                Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .getLeaderboardIntent(getString(R.string.leaderboard))
                .addOnSuccessListener(intent -> startActivityForResult(intent, RC_LEADERBOARD_UI));
    }

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
                    message = "Неизвестная ошибка. Убедитесь, что у Вас установлены Google Play игры и выполнен вход в аккаунт.";
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        } else if (requestCode == RC_LEADERBOARD_UI) {
            Log.d("BrainRing", "Showed leaderboard");
        }
    }
}