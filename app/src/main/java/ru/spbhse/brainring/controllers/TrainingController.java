package ru.spbhse.brainring.controllers;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.logic.TrainingPlayerLogic;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.ui.TrainingGameActivity;

/** Controller for training game */
public class TrainingController extends Controller {
    private static WeakReference<TrainingGameActivity> trainingGameActivity;

    /** Sets new {@code TrainingGameActivity} */
    public static void setUI(TrainingGameActivity trainingGameActivity) {
        TrainingController.trainingGameActivity = new WeakReference<>(trainingGameActivity);
    }

    /** Returns stored {@code TrainingGameActivity} */
    public static TrainingGameActivity getTrainingGameActivity() {
        return trainingGameActivity.get();
    }

    /** Creates training game,
     *  namely creates new random questions sequence and creates training player logic */
    public static void createTrainingGame() {
        DatabaseController.generateNewSequence();
        TrainingLogicController.trainingPlayerLogic = new TrainingPlayerLogic();
    }

    /** Controls logic during the game **/
    public static class TrainingLogicController implements GameController {
        private static TrainingPlayerLogic trainingPlayerLogic;
        private static GameController gameController;

        /** Returns an instance of game controller (singleton) */
        @NonNull
        public static GameController getInstance() {
            if (gameController == null) {
                gameController = new TrainingLogicController();
            }
            return gameController;
        }

        /** Sets time for reading questions during this game */
        public static void setReadingTime(int readingTime) {
            trainingPlayerLogic.setReadingTime(readingTime);
        }

        /** Asks player a new question */
        public static void newQuestion() {
            trainingPlayerLogic.newQuestion();
        }

        /** {@inheritDoc} */
        @Override
        public void answerButtonPushed() {
            trainingPlayerLogic.answerButtonPushed();
        }

        /** {@inheritDoc} */
        @Override
        public void answerIsWritten(@NonNull String answer) {
            trainingPlayerLogic.answerIsWritten(answer);
        }

        /** {@inheritDoc} */
        @Override
        @NonNull
        public ComplainedQuestion getCurrentQuestionData() {
            return trainingPlayerLogic.getCurrentQuestionData();
        }

        /** Finishes the game */
        public static void finishGame() {
            trainingPlayerLogic.finishGame();
        }
    }

    /** Controls user interface during the game */
    public static class TrainingUIController extends Controller {
        /** Sets current question text */
        public static void setQuestionText(String question) {
            trainingGameActivity.get().setQuestionText(question);
        }

        /** Sets current question result */
        public static void setQuestionResult(@NonNull String result) {
            trainingGameActivity.get().setQuestionResult(result);
        }

        /** Reacts on new questions */
        public static void onNewQuestion() {
            trainingGameActivity.get().onNewQuestion();
        }

        /** Sets showed time */
        public static void setTime(String time) {
            trainingGameActivity.get().setTime(time);
        }

        /** Sets question's answer */
        public static void setAnswer(String answer) {
            trainingGameActivity.get().setAnswerText(answer);
        }

        /** Sets question's comment */
        public static void setComment(String comment) {
            trainingGameActivity.get().setCommentText(comment);
        }

        /** Sets current score */
        public static void setScore(int correctAnswers, int wrongAnswers) {
            trainingGameActivity.get().setScore(String.valueOf(correctAnswers),
                    String.valueOf(wrongAnswers));
        }

        /** Sets current game location */
        public static void setLocation(GameActivityLocation location) {
            trainingGameActivity.get().setLocation(location);
        }

        /** Returns what the player has written **/
        @NonNull
        public static String getWhatWritten() {
            return trainingGameActivity.get().getWhatWritten();
        }

        /** Reacts on finishing the game */
        public static void onGameFinished() {
            trainingGameActivity.get().onGameFinished();
        }
    }
}
