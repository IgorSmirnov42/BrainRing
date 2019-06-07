package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.util.Log;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.TrainingController;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.utils.Question;

public class TrainingPlayerLogic {
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private Question currentQuestion;
    private static final int TIME_TO_WRITE_ANSWER = 20;
    public static final int DEFAULT_READING_TIME = 10;
    private static final int SECOND = 1000;
    private CountDownTimer timer;
    private int readingTime = DEFAULT_READING_TIME;

    public ComplainedQuestion getCurrentQuestionData() {
        return new ComplainedQuestion(currentQuestion.getQuestion(),
                currentQuestion.getAllAnswers(), currentQuestion.getId());
    }

    public void newQuestion() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (DatabaseController.getNumberOfRemainingQuestions() == 0) {
            TrainingController.TrainingUIController.onGameFinished();
            return;
        }
        currentQuestion = DatabaseController.getRandomQuestion();
        TrainingController.TrainingUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
        TrainingController.TrainingUIController.setQuestionText(currentQuestion.getQuestion());
        TrainingController.TrainingUIController.setTime("");
        TrainingController.TrainingUIController.setAnswer(currentQuestion.getAllAnswers());
        TrainingController.TrainingUIController.setComment(currentQuestion.getComment());
        TrainingController.TrainingUIController.onNewQuestion();
        Log.d(Controller.APP_TAG, "New question");
        timer = new CountDownTimer(readingTime * SECOND, SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timer == this) {
                    if (millisUntilFinished <= readingTime * SECOND) {
                        long secondsLeft = millisUntilFinished / SECOND;
                        TrainingController.TrainingUIController.setTime(String.valueOf(secondsLeft));
                        Log.d(Controller.APP_TAG, "TICK" + secondsLeft);
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
                    if (millisUntilFinished <= TIME_TO_WRITE_ANSWER * SECOND / 2) {
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
        Log.d(Controller.APP_TAG, "Checking answer " + answer);
        if (currentQuestion.checkAnswer(answer)) {
            TrainingController.TrainingUIController.setQuestionResult("Правильный ответ!");
            correctAnswers++;
        } else {
            TrainingController.TrainingUIController.setQuestionResult("Неверный ответ");
            wrongAnswers++;
        }
        TrainingController.TrainingUIController.setScore(correctAnswers, wrongAnswers);
        TrainingController.TrainingUIController.setLocation(GameActivityLocation.SHOW_ANSWER);
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
        DatabaseController.setGameTable(null);
    }

    public void setReadingTime(int readingTime) {
        this.readingTime = readingTime;
    }
}
