package ru.spbhse.brainring;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.lang.ref.WeakReference;
import java.util.Random;

import ru.spbhse.brainring.database.QuestionDataBase;
import ru.spbhse.brainring.logic.LocalGameAdminLogic;
import ru.spbhse.brainring.logic.LocalGamePlayerLogic;
import ru.spbhse.brainring.logic.OnlineGameAdminLogic;
import ru.spbhse.brainring.logic.OnlineGameUserLogic;
import ru.spbhse.brainring.network.LocalNetwork;
import ru.spbhse.brainring.network.LocalNetworkAdmin;
import ru.spbhse.brainring.network.LocalNetworkPlayer;
import ru.spbhse.brainring.network.Network;
import ru.spbhse.brainring.ui.GameActivity;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.ui.JuryActivity;
import ru.spbhse.brainring.ui.LocalGameLocation;
import ru.spbhse.brainring.ui.PlayerActivity;
import ru.spbhse.brainring.utils.Question;

public class Controller {

    private static WeakReference<GameActivity> gameActivity;
    private static WeakReference<JuryActivity> juryActivity;
    private static WeakReference<PlayerActivity> playerActivity;

    public static GameActivity getGameActivity() {
        return gameActivity.get();
    }

    public static JuryActivity getJuryActivity() {
        return juryActivity.get();
    }

    public static PlayerActivity getPlayerActivity() {
        return playerActivity.get();
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

        public static void onAcceptAnswer() {
            adminLogic.onAcceptAnswer();
        }

        public static void onRejectAnswer() {
            adminLogic.onRejectAnswer();
        }

        public static boolean toNextState() {
            return adminLogic.toNextState();
        }

        public static void onAnswerIsReady(String userId) {
            adminLogic.onAnswerIsReady(userId);
        }

        public static void plusPoint(int userNumber) {
            adminLogic.plusPoint(userNumber);
            LocalNetworkAdminUIController.redraw();
        }

        public static void minusPoint(int userNumber) {
            adminLogic.minusPoint(userNumber);
            LocalNetworkAdminUIController.redraw();
        }
    }

    public static class OnlineAdminLogicController {
        private static OnlineGameAdminLogic adminLogic;

        public static void onAnswerIsReady(String userId) {
            adminLogic.onAnswerIsReady(userId);
        }

        public static void onAnswerIsWritten(String writtenAnswer, String id) {
            adminLogic.onAnswerIsWritten(writtenAnswer, id);
        }
    }

    public static class LocalUserLogicController {
        private static LocalGamePlayerLogic userLogic = new LocalGamePlayerLogic(); //

        public static void onForbiddenToAnswer() {
            userLogic.onForbiddenToAnswer();
        }

        public static void onAllowedToAnswer() {
            userLogic.onAllowedToAnswer();
        }

        public static void answerButtonPushed() {
            userLogic.answerButtonPushed();
        }
    }

    public static class OnlineUserLogicController {
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

        public static void onReceivingTick(String secondsLeft) {
            userLogic.onReceivingTick(secondsLeft);
        }

        public static void onTimeStart() {
            userLogic.onTimeStart();
        }

        public static void onTimeToWriteAnswerIsOut() {
            userLogic.onTimeToWriteAnswerIsOut();
        }

        public static void onFalseStart() {
            userLogic.onFalseStart();
        }
    }

    public static class LocalNetworkAdminUIController {
        public static void redraw() {
            juryActivity.get().redrawLocation();
        }

        public static void setLocation(LocalGameLocation location) {
            juryActivity.get().setLocation(location);
        }

        public static void onReceivingAnswer() {
            juryActivity.get().onReceivingAnswer();
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
            System.out.println("BEGIN LOGIN");
            juryActivity.get().signIn();
        }

        public static void startGameCycle() {
            LocalAdminLogicController.adminLogic.addUsers(network.getGreenId(),
                    network.getRedId());
            if (!LocalAdminLogicController.toNextState()) {
                Log.wtf("BrainRing", "Cannot start new game");
            }
        }

        /*public static String getGreenParticipantId() {
            return network.getGreenId();
        }

        public static String getRedParticipantId() {
            return network.getRedId();
        }*/
    }

    public static class LocalNetworkPlayerController {
        private static LocalNetworkPlayer network;

        public static void createLocalGame(String color) {
            network = new LocalNetworkPlayer(color);
            LocalNetworkController.network = network;
            playerActivity.get().signIn();
        }

        public static void sendMessageToServer(byte[] message) {
            network.sendMessageToServer(message);
        }
    }

    public static class LocalNetworkController {
        private static LocalNetwork network;

        public static void loggedIn(GoogleSignInAccount signedInAccount) {
            network.googleSignInAccount = signedInAccount;
            network.startQuickGame();
        }

        public static void sendMessageToConcreteUser(String userId, byte[] message) {
            network.sendMessageToConcreteUser(userId, message);
        }
    }

    public static class NetworkController {
        private static Network network;
        // функция, которую должен вызывать UI при нажатии на кнопку в layout 1
        public static void createOnlineGame() {
            network = new Network();
            gameActivity.get().signIn();
        }

        public static void leaveRoom() {
            if (network != null) {
                network.leaveRoom();
            }
        }

        public static void loggedIn(GoogleSignInAccount signedInAccount) {
            if (network == null) {
                Log.wtf("BrainRing", "Logged in but network is null");
                return;
            }
            network.googleSignInAccount = signedInAccount;
            network.startQuickGame();
        }

        public static void sendMessageToServer(byte[] message) {
            if (network == null) {
                Log.wtf("BrainRing", "Sending message to server but network is null");
                return;
            }
            network.sendMessageToServer(message);
        }

        public static String getMyParticipantId() {
            if (network == null) {
                Log.wtf("BrainRing", "Getting id but network is null");
                return null;
            }
            return network.getMyParticipantId();
        }

        public static String getOpponentParticipantId() {
            if (network == null) {
                Log.wtf("BrainRing", "Getting id but network is null");
                return null;
            }
            return network.getOpponentParticipantId();
        }

        public static void sendMessageToConcreteUser(String userId, byte[] message) {
            if (network == null) {
                Log.wtf("BrainRing", "Sending message but network is null");
                return;
            }
            network.sendMessageToConcreteUser(userId, message);
        }

        public static void sendMessageToAll(byte[] message) {
            if (network == null) {
                Log.wtf("BrainRing", "Sending message but network is null");
                return;
            }
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
        OnlineUserLogicController.userLogic = new OnlineGameUserLogic();
        OnlineAdminLogicController.adminLogic.newQuestion();
    }

    public static void finishOnlineGame() {
        if (OnlineAdminLogicController.adminLogic != null) {
            OnlineAdminLogicController.adminLogic.finishGame();
        }
        OnlineAdminLogicController.adminLogic = null;
        OnlineUserLogicController.userLogic = null;
        NetworkController.network = null;
        if (gameActivity != null) {
            GameActivity activity = gameActivity.get();
            if (activity != null) {
                activity.finish();
            }
        }
    }

    public static void initializeLocalGame() {
        LocalAdminLogicController.adminLogic = new LocalGameAdminLogic();
    }

    public static void finishLocalGameAsAdmin() {
        LocalAdminLogicController.adminLogic = null;
    }
}
