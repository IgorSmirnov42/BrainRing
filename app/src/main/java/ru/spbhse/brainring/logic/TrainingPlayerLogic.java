package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.controllers.TrainingController;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.utils.Question;

public class TrainingPlayerLogic {
    private static final int TIME_TO_WRITE_ANSWER = 20;
    private static final int SECOND = 1000;
    public static final int DEFAULT_READING_TIME = 20;
    private Question currentQuestion;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int readingTime = DEFAULT_READING_TIME;
    private CountDownTimer timer;

    /**
     * Returns current question data
     *
     * @return current question data as {@code ComplainedQuestions}
     */
    @NonNull
    public ComplainedQuestion getCurrentQuestionData() {
        return new ComplainedQuestion(currentQuestion.getQuestion(),
                currentQuestion.getAllAnswers(), currentQuestion.getId());
    }

    /**
     * Starts a new question. Namely, tries to get a new question from a database.
     * If no questions are available, finishes the game.
     */
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

    /** Reacts on pushing the answer button, cancels the timer and suggests user to answer the question */
    public void answerButtonPushed() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        startAnswer();
    }

    /** Finishes the game, sets the game table to null */
    public void finishGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        DatabaseController.setGameTable(null);
    }

    /** Sets the reading time */
    public void setReadingTime(int readingTime) {
        this.readingTime = readingTime;
    }

    /** Reacts on writing the answer */
    public void answerIsWritten(@NonNull String answer) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        TrainingController.TrainingUIController.setTime("");
        Log.d(Controller.APP_TAG, "Checking answer " + answer);
        if (currentQuestion.checkAnswer(answer)) {
            TrainingController.TrainingUIController.setQuestionResult(
                    TrainingController.getTrainingGameActivity().getString(R.string.right_answer));
            correctAnswers++;
        } else {
            TrainingController.TrainingUIController.setQuestionResult(
                    TrainingController.getTrainingGameActivity().getString(R.string.wrong_answer));
            wrongAnswers++;
        }
        TrainingController.TrainingUIController.setScore(correctAnswers, wrongAnswers);
        TrainingController.TrainingUIController.setLocation(GameActivityLocation.SHOW_ANSWER);
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

    private void onReceivingTick(long secondsLeft) {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(TrainingController.getTrainingGameActivity(),
                    R.raw.countdown);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
    }
}
