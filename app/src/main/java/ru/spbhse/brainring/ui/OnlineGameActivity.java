package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.GamesActivityResultCodes;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.database.QuestionDataBase;

public class OnlineGameActivity extends GameActivity {

    private static final int RC_SIGN_IN = 42;

    public QuestionDataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /////////////
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /////////////
        super.onCreate(savedInstanceState);

        OnlineController.setUI(OnlineGameActivity.this);
        dataBase = new QuestionDataBase(OnlineGameActivity.this);
        dataBase.openDataBase();

        drawLocation();

        OnlineController.NetworkController.createOnlineGame();
    }

    public String getWhatWritten() {
        EditText answerEditor = findViewById(R.id.answerEditor);
        if (answerEditor != null) {
            return answerEditor.getText().toString();
        } else {
            Log.wtf("BrainRing", "Answer editing wasn't open but should");
            return "";
        }
    }

    /* I know that this function is out of content here,
       but it is linked with onActivityResult that can be placed only here */
    public void signIn() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
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
            OnlineController.NetworkController.leaveRoom();
        } else if (requestCode == GamesActivityResultCodes.RESULT_SEND_REQUEST_FAILED) {
            Log.d("BrainRing", "Send request failed");
        } else if (requestCode == GamesActivityResultCodes.RESULT_NETWORK_FAILURE) {
            Log.d("BrainRing", "Network failure");
        }
    }

    @Override
    protected void onStop() {
        Log.d("BrainRing", "Stopping activity. Leaving room");
        super.onStop();
        OnlineController.NetworkController.leaveRoom();
    }
}
