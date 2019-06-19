package ru.spbhse.brainring.logic;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.DatabaseController;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.logic.timers.TrainingGameTimer;
import ru.spbhse.brainring.logic.timers.TrainingWritingTimer;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.ui.TrainingGameActivity;
import ru.spbhse.brainring.utils.Constants;
import ru.spbhse.brainring.utils.Question;
import ru.spbhse.brainring.utils.SoundPlayer;

public class TrainingPlayerLogic implements PlayerLogic {
    private static final int TIME_TO_WRITE_ANSWER = 20;
    public static final int DEFAULT_READING_TIME = 20;
    private SoundPlayer player = new SoundPlayer();
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

        Log.d(Constants.APP_TAG, "New question");

        timer = new TrainingGameTimer(readingTime, this);
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
        player.finish();
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
        Log.d(Constants.APP_TAG, "Checking answer " + answer);
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

    public void startAnswer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        activity.setLocation(GameActivityLocation.WRITE_ANSWER);

        timer = new TrainingWritingTimer(TIME_TO_WRITE_ANSWER, this);
        timer.start();
    }

    public void onReceivingTick(long secondsLeft) {
        player.play(activity, R.raw.countdown);
    }

    public CountDownTimer getTimer() {
        return timer;
    }

    public TrainingGameActivity getActivity() {
        return activity;
    }
}
