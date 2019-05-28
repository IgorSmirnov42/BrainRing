package ru.spbhse.brainring.ui;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.GameController;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.files.ComplainsFileHandler;

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
    protected GameController gameController;

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

            answerButton.setOnClickListener(v -> gameController.answerButtonPushed());
        }
        if (currentLocation == WRITE_ANSWER) {
            setContentView(R.layout.activity_writing_answer);

            TextView questionTextField = findViewById(R.id.questionText);
            questionTextField.setText(question);

            EditText answerEditor = findViewById(R.id.answerEditor);
            Button answerWrittenButton = findViewById(R.id.answerWrittenButton);
            answerWrittenButton.setOnClickListener(
                    v -> gameController.answerIsWritten(
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

            Button complainButton = findViewById(R.id.complainButton);
            complainButton.setOnClickListener(v -> {
                ComplainedQuestion question = gameController.getCurrentQuestionData();
                try {
                    ComplainsFileHandler.appendComplain(question);
                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                    return;
                }
                Toast toast = Toast.makeText(GameActivity.this,
                        "Вопрос добавлен в список. После игры зайдите во вкладку " +
                                "\"Пожаловаться на вопрос\", чтобы отправить жалобу",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            });
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

    public String getWhatWritten() {
        EditText answerEditor = findViewById(R.id.answerEditor);
        if (answerEditor != null) {
            return answerEditor.getText().toString();
        } else {
            Log.wtf("BrainRing", "Answer editing wasn't open but should");
            return "";
        }
    }
}
