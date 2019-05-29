package ru.spbhse.brainring.controllers;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.lang.ref.WeakReference;

import ru.spbhse.brainring.logic.OnlineGameAdminLogic;
import ru.spbhse.brainring.logic.OnlineGameUserLogic;
import ru.spbhse.brainring.network.Network;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.ui.OnlineGameActivity;

public class OnlineController extends Controller {
    static WeakReference<OnlineGameActivity> onlineGameActivity;

    public static OnlineGameActivity getOnlineGameActivity() {
        return onlineGameActivity.get();
    }

    public static void setUI(OnlineGameActivity ui) {
        onlineGameActivity = new WeakReference<>(ui);
    }

    public static void startOnlineGame() {
        OnlineAdminLogicController.adminLogic = new OnlineGameAdminLogic();
        OnlineAdminLogicController.adminLogic.newQuestion();
    }

    public static void finishOnlineGame() {
        if (OnlineAdminLogicController.adminLogic != null) {
            OnlineAdminLogicController.adminLogic.finishGame();
            OnlineAdminLogicController.adminLogic = null;
        }
        if (OnlineUserLogicController.userLogic != null) {
            OnlineUserLogicController.userLogic.finishGame();
            OnlineUserLogicController.userLogic = null;
        }
        if (NetworkController.network != null) {
            NetworkController.network.updateRating();
            NetworkController.network = null;
        }
        if (onlineGameActivity != null) {
            finishActivity(onlineGameActivity.get());
        }
    }

    public static class OnlineAdminLogicController {
        private static OnlineGameAdminLogic adminLogic;

        public static void onFalseStart(String userId) {
            adminLogic.onFalseStart(userId);
        }

        public static void onAnswerIsReady(String userId, long time) {
            adminLogic.onAnswerIsReady(userId, time);
        }

        public static void onAnswerIsWritten(String writtenAnswer, String id) {
            adminLogic.onAnswerIsWritten(writtenAnswer, id);
        }

        public static void onTimeLimit(long roundNumber, String userId) {
            adminLogic.onTimeLimit(roundNumber, userId);
        }

        public static void publishing() {
            adminLogic.publishing();
        }
    }

    public static class OnlineUserLogicController implements GameController {
        private static OnlineGameUserLogic userLogic;
        private static GameController gameController;

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

        public static GameController getInstance() {
            if (gameController == null) {
                gameController = new OnlineUserLogicController();
            }
            return gameController;
        }

        @Override
        public void answerButtonPushed() {
            userLogic.answerButtonPushed();
        }

        @Override
        public void answerIsWritten(String answer) {
            userLogic.answerIsWritten(answer);
        }

        public static void onTimeStart() {
            userLogic.onTimeStart();
        }
    }

    public static class NetworkUIController {

        public static String getWhatWritten() {
            return onlineGameActivity.get().getWhatWritten();
        }

        public static void setQuestionText(String question) {
            onlineGameActivity.get().setQuestionText(question);
        }

        public static void onNewQuestion() {
            onlineGameActivity.get().onNewQuestion();
        }

        public static void setButtonText(String text) {
            onlineGameActivity.get().setButtonText(text);
        }

        public static void setTime(String time) {
            onlineGameActivity.get().setTime(time);
        }

        public static void setAnswer(String answer) {
            onlineGameActivity.get().setAnswer(answer);
        }

        public static void setComment(String comment) {
            onlineGameActivity.get().setComment(comment);
        }

        public static void setLocation(GameActivityLocation location) {
            onlineGameActivity.get().setLocation(location);
        }

        public static void setScore(int my, int opponent) {
            onlineGameActivity.get().setScore(my, opponent);
        }

        public static void setOpponentAnswer(String answer) {
            onlineGameActivity.get().setOpponentAnswer(answer);
        }
    }

    public static class NetworkController {
        private static Network network;

        public static void createOnlineGame() {
            network = new Network();
            OnlineUserLogicController.userLogic = new OnlineGameUserLogic();
            onlineGameActivity.get().signIn();
        }

        public static void sendQuestion(byte[] message) {
            if (network != null) {
                network.sendQuestion(message);
            }
        }

        public static void leaveRoom() {
            if (network != null) {
                network.updateRating();
                network.leaveRoom();
            }
        }

        public static boolean iAmServer() {
            return network.iAmServer();
        }

        public static void loggedIn(GoogleSignInAccount signedInAccount) {
            if (network == null) {
                Log.wtf("BrainRing", "Logged in but network is null");
                return;
            }
            Log.d("BrainRing", "Logged in");
            network.onSignedIn(signedInAccount);
        }

        public static void startGame() {
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
}
