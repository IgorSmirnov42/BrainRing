package ru.spbhse.brainring;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import ru.spbhse.brainring.logic.OnlineGameAdminLogic;
import ru.spbhse.brainring.logic.OnlineGameUserLogic;
import ru.spbhse.brainring.network.Network;
import ru.spbhse.brainring.ui.GameActivity;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.utils.Question;

public class Controller {

    public static GameActivity gameActivity;

    public static void setUI(GameActivity ui) {
        gameActivity = ui;
    }

    public static class AdminLogicController {
        private static OnlineGameAdminLogic adminLogic;

        public static void onAnswerIsReady(String userId) {
            adminLogic.onAnswerIsReady(userId);
        }

        public static void onAnswerIsWritten(String writtenAnswer) {
            adminLogic.onAnswerIsWritten(writtenAnswer);
        }
    }

    public static class UserLogicController {
        private static OnlineGameUserLogic userLogic;

        public static void onForbiddenToAnswer() {
            userLogic.onForbiddenToAnswer();
        }

        public static void onAllowedToAnswer() {
            userLogic.onAllowedToAnswer();
        }

        public static void onReceivingQuestion(String question) {
            if (userLogic == null) {
                userLogic = new OnlineGameUserLogic();
            }
            userLogic.onReceivingQuestion(question);
        }

        public static void onIncorrectOpponentAnswer(String opponentAnswer) {
            userLogic.onIncorrectOpponentAnswer(opponentAnswer);
        }

        public static void onReceivingAnswer(int firstUserScore, int secondUserScore, String correctAnswer) {
            userLogic.onReceivingAnswer(firstUserScore, secondUserScore, correctAnswer);
        }

        public static void onOpponentIsAnswering() {
            userLogic.onOpponentIsAnswering();
        }

        // функция, которую должен вызывать UI при нажатии на кнопку в layout 2a
        public static void answerButtonPushed() {
            userLogic.answerButtonPushed();
        }

        // функция, которую должен вызывать UI при нажатии на кнопку в layout 2b
        // answer -- введенный текст
        public static void answerIsWritten(String answer) {
            userLogic.answerIsWritten(answer);
        }
    }

    public static class UIController {
        public static void clearEditText() {
            gameActivity.clearEditText();
        }

        public static void hideKeyboard() {
            gameActivity.hideKeyboard();
        }

        public static void setQuestionText(String question) {
            gameActivity.setQuestionText(question);
        }

        public static void setAnswer(String answer) {
            gameActivity.setAnswer(answer);
        }

        public static void setLocation(GameActivityLocation location) {
            gameActivity.setLocation(location);
        }
    }

    public static class NetworkController {
        private static Network network;
        // функция, которую должен вызывать UI при нажатии на кнопку в layout 1
        public static void createOnlineGame() {
            network = new Network();
            gameActivity.signIn();
        }

        public static void loggedIn(GoogleSignInAccount signedInAccount) {
            network.googleSignInAccount = signedInAccount;
            network.startQuickGame();
        }

        public static void sendMessageToServer(byte[] message) {
            network.sendMessageToServer(message);
        }

        public static String getMyParticipantId() {
            return network.getMyParticipantId();
        }

        public static String getOpponentParticipantId() {
            return network.getOpponentParticipantId();
        }

        public static void sendMessageToConcreteUser(String userId, byte[] message) {
            network.sendMessageToConcreteUser(userId, message);
        }

        public static void sendMessageToAll(byte[] message) {
            network.sendMessageToAll(message);
        }
    }

    public static class DatabaseController {
        /** Gets random question from database */
        public static Question getRandomQuestion() {
            // TODO : get question from database
            return new Question("Что должен делать Серёжа?", "Писать код", "Работать", "gg");
        }
    }

    public static void startOnlineGame() {
        System.out.println("НАЧИНАЕМ ИГРУ");
        AdminLogicController.adminLogic = new OnlineGameAdminLogic();
        UserLogicController.userLogic = new OnlineGameUserLogic();
        AdminLogicController.adminLogic.newQuestion();
    }

    public static void finishOnlineGame() {
        AdminLogicController.adminLogic = null;
        UserLogicController.userLogic = null;
    }
}
