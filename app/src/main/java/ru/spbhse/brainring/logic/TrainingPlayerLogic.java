package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.ui.TrainingGameActivity;
import ru.spbhse.brainring.utils.Question;

public class TrainingPlayerLogic implements PlayerLogic {
    private static final int TIME_TO_WRITE_ANSWER = 20;
    private static final int SECOND = 1000;
    public static final int DEFAULT_READING_TIME = 20;
    private Question currentQuestion;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int readingTime = DEFAULT_READING_TIME;
    private CountDownTimer timer;
    private TrainingGameActivity activity;

    public TrainingPlayerLogic(TrainingGameActivity activity) {
        this.activity = activity;
    }

    /**
     * Returns current question data
     *
     * @return current question data as {@code ComplainedQuestions}
     */
    @NonNull
    @Override
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
            activity.onGameFinished();
            return;
        }
        currentQuestion = DatabaseController.getRandomQuestion();

        activity.setLocation(GameActivityLocation.SHOW_QUESTION);
        activity.setQuestionText(currentQuestion.getQuestion());
        activity.setTime("");
        activity.setAnswerText(currentQuestion.getAllAnswers());
        activity.setCommentText(currentQuestion.getComment());
        activity.onNewQuestion();

        Log.d(Controller.APP_TAG, "New question");

        timer = new CountDownTimer(readingTime * SECOND, SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timer == this) {
                    if (millisUntilFinished <= readingTime * SECOND) {
                        long secondsLeft = millisUntilFinished / SECOND;
                        activity.setTime(String.valueOf(secondsLeft));
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

    /**
     * Reacts on pushing the answer button, cancels the timer and suggests user to answer
     * the question
     */
    @Override
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
    @Override
    public void answerIsWritten(@NonNull String answer) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        activity.setTime("");
        Log.d(Controller.APP_TAG, "Checking answer " + answer);
        if (currentQuestion.checkAnswer(answer)) {
            activity.setQuestionResult(activity.getString(R.string.right_answer));
            correctAnswers++;
        } else {
            activity.setQuestionResult(activity.getString(R.string.wrong_answer));
            wrongAnswers++;
        }
        activity.setScore(String.valueOf(correctAnswers), String.valueOf(wrongAnswers));
        activity.setLocation(GameActivityLocation.SHOW_ANSWER);
    }

    private void startAnswer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        activity.setLocation(GameActivityLocation.WRITE_ANSWER);

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
                answerIsWritten(activity.getWhatWritten());
            }
        };
        timer.start();
    }

    private void onReceivingTick(long secondsLeft) {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(activity, R.raw.countdown);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
    }
}
