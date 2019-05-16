package ru.spbhse.brainring.controllers;

import java.lang.ref.WeakReference;

import ru.spbhse.brainring.logic.TrainingPlayerLogic;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.ui.TrainingGameActivity;

public class TrainingController extends Controller {
    static WeakReference<TrainingGameActivity> trainingGameActivity;

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

    public static class TrainingLogicController extends Controller {
        private static TrainingPlayerLogic trainingPlayerLogic;
        /*public static void onForbiddenToAnswer() {
            trainingPlayerLogic.onForbiddenToAnswer();
        }

        public static void onAllowedToAnswer() {
            trainingPlayerLogic.onAllowedToAnswer();
        }

        public static void onReceivingQuestion(String question) {
            if (trainingPlayerLogic == null) {
                trainingPlayerLogic = new TrainingPlayerLogic();
            }
            trainingPlayerLogic.onReceivingQuestion(question);
        }

        public static void onIncorrectOpponentAnswer(String opponentAnswer) {
            trainingPlayerLogic.onIncorrectOpponentAnswer(opponentAnswer);
        }

        public static void onReceivingAnswer(int firstUserScore, int secondUserScore, String correctAnswer) {
            trainingPlayerLogic.onReceivingAnswer(firstUserScore, secondUserScore, correctAnswer);
        }

        public static void onOpponentIsAnswering() {
            trainingPlayerLogic.onOpponentIsAnswering();
        }*/

        // функция, которую должен вызывать UI при нажатии на кнопку в layout 2a
        public static void answerButtonPushed() {
            trainingPlayerLogic.answerButtonPushed();
        }

        // функция, которую должен вызывать UI при нажатии на кнопку в layout 2b
        // answer -- введенный текст
        public static void answerIsWritten(String answer) {
            trainingPlayerLogic.answerIsWritten(answer);
        }

        public static void finishGame() {
            trainingPlayerLogic.finishGame();
        }

        /*public static void onTimeStart() {
            trainingPlayerLogic.onTimeStart();
        }*/
    }

    public static class TrainingUIController extends Controller {
        public static void setQuestionText(String question) {
            trainingGameActivity.get().setQuestionText(question);
        }

        public static void onNewQuestion() {
            trainingGameActivity.get().onNewQuestion();
        }

        public static void setButtonText(String text) {
            trainingGameActivity.get().setButtonText(text);
        }

        public static void setTime(String time) {
            trainingGameActivity.get().setTime(time);
        }

        public static void setAnswer(String answer) {
            trainingGameActivity.get().setAnswer(answer);
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
