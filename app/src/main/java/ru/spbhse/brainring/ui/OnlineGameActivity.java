package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.GamesActivityResultCodes;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.database.QuestionDatabase;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.managers.OnlineGameManager;

/** This activity maintains online game */
public class OnlineGameActivity extends GameActivity {
    private static final int RC_SIGN_IN = 42;
    private OnlineGameManager manager;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = new OnlineGameManager(this);

        QuestionDatabase dataBase = QuestionDatabase.getInstance(this);
        DatabaseController.setDatabase(dataBase);

        drawLocation();

        DatabaseController.generateNewSequence();
        signIn();
    }

    /* I know that this function is out of content here,
       but it is linked with onActivityResult that can be placed only here */
    /** Signs in to GooglePlay */
    public void signIn() {
        GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            Log.d(Controller.APP_TAG, "Already logged in");
            manager.getNetwork().onSignedIn(account);
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
            if (result.isSuccess() && result.getSignInAccount() != null) {
                manager.getNetwork().onSignedIn(result.getSignInAccount());
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.login_fail);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, (dialog, which) -> finish()).show();
            }
        } else if (requestCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
            Log.d(Controller.APP_TAG, "Left room from activity");
            manager.getNetwork().finishImmediately(getString(R.string.default_error));
        } else if (requestCode == GamesActivityResultCodes.RESULT_SEND_REQUEST_FAILED) {
            Log.d(Controller.APP_TAG, "Send request failed");
        } else if (requestCode == GamesActivityResultCodes.RESULT_NETWORK_FAILURE) {
            Log.d(Controller.APP_TAG, "Network failure");
        }
    }

    /** When back button is pressed, asks if user is sure to leave the game and did not pressed it accidentally */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.out_of_online))
                .setMessage(getString(R.string.want_out))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> finish())
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                })
                .show();
    }

    /** Called after game is finished, starts AfterGameActivity*/
    public void showGameFinishedActivity(@NonNull String message) {
        Intent intent = new Intent(this, AfterGameActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(intent);
        finish();
    }

    /** {@inheritDoc} */
    @Override
    protected void drawLocation() {
        super.drawLocation();
        if (currentLocation == GameActivityLocation.SHOW_ANSWER) {
            Button continueGameButton = findViewById(R.id.continueGameButton);
            continueGameButton.setOnClickListener(v ->
                    manager.getUserLogic().readyForQuestion());
        }
    }

    @Override
    protected void handleWrittenAnswer(String writtenAnswer) {
        manager.getUserLogic().answerIsWritten(writtenAnswer);
    }

    @Override
    protected ComplainedQuestion getCurrentQuestionData() {
        return manager.getUserLogic().getQuestionData();
    }

    @Override
    protected void handleAnswerButtonPushed() {
        manager.getUserLogic().answerButtonPushed();
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        Log.d(Controller.APP_TAG, "Stopping activity. Leaving room");
        super.onStop();
        manager.finishOnlineGame();
    }
}
