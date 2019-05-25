package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.TrainingController;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.utils.Question;

public class TrainingPlayerLogic {
    private int playerScore = 0;
    private int maxScore = 0;
    private Question currentQuestion;
    private static final int TIME_TO_WRITE_ANSWER = 20;
    private static final int TIME_TO_READ_QUESTION = 10;
    private static final int TIME_TO_SHOW_ANSWER = 5;
    private static final int SECOND = 1000;
    private CountDownTimer timer;

    public void newQuestion() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        currentQuestion = DatabaseController.getRandomQuestion();
        maxScore++;
        TrainingController.TrainingUIController.setQuestionText(currentQuestion.getQuestion());
        TrainingController.TrainingUIController.onNewQuestion();
        TrainingController.TrainingUIController.setTime("");
        TrainingController.TrainingUIController.setAnswer(currentQuestion.getAllAnswers());
        TrainingController.TrainingUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
        Log.d("BrainRing", "New question");
        timer = new CountDownTimer(TIME_TO_READ_QUESTION * SECOND, SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timer == this) {
                    if (millisUntilFinished <= TIME_TO_READ_QUESTION * SECOND) {
                        Log.d("BrainRing", "TICK");
                    }
                }
            }

            @Override
            public void onFinish() {
                startAnswer();
            }
        };
        timer.start();
    }

    private void startAnswer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        TrainingController.TrainingUIController.setLocation(GameActivityLocation.WRITE_ANSWER);

        timer = new CountDownTimer(TIME_TO_WRITE_ANSWER * SECOND, SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timer == this) {
                    if (millisUntilFinished <= TIME_TO_WRITE_ANSWER * SECOND) {
                        onReceivingTick(millisUntilFinished / SECOND);
                    }
                }
            }

            @Override
            public void onFinish() {
                answerIsWritten(TrainingController.TrainingUIController.getWhatWritten());
            }
        };
        timer.start();
    }

    public void answerIsWritten(String answer) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        TrainingController.TrainingUIController.setTime("");
        Log.d("BrainRing", "Checking answer " + answer);
        if (currentQuestion.checkAnswer(answer)) {
            playerScore++;
        }
        TrainingController.TrainingUIController.setScore(playerScore, maxScore);
        TrainingController.TrainingUIController.setLocation(GameActivityLocation.SHOW_ANSWER);
        timer = new CountDownTimer(TIME_TO_SHOW_ANSWER * SECOND, SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                newQuestion();
            }
        };
        timer.start();
    }

    private void onReceivingTick(long secondsLeft) {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(TrainingController.getTrainingGameActivity(), R.raw.countdown);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
    }

    public void answerButtonPushed() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        startAnswer();
    }

    public void finishGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
