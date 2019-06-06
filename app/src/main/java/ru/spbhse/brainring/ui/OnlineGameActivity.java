package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.GamesActivityResultCodes;

import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.database.QuestionDatabase;

public class OnlineGameActivity extends GameActivity {

    private static final int RC_SIGN_IN = 42;

    public QuestionDatabase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /////////////
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /////////////
        super.onCreate(savedInstanceState);

        OnlineController.setUI(OnlineGameActivity.this);
        dataBase = new QuestionDatabase(OnlineGameActivity.this);
        DatabaseController.setDatabase(dataBase);
        dataBase.openDataBase();
        dataBase.createTable(dataBase.getBaseTable());
        gameController = OnlineController.OnlineUserLogicController.getInstance();

        drawLocation();

        OnlineController.NetworkController.createOnlineGame();
    }

    /* I know that this function is out of content here,
       but it is linked with onActivityResult that can be placed only here */
    public void signIn() {
        GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            Log.d("BrainRing", "Already logged in");
            OnlineController.NetworkController.loggedIn(account);
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
                OnlineController.NetworkController.loggedIn(result.getSignInAccount());
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = "Ошибка входа в аккаунт Google Play Games";
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, (dialog, which) -> finish()).show();
            }
        } else if (requestCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
            Log.d("BrainRing", "Left room from activity");
            OnlineController.finishOnlineGame();
            OnlineController.NetworkController.finishImmediately("Игра прервана. Разорвано соединение с соперником.");
            finish();
        } else if (requestCode == GamesActivityResultCodes.RESULT_SEND_REQUEST_FAILED) {
            Log.d("BrainRing", "Send request failed");
        } else if (requestCode == GamesActivityResultCodes.RESULT_NETWORK_FAILURE) {
            Log.d("BrainRing", "Network failure");
        }
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("Выход из игры по сети")
                .setMessage("Вы хотите выйти?")
                .setPositiveButton("Да", (dialog, which) -> {
                    Intent intent = new Intent(OnlineGameActivity.this,
                            SelectOnlineOpponentActivity.class);
                    startActivity(intent);

                    finish();
                })
                .setNegativeButton("Нет", (dialog, which) -> {
                })
                .show();
    }

    public void showGameFinishedActivity(@NonNull String message) {
        Intent intent = new Intent(this, OnlineGameFinished.class);
        intent.putExtra("message", message);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        Log.d("BrainRing", "Stopping activity. Leaving room");
        super.onStop();
        OnlineController.finishOnlineGame();
    }
}
