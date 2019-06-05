package ru.spbhse.brainring.controllers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.lang.ref.WeakReference;

import ru.spbhse.brainring.files.ComplainedQuestion;
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

    public static void setUI(@Nullable OnlineGameActivity ui) {
        onlineGameActivity = new WeakReference<>(ui);
    }

    public static void startOnlineGame() {
        OnlineAdminLogicController.adminLogic = new OnlineGameAdminLogic();
        OnlineAdminLogicController.adminLogic.newQuestion();
    }

    public static void finishOnlineGame(boolean clearUI) {
        if (OnlineAdminLogicController.adminLogic != null) {
            Log.d("BrainRing","Clearing admin logic");
            OnlineAdminLogicController.adminLogic.finishGame();
            OnlineAdminLogicController.adminLogic = null;
        }
        if (OnlineUserLogicController.userLogic != null) {
            Log.d("BrainRing","Clearing user logic");
            OnlineUserLogicController.userLogic.finishGame();
            OnlineUserLogicController.userLogic = null;
        }
        if (NetworkController.network != null) {
            Log.d("BrainRing","Clearing network");
            NetworkController.network.finish();
            NetworkController.network = null;
        }
        if (clearUI && onlineGameActivity != null) {
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

        public static void onTimeLimit(int roundNumber, String userId) {
            adminLogic.onTimeLimit(roundNumber, userId);
        }

        public static void publishing() {
            adminLogic.publishing();
        }
    }

    public static class OnlineUserLogicController implements GameController {
        private static OnlineGameUserLogic userLogic;
        private static GameController gameController;

        public static ComplainedQuestion getQuestionData() {
            return userLogic.getQuestionData();
        }

        public static void onForbiddenToAnswer() {
            userLogic.onForbiddenToAnswer();
        }

        public static void onAllowedToAnswer() {
            userLogic.onAllowedToAnswer();
        }

        public static void onReceivingQuestion(int questionId, @NonNull String question) {
            if (userLogic == null) {
                userLogic = new OnlineGameUserLogic();
            }
            userLogic.onReceivingQuestion(questionId, question);
        }

        public static void onIncorrectOpponentAnswer(@NonNull String opponentAnswer) {
            userLogic.onIncorrectOpponentAnswer(opponentAnswer);
        }

        public static void onReceivingAnswer(int firstUserScore,
                                             int secondUserScore,
                                             @NonNull String correctAnswer,
                                             @NonNull String comment,
                                             @NonNull String questionMessage) {
            userLogic.onReceivingAnswer(firstUserScore, secondUserScore, correctAnswer,
                    comment, questionMessage);
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
        public void answerIsWritten(@NonNull String answer) {
            userLogic.answerIsWritten(answer);
        }

        @Override
        public ComplainedQuestion getCurrentQuestionData() {
            return OnlineUserLogicController.getQuestionData();
        }

        public static void onTimeStart() {
            userLogic.onTimeStart();
        }
    }

    public static class OnlineUIController {

        public static String getWhatWritten() {
            return onlineGameActivity.get().getWhatWritten();
        }

        public static void setQuestionText(@NonNull String question) {
            onlineGameActivity.get().setQuestionText(question);
        }

        public static void setQuestionResult(@NonNull String result) {
            onlineGameActivity.get().setQuestionResult(result);
        }

        public static void setMyNick(@NonNull String nick) {
            onlineGameActivity.get().setMyNick(nick);
        }

        public static void setOpponentNick(@NonNull String nick) {
            onlineGameActivity.get().setOpponentNick(nick);
        }

        public static void onNewQuestion() {
            onlineGameActivity.get().onNewQuestion();
        }

        public static void setButtonText(@NonNull String text) {
            onlineGameActivity.get().setButtonText(text);
        }

        public static void setTime(@NonNull String time) {
            onlineGameActivity.get().setTime(time);
        }

        public static void setAnswer(@NonNull String answer) {
            onlineGameActivity.get().setAnswer(answer);
        }

        public static void setComment(String comment) {
            onlineGameActivity.get().setComment(comment);
        }

        public static void setLocation(@NonNull GameActivityLocation location) {
            onlineGameActivity.get().setLocation(location);
        }

        public static void setScore(int my, int opponent) {
            onlineGameActivity.get().setScore(String.valueOf(my), String.valueOf(opponent));
        }

        public static void setOpponentAnswer(@NonNull String answer) {
            onlineGameActivity.get().setOpponentAnswer(answer);
        }
    }

    public static class NetworkController {
        private static Network network;

        public static String getParticipantName(@NonNull String userId) {
            return network.getParticipantName(userId);
        }

        public static void createOnlineGame() {
            network = new Network();
            Log.d("BrainRing", "Network: " + network);
            OnlineUserLogicController.userLogic = new OnlineGameUserLogic();
            onlineGameActivity.get().signIn();
        }

        public static void sendQuestion(@NonNull byte[] message) {
            if (network != null) {
                network.sendQuestion(message);
            }
        }

        public static void finish() {
            if (network != null) {
                network.finish();
            }
        }

        public static boolean iAmServer() {
            return network.iAmServer();
        }

        public static void loggedIn(@NonNull GoogleSignInAccount signedInAccount) {
            if (network == null) {
                Log.wtf("BrainRing", "Logged in but network is null");
                return;
            }
            Log.d("BrainRing", "Logged in");
            network.onSignedIn(signedInAccount);
        }

        public static void startGame() {
            Log.d("BrainRing", "StartQuick network: " + network);
            network.startQuickGame();
        }

        public static void sendMessageToServer(@NonNull byte[] message) {
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

        public static void sendMessageToConcreteUser(@NonNull String userId, @NonNull byte[] message) {
            if (network == null) {
                Log.wtf("BrainRing", "Sending message but network is null");
                return;
            }
            network.sendMessageToConcreteUser(userId, message);
        }

        public static void sendMessageToAll(@NonNull byte[] message) {
            if (network == null) {
                Log.wtf("BrainRing", "Sending message but network is null");
                return;
            }
            network.sendMessageToAll(message);
        }
    }
}
