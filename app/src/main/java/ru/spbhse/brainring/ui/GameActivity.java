package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ru.spbhse.brainring.R;

public class GameActivity extends AppCompatActivity {

    enum Location {
        GAME_WAITING_START,
        SHOW_QUESTION,
        WRITE_ANSWER,
        SHOW_ANSWER
    }

    private Location currentLocation = Location.GAME_WAITING_START;
    private View questionTextField;
    private View answerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        questionTextField = findViewById(R.id.questionText);
        answerButton = findViewById(R.id.answerReadyButton);
        drawLocation();
    }

    public void gameCreated() {
        if (currentLocation != Location.GAME_WAITING_START) {
            return;
        }

        currentLocation = Location.SHOW_QUESTION;
        drawLocation();
    }

    private void drawLocation() {
        if (currentLocation == Location.GAME_WAITING_START) {
            questionTextField.setVisibility(View.VISIBLE);
            answerButton.setVisibility(View.GONE);
        }
        if (currentLocation == Location.SHOW_QUESTION) {
            questionTextField.setVisibility(View.VISIBLE);
            answerButton.setVisibility(View.VISIBLE);
        }
    }

}
