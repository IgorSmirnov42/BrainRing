package ru.spbhse.brainring.ui;

import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.OnlineController;

import static ru.spbhse.brainring.ui.GameActivityLocation.GAME_WAITING_START;
import static ru.spbhse.brainring.ui.GameActivityLocation.OPPONENT_IS_ANSWERING;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_ANSWER;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_QUESTION;
import static ru.spbhse.brainring.ui.GameActivityLocation.WRITE_ANSWER;

abstract public class GameActivity extends AppCompatActivity {
    protected GameActivityLocation currentLocation = GAME_WAITING_START;

    protected String question;
    protected String buttonText;
    protected String timeLeft;
    protected String opponentAnswer;
    protected String answer;
    protected String myScore;
    protected String opponentScore;

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

            answerButton.setOnClickListener(v -> OnlineController.OnlineUserLogicController.answerButtonPushed());
        }
        if (currentLocation == WRITE_ANSWER) {
            setContentView(R.layout.activity_writing_answer);

            TextView questionTextField = findViewById(R.id.questionText);
            questionTextField.setText(question);

            EditText answerEditor = findViewById(R.id.answerEditor);
            Button answerWrittenButton = findViewById(R.id.answerWrittenButton);
            answerWrittenButton.setOnClickListener(
                    v -> OnlineController.OnlineUserLogicController.answerIsWritten(
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
}
