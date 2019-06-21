package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.managers.LocalPlayerGameManager;
import ru.spbhse.brainring.utils.LocalGameRoles;

/** This activity is for maintaining player in local mode */
public class PlayerActivity extends AppCompatActivity {
    private final static int RC_SIGN_IN = 42;
    private LocalPlayerGameManager manager;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        LocalGameRoles colorName = (LocalGameRoles) getIntent().getSerializableExtra("color");
        manager = new LocalPlayerGameManager(this, colorName);

        Button button = findViewById(R.id.buttonPush);
        button.setOnClickListener(v -> manager.getLogic().answerButtonPushed());
        if (colorName == LocalGameRoles.ROLE_RED) {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCardinal));
        } else {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorJungleGreen));
        }

        signIn();
    }

    /* I know that this function is out of content here,
       but it is linked with onActivityResult that can be placed only here */
    /** Signs in to GooglePlay */
    public void signIn() {
        GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            manager.getNetwork().signedIn(account);
            manager.getNetwork().startQuickGame();
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
                manager.getNetwork().signedIn(result.getSignInAccount());
                manager.getNetwork().startQuickGame();
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

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        super.onStop();
        manager.finishGame();
    }

    /** When back button is pressed, asks if user is sure to leave the game and did not pressed it accidentally */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.out_of_local))
                .setMessage(getString(R.string.want_out))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    manager.finishGame();
                    finish();
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                })
                .show();
    }
}
