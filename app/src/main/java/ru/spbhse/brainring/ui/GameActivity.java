package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.R;

import static ru.spbhse.brainring.ui.GameActivityLocation.GAME_WAITING_START;
import static ru.spbhse.brainring.ui.GameActivityLocation.OPPONENT_IS_ANSWERING;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_ANSWER;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_QUESTION;
import static ru.spbhse.brainring.ui.GameActivityLocation.WRITE_ANSWER;

public class GameActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 42;

    private GameActivityLocation currentLocation = GAME_WAITING_START;
    private TextView questionTextField;
    private Button answerButton;
    private Button answerWrittenButton;
    private TextView rightAnswerTextField;
    private EditText answerEditor;
    private TextView opponentIsAnswering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Controller.setUI(GameActivity.this);

        questionTextField = findViewById(R.id.questionText);
        answerButton = findViewById(R.id.answerReadyButton);
        answerWrittenButton = findViewById(R.id.answerWrittenButton);
        rightAnswerTextField = findViewById(R.id.rightAnswerTextField);
        answerEditor = findViewById(R.id.answerEditor);
        opponentIsAnswering = findViewById(R.id.opponentIsAnswering);

        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller.UserLogicController.answerButtonPushed();
            }
        });

        answerWrittenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller.UserLogicController.answerIsWritten(answerEditor.getText().toString());
                hideKeyboard();
            }
        });

        drawLocation();

        Controller.NetworkController.createOnlineGame();
    }

    private void drawLocation() {
        if (currentLocation == GAME_WAITING_START) {
            questionTextField.setVisibility(View.VISIBLE);
            answerButton.setVisibility(View.GONE);
            answerWrittenButton.setVisibility(View.GONE);
            rightAnswerTextField.setVisibility(View.GONE);
            answerEditor.setVisibility(View.GONE);
            opponentIsAnswering.setVisibility(View.GONE);
        }
        if (currentLocation == SHOW_QUESTION) {
            questionTextField.setVisibility(View.VISIBLE);
            answerButton.setVisibility(View.VISIBLE);
            answerWrittenButton.setVisibility(View.GONE);
            rightAnswerTextField.setVisibility(View.GONE);
            answerEditor.setVisibility(View.GONE);
            opponentIsAnswering.setVisibility(View.GONE);
        }
        if (currentLocation == WRITE_ANSWER) {
            questionTextField.setVisibility(View.GONE);
            answerButton.setVisibility(View.GONE);
            answerWrittenButton.setVisibility(View.VISIBLE);
            rightAnswerTextField.setVisibility(View.GONE);
            answerEditor.setVisibility(View.VISIBLE);
            opponentIsAnswering.setVisibility(View.GONE);
        }
        if (currentLocation == SHOW_ANSWER) {
            questionTextField.setVisibility(View.GONE);
            answerButton.setVisibility(View.GONE);
            answerWrittenButton.setVisibility(View.GONE);
            rightAnswerTextField.setVisibility(View.VISIBLE);
            answerEditor.setVisibility(View.GONE);
            opponentIsAnswering.setVisibility(View.GONE);
        }
        if (currentLocation == OPPONENT_IS_ANSWERING) {
            questionTextField.setVisibility(View.VISIBLE);
            answerButton.setVisibility(View.GONE);
            answerWrittenButton.setVisibility(View.GONE);
            rightAnswerTextField.setVisibility(View.GONE);
            answerEditor.setVisibility(View.GONE);
            opponentIsAnswering.setVisibility(View.VISIBLE);
        }
    }

    public void setQuestionText(String question) {
        questionTextField.setText(question);
    }

    public void setAnswer(String answer) {
        rightAnswerTextField.setText(answer);
    }

    public void setLocation(GameActivityLocation location) {
        currentLocation = location;
        drawLocation();
    }

    public void signIn() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    public void hideKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void clearEditText() {
        answerEditor.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Controller.NetworkController.loggedIn(result.getSignInAccount());
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = "Неизвестная ошибка. Убедитесь, что у Вас установлены Google Play игры и выполнен вход в аккаунт.";
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }
}
