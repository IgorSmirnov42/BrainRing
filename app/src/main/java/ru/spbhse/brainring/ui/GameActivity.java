package ru.spbhse.brainring.ui;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
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

    protected String question = "";
    protected String buttonText = "";
    protected String timeLeft = "";
    protected String opponentAnswer = "";
    protected String answer = "";
    protected String comment = "";
    protected String myScore = "0";
    protected String opponentScore = "0";
    protected GameController gameController;
    protected String opponentNick = "";
    protected String myNick = "";
    protected String questionResult = "";

    protected void drawLocation() {
        if (currentLocation == GAME_WAITING_START) {
            setContentView(R.layout.activity_waiting_for_start);
        }
        if (currentLocation == SHOW_QUESTION) {
            setContentView(R.layout.activity_showing_question);

            setButtonText(buttonText);

            setQuestionText(question);
            makeScrollable(findViewById(R.id.questionText));

            setOpponentAnswer(opponentAnswer);

            setTime(timeLeft);

            Button answerButton = findViewById(R.id.answerReadyButton);
            answerButton.setOnClickListener(v -> gameController.answerButtonPushed());
        }
        if (currentLocation == WRITE_ANSWER) {
            setContentView(R.layout.activity_writing_answer);

            setQuestionText(question);
            makeScrollable(findViewById(R.id.questionText));

            EditText answerEditor = findViewById(R.id.answerEditor);
            Button answerWrittenButton = findViewById(R.id.answerWrittenButton);
            answerWrittenButton.setOnClickListener(
                    v -> gameController.answerIsWritten(
                            answerEditor.getText().toString()));
        }
        if (currentLocation == SHOW_ANSWER) {
            setContentView(R.layout.activity_showing_answer);

            setAnswer(answer);
            makeScrollable(findViewById(R.id.rightAnswerTextField));

            setComment(comment);
            makeScrollable(findViewById(R.id.commentField));

            setScore(myScore, opponentScore);
            setQuestionResult(questionResult);
            setMyNick(myNick);
            setOpponentNick(opponentNick);

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

            setQuestionText(question);
            makeScrollable(findViewById(R.id.questionText));
        }
    }

    private void makeScrollable(@NonNull TextView view) {
        view.setMovementMethod(new ScrollingMovementMethod());
    }

    public void onNewQuestion() {
        setOpponentAnswer("");
        setTime("");
        setButtonText("Чтение вопроса");
    }

    public void setOpponentNick(@NonNull String nick) {
        opponentNick = nick;
        TextView opponentNickView = findViewById(R.id.opponentNick);
        if (opponentNickView != null) {
            opponentNickView.setText(opponentNick);
        }
    }

    public void setMyNick(@NonNull String nick) {
        myNick = nick;
        TextView myNickView = findViewById(R.id.myNick);
        if (myNickView != null) {
            myNickView.setText(myNick);
        }
    }

    public void setQuestionResult(@NonNull String result) {
        questionResult = result;
        TextView questionResultView = findViewById(R.id.questionResult);
        if (questionResultView != null) {
            questionResultView.setText(questionResult);
        }
    }

    public void setButtonText(@NonNull String text) {
        buttonText = text;
        Button button = findViewById(R.id.answerReadyButton);
        if (button != null) {
            button.setText(buttonText);
        }
    }

    public void setTime(@NonNull String time) {
        timeLeft = time;
        TextView timeLeftView = findViewById(R.id.timeLeft);
        if (timeLeftView != null) {
            timeLeftView.setText(timeLeft);
        }
    }

    public void setOpponentAnswer(@NonNull String answer) {
        opponentAnswer = answer;
        TextView opponentAnswer = findViewById(R.id.opponentAnswer);
        if (opponentAnswer != null) {
            opponentAnswer.setText(answer);
        }
    }

    public void setScore(@NonNull String my, @NonNull String opponent) {
        myScore = my;
        opponentScore = opponent;
        TextView myScore = findViewById(R.id.myScore);
        TextView opponentScore = findViewById(R.id.opponentScore);
        if (myScore != null && opponentScore != null) {
            opponentScore.setText(this.opponentScore);
            myScore.setText(this.myScore);
        }
    }

    public void setQuestionText(@NonNull String question) {
        this.question = question;
        TextView questionTextField = findViewById(R.id.questionText);
        if (questionTextField != null) {
            questionTextField.setText(question);
        }
    }

    public void setAnswer(@NonNull String answer) {
        this.answer = answer;
        TextView rightAnswerTextField = findViewById(R.id.rightAnswerTextField);
        if (rightAnswerTextField != null) {
            String answerToShow = "Ответ: " + answer;
            rightAnswerTextField.setText(answerToShow);
        }
    }

    public void setComment(@NonNull String comment) {
        this.comment = comment;
        TextView commentField = findViewById(R.id.commentField);
        if (commentField != null) {
            String commentToShow = comment;
            if (!comment.equals("") && !comment.startsWith("Комментарий: ")) {
                commentToShow = "Комментарий: " + commentToShow;
            }
            commentField.setText(commentToShow);
        }
    }

    public void setLocation(@NonNull GameActivityLocation location) {
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
