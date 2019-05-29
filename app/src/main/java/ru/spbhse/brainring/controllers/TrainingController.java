package ru.spbhse.brainring.controllers;

import java.lang.ref.WeakReference;

import ru.spbhse.brainring.logic.TrainingPlayerLogic;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.ui.TrainingGameActivity;

public class TrainingController extends Controller {
    private static WeakReference<TrainingGameActivity> trainingGameActivity;

    public static void setUI(TrainingGameActivity ui) {
        trainingGameActivity = new WeakReference<>(ui);
    }

    public static TrainingGameActivity getTrainingGameActivity() {
        return trainingGameActivity.get();
    }


    public static void createTrainingGame() {
        TrainingLogicController.trainingPlayerLogic = new TrainingPlayerLogic();
        TrainingLogicController.trainingPlayerLogic.newQuestion();
    }

    public static class TrainingLogicController implements GameController {
        private static TrainingPlayerLogic trainingPlayerLogic;
        private static GameController gameController;

        public static GameController getInstance() {
            if (gameController == null) {
                gameController = new TrainingLogicController();
            }
            return gameController;
        }

        public static void setAnswerTime(int answerTime) {
            trainingPlayerLogic.setAnswerTime(answerTime);
        }

        @Override
        public void answerButtonPushed() {
            trainingPlayerLogic.answerButtonPushed();
        }

        @Override
        public void answerIsWritten(String answer) {
            trainingPlayerLogic.answerIsWritten(answer);
        }

        public static void finishGame() {
            trainingPlayerLogic.finishGame();
        }
    }

    public static class TrainingUIController extends Controller {
        public static void setQuestionText(String question) {
            trainingGameActivity.get().setQuestionText(question);
        }

        public static void onNewQuestion() {
            trainingGameActivity.get().onNewQuestion();
        }

        public static void setTime(String time) {
            trainingGameActivity.get().setTime(time);
        }

        public static void setAnswer(String answer) {
            trainingGameActivity.get().setAnswer(answer);
        }

        public static void setComment(String comment) {
            trainingGameActivity.get().setComment(comment);
        }

        public static void setScore(int myScore, int maxScore) {
            trainingGameActivity.get().setScore(myScore, maxScore);
        }

        public static void setLocation(GameActivityLocation location) {
            trainingGameActivity.get().setLocation(location);
        }

        public static String getWhatWritten() {
            return trainingGameActivity.get().getWhatWritten();
        }
    }
}
