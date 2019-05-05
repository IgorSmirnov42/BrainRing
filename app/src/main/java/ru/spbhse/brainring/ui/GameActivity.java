package ru.spbhse.brainring.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import ru.spbhse.brainring.database.QuestionDataBase;

import static ru.spbhse.brainring.ui.GameActivityLocation.GAME_WAITING_START;
import static ru.spbhse.brainring.ui.GameActivityLocation.OPPONENT_IS_ANSWERING;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_ANSWER;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_QUESTION;
import static ru.spbhse.brainring.ui.GameActivityLocation.WRITE_ANSWER;

public class GameActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 42;

    private GameActivityLocation currentLocation = GAME_WAITING_START;

    public QuestionDataBase dataBase;

    private String question;
    private String buttonText;
    private String timeLeft;
    private String opponentAnswer;
    private String answer;
    private String myScore;
    private String opponentScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /////////////
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /////////////
        super.onCreate(savedInstanceState);

        Controller.setUI(GameActivity.this);
        dataBase = new QuestionDataBase(GameActivity.this);
        dataBase.openDataBase();

        drawLocation();

        Controller.NetworkController.createOnlineGame();
    }

    private void drawLocation() {
        if (currentLocation == GAME_WAITING_START) {
            setContentView(R.layout.activity_waiting_for_start);
        }
        if (currentLocation == SHOW_QUESTION) {
            setContentView(R.layout.activity_showing_question);

            Button answerButton = findViewById(R.id.answerReadyButton);
            answerButton.setText(buttonText);

            TextView questionTextField = findViewById(R.id.questionText);
            questionTextField.setText(question);

            TextView opponentAnswer = findViewById(R.id.opponentAnswer);
            opponentAnswer.setText(this.opponentAnswer);

            TextView timeLeft = findViewById(R.id.timeLeft);
            timeLeft.setText(this.timeLeft);

            answerButton.setOnClickListener(v -> Controller.OnlineUserLogicController.answerButtonPushed());
        }
        if (currentLocation == WRITE_ANSWER) {
            setContentView(R.layout.activity_writing_answer);

            TextView questionTextField = findViewById(R.id.questionText);
            questionTextField.setText(question);

            EditText answerEditor = findViewById(R.id.answerEditor);
            Button answerWrittenButton = findViewById(R.id.answerWrittenButton);
            answerWrittenButton.setOnClickListener(v -> {
                Controller.OnlineUserLogicController.answerIsWritten(answerEditor.getText().toString());
                //hideKeyboard();
            });
        }
        if (currentLocation == SHOW_ANSWER) {
            setContentView(R.layout.activity_showing_answer);

            TextView rightAnswerTextField = findViewById(R.id.rightAnswerTextField);
            rightAnswerTextField.setText(answer);

            TextView myScore = findViewById(R.id.myScore);
            myScore.setText(this.myScore);

            TextView opponentScore = findViewById(R.id.opponentScore);
            opponentScore.setText(this.opponentScore);
        }
        if (currentLocation == OPPONENT_IS_ANSWERING) {
            setContentView(R.layout.activity_opponent_answering);

            TextView questionTextField = findViewById(R.id.questionText);
            questionTextField.setText(question);
        }
    }

    public void onNewQuestion() {
        setOpponentAnswer("");
        setTime("");
        buttonText = "Чтение вопроса";
    }

    public void setButtonText(String text) {
        buttonText = text;
        drawLocation();
    }

    public void setTime(String time) {
        timeLeft = time;
        drawLocation();
    }

    public void setOpponentAnswer(String answer) {
        opponentAnswer = answer;
        drawLocation();
    }

    public void setScore(int my, int opponent) {
        myScore = String.valueOf(my);
        opponentScore = String.valueOf(opponent);
        drawLocation();
    }

    public void setQuestionText(String question) {
        this.question = question;
        drawLocation();
    }

    public void setAnswer(String answer) {
        this.answer = answer;
        drawLocation();
    }

    public void setLocation(GameActivityLocation location) {
        currentLocation = location;
        drawLocation();
    }

    public void hideKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
                Controller.NetworkController.loggedIn(result.getSignInAccount());
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = "Ошибка входа в аккаунт Google Play Games";
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, (dialog, which) -> finish()).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("BrainRing", "Destroying activity. Leaving room");
        super.onDestroy();
        Controller.NetworkController.leaveRoom();
    }
}
