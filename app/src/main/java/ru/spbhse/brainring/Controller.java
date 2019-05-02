package ru.spbhse.brainring;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.lang.ref.WeakReference;
import java.util.Random;

import ru.spbhse.brainring.database.QuestionDataBase;
import ru.spbhse.brainring.logic.LocalGameAdminLogic;
import ru.spbhse.brainring.logic.OnlineGameAdminLogic;
import ru.spbhse.brainring.logic.OnlineGameUserLogic;
import ru.spbhse.brainring.network.LocalNetwork;
import ru.spbhse.brainring.network.LocalNetworkAdmin;
import ru.spbhse.brainring.network.LocalNetworkPlayer;
import ru.spbhse.brainring.network.Network;
import ru.spbhse.brainring.ui.GameActivity;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.ui.JuryActivity;
import ru.spbhse.brainring.ui.PlayerActivity;
import ru.spbhse.brainring.utils.Question;

public class Controller {

    private static WeakReference<GameActivity> gameActivity;
    private static WeakReference<JuryActivity> juryActivity;
    private static WeakReference<PlayerActivity> playerActivity;

    public static GameActivity getGameActivity() {
        return gameActivity.get();
    }

    public static void setUI(GameActivity ui) {
        gameActivity = new WeakReference<>(ui);
    }

    public static void setUI(JuryActivity ui) {
        juryActivity = new WeakReference<>(ui);
    }

    public static void setUI(PlayerActivity ui) {
        playerActivity = new WeakReference<>(ui);
    }

    public static class LocalAdminLogicController {
        private static LocalGameAdminLogic adminLogic;

        public static String getGreenScore() {
            return adminLogic.getGreenScore();
        }

        public static String getRedScore() {
            return adminLogic.getRedScore();
        }

        public static void toNextState() {
            // TODO
        }

        public static void onAnswerIsReady(String userId) {
            // TODO
        }

        public static void plusPoint(int userNumber) {
            adminLogic.plusPoint(userNumber);
        }

        public static void minusPoint(int userNumber) {
            adminLogic.minusPoint(userNumber);
        }
    }

    public static class OnlineAdminLogicController {
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

    public static class LocalNetworkAdminUIController {
        public void redraw() {
            juryActivity.get().redrawLocation();
        }
    }

    public static class NetworkUIController {
        public static void clearEditText() {
            gameActivity.get().clearEditText();
        }

        public static void hideKeyboard() {
            gameActivity.get().hideKeyboard();
        }

        public static void setQuestionText(String question) {
            gameActivity.get().setQuestionText(question);
        }

        public static void setAnswer(String answer) {
            gameActivity.get().setAnswer(answer);
        }

        public static void setLocation(GameActivityLocation location) {
            gameActivity.get().setLocation(location);
        }
    }

    public static class LocalNetworkAdminController {
        private static LocalNetworkAdmin network;

        public static void createLocalGame() {
            network = new LocalNetworkAdmin();
            LocalNetworkController.network = network;
            juryActivity.get().signIn();
        }

        public static void startGameCycle() {
            // TODO
        }

        public static String getGreenParticipantId() {
            return network.getGreenId();
        }

        public static String getRedParticipantId() {
            return network.getRedId();
        }
    }

    public static class LocalNetworkPlayerController {
        public static void createLocalGame(String color) {
            LocalNetworkController.network = new LocalNetworkPlayer(color);
            playerActivity.get().signIn();
        }
    }

    public static class LocalNetworkController {
        private static LocalNetwork network;

        public static void loggedIn(GoogleSignInAccount signedInAccount) {
            network.googleSignInAccount = signedInAccount;
            network.startQuickGame();
        }
    }

    public static class NetworkController {
        private static Network network;
        // функция, которую должен вызывать UI при нажатии на кнопку в layout 1
        public static void createOnlineGame() {
            network = new Network();
            gameActivity.get().signIn();
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
        private static final Random RAND = new Random();
        /** Gets random question from database */
        public static Question getRandomQuestion() {
            QuestionDataBase dataBase = gameActivity.get().dataBase;
            if (dataBase == null) {
                dataBase = new QuestionDataBase(gameActivity.get());
            }
            dataBase.openDataBase();
            int questionId = RAND.nextInt((int) dataBase.size());
            return dataBase.getQuestion(questionId);
        }
    }

    public static void startOnlineGame() {
        System.out.println("НАЧИНАЕМ ИГРУ");
        OnlineAdminLogicController.adminLogic = new OnlineGameAdminLogic();
        UserLogicController.userLogic = new OnlineGameUserLogic();
        OnlineAdminLogicController.adminLogic.newQuestion();
    }

    public static void finishOnlineGame() {
        OnlineAdminLogicController.adminLogic = null;
        UserLogicController.userLogic = null;
        NetworkController.network = null;
    }

    public static void initializeLocalGame() {
        LocalAdminLogicController.adminLogic = new LocalGameAdminLogic();
    }

    public static void finishLocalGameAsAdmin() {
        LocalAdminLogicController.adminLogic = null;
    }
}
