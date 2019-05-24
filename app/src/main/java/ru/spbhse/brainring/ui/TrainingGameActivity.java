package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.TrainingController;
import ru.spbhse.brainring.database.QuestionDataBase;

import static ru.spbhse.brainring.ui.GameActivityLocation.GAME_WAITING_START;
import static ru.spbhse.brainring.ui.GameActivityLocation.OPPONENT_IS_ANSWERING;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_ANSWER;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_QUESTION;
import static ru.spbhse.brainring.ui.GameActivityLocation.WRITE_ANSWER;

public class TrainingGameActivity extends GameActivity {
    public QuestionDataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /////////////
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /////////////
        super.onCreate(savedInstanceState);

        currentLocation = GameActivityLocation.GAME_WAITING_START;

        TrainingController.setUI(TrainingGameActivity.this);
        dataBase = new QuestionDataBase(TrainingGameActivity.this);
        dataBase.openDataBase();

        TrainingController.createTrainingGame();
    }

    protected void drawLocation() {
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

            answerButton.setOnClickListener(v -> TrainingController.TrainingLogicController.answerButtonPushed());
        }
        if (currentLocation == WRITE_ANSWER) {
            setContentView(R.layout.activity_writing_answer);

            TextView questionTextField = findViewById(R.id.questionText);
            questionTextField.setText(question);

            EditText answerEditor = findViewById(R.id.answerEditor);
            Button answerWrittenButton = findViewById(R.id.answerWrittenButton);
            answerWrittenButton.setOnClickListener(
                    v -> TrainingController.TrainingLogicController.answerIsWritten(
                            answerEditor.getText().toString()));
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

    @Override
    public void onNewQuestion() {
        setOpponentAnswer("");
        setTime("");
        buttonText = "ЖМЯК!!!";
    }

    @Override
    protected void onStop() {
        super.onStop();
        TrainingController.TrainingLogicController.finishGame();
    }
}
