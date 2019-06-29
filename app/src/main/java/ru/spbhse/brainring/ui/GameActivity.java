package ru.spbhse.brainring.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.files.ComplainsFileHandler;
import ru.spbhse.brainring.logic.PlayerLogic;
import ru.spbhse.brainring.utils.Constants;

import static ru.spbhse.brainring.ui.GameActivityLocation.GAME_WAITING_START;
import static ru.spbhse.brainring.ui.GameActivityLocation.OPPONENT_IS_ANSWERING;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_ANSWER;
import static ru.spbhse.brainring.ui.GameActivityLocation.SHOW_QUESTION;
import static ru.spbhse.brainring.ui.GameActivityLocation.WRITE_ANSWER;

/**
 * This activity is used as main game activity.
 * Contains all basic attributes, similar in online and training game
 */
abstract public class GameActivity extends AppCompatActivity {
    protected GameActivityLocation currentLocation = GAME_WAITING_START;
    protected String question = "";
    protected String answer = "";
    protected String comment = "";
    protected String myScore = "0";
    protected String opponentScore = "0";
    protected String myNick = "";
    protected String opponentAnswer = "";
    protected String opponentNick = "";
    protected String timeLeft = "";
    protected String buttonText = "";
    protected String questionResult = "";
    protected PlayerLogic playerLogic;

    /** Main function. Based on current location, draws all needed components for this location. */
    protected void drawLocation() {
        if (currentLocation == GAME_WAITING_START) {
            setContentView(R.layout.activity_waiting_for_start);
            TextView waitingForOpponent = findViewById(R.id.waitingStartInfo);
            waitingForOpponent.setVisibility(View.VISIBLE);
        }
        if (currentLocation == SHOW_QUESTION) {
            setContentView(R.layout.activity_showing_question);

            setAnswerButtonText(buttonText);

            setQuestionText(question);
            makeScrollable(findViewById(R.id.questionText));

            setOpponentAnswer(opponentAnswer);

            setTime(timeLeft);

            Button answerButton = findViewById(R.id.answerReadyButton);
            answerButton.setOnClickListener(v -> playerLogic.answerButtonPushed());
        }
        if (currentLocation == WRITE_ANSWER) {
            setContentView(R.layout.activity_writing_answer);

            setQuestionText(question);
            makeScrollable(findViewById(R.id.questionText));

            EditText answerEditor = findViewById(R.id.answerEditor);
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(answerEditor, InputMethodManager.SHOW_IMPLICIT);

            Button answerWrittenButton = findViewById(R.id.answerWrittenButton);
            answerWrittenButton.setOnClickListener(
                    v -> playerLogic.answerIsWritten(answerEditor.getText().toString()));
        }
        if (currentLocation == SHOW_ANSWER) {
            setContentView(R.layout.activity_showing_answer);

            setAnswerText(answer);
            makeScrollable(findViewById(R.id.rightAnswerTextField));

            setCommentText(comment);
            makeScrollable(findViewById(R.id.commentField));

            setScore(myScore, opponentScore);
            setQuestionResult(questionResult);
            setMyNick(myNick);
            setOpponentNick(opponentNick);

            Button complainButton = findViewById(R.id.complainButton);
            complainButton.setOnClickListener(v -> {
                ComplainedQuestion question = playerLogic.getCurrentQuestionData();
                try {
                    ComplainsFileHandler.appendComplain(question, this);
                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                    return;
                }

                Toast toast = Toast.makeText(GameActivity.this,
                        getString(R.string.added_complain),
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

    /** Reacts on new question */
    public void onNewQuestion() {
        setOpponentAnswer("");
        setTime("");
        setAnswerButtonText(getString(R.string.reading_question_btn));
    }

    /** Sets opponent nickname */
    public void setOpponentNick(@NonNull String nick) {
        opponentNick = nick;
        TextView opponentNickView = findViewById(R.id.opponentNick);
        if (opponentNickView != null) {
            opponentNickView.setText(opponentNick);
        }
    }

    /** Sets user nickname */
    public void setMyNick(@NonNull String nick) {
        myNick = nick;
        TextView myNickView = findViewById(R.id.myNick);
        if (myNickView != null) {
            myNickView.setText(myNick);
        }
    }

    /** Sets questions result */
    public void setQuestionResult(@NonNull String result) {
        questionResult = result;
        TextView questionResultView = findViewById(R.id.questionResult);
        if (questionResultView != null) {
            questionResultView.setText(questionResult);
        }
    }

    /** Sets text on answer button */
    public void setAnswerButtonText(@NonNull String text) {
        buttonText = text;
        Button button = findViewById(R.id.answerReadyButton);
        if (button != null) {
            button.setText(buttonText);
        }
    }

    /** Sets time */
    public void setTime(@NonNull String time) {
        timeLeft = time;
        TextView timeLeftView = findViewById(R.id.timeLeft);
        if (timeLeftView != null) {
            timeLeftView.setText(timeLeft);
        }
    }

    /** Sets opponent answer */
    public void setOpponentAnswer(@NonNull String answer) {
        opponentAnswer = answer;
        TextView opponentAnswer = findViewById(R.id.opponentAnswer);
        if (opponentAnswer != null) {
            opponentAnswer.setText(answer);
        }
    }

    /** Sets score */
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

    /** Sets question text */
    public void setQuestionText(@NonNull String question) {
        this.question = question;
        TextView questionTextField = findViewById(R.id.questionText);
        if (questionTextField != null) {
            questionTextField.setText(question);
        }
    }

    /** Sets question text */
    public void setAnswerText(@NonNull String answer) {
        this.answer = answer;
        TextView rightAnswerTextField = findViewById(R.id.rightAnswerTextField);
        if (rightAnswerTextField != null) {
            String answerToShow = getString(R.string.answer_show) + " " + answer;
            rightAnswerTextField.setText(answerToShow);
        }
    }

    /** Sets comment text */
    public void setCommentText(@NonNull String comment) {
        this.comment = comment;
        TextView commentField = findViewById(R.id.commentField);
        if (commentField != null) {
            String commentToShow = comment;
            if (!comment.equals("")) {
                commentToShow = getString(R.string.comment) + " " + commentToShow;
            }
            commentField.setText(commentToShow);
        }
    }

    /** Sets current location */
    public void setLocation(@NonNull GameActivityLocation location) {
        currentLocation = location;
        drawLocation();
    }

    /** Gets what is written in edit answer field */
    public String getWhatWritten() {
        EditText answerEditor = findViewById(R.id.answerEditor);
        if (answerEditor != null) {
            return answerEditor.getText().toString();
        } else {
            Log.wtf(Constants.APP_TAG, "Answer editing wasn't open but should");
            return "";
        }
    }
}
