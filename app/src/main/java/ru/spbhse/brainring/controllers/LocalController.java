package ru.spbhse.brainring.controllers;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.lang.ref.WeakReference;

import ru.spbhse.brainring.logic.LocalGameAdminLogic;
import ru.spbhse.brainring.logic.LocalGamePlayerLogic;
import ru.spbhse.brainring.network.LocalNetwork;
import ru.spbhse.brainring.network.LocalNetworkAdmin;
import ru.spbhse.brainring.network.LocalNetworkPlayer;
import ru.spbhse.brainring.ui.JuryActivity;
import ru.spbhse.brainring.ui.LocalGameLocation;
import ru.spbhse.brainring.ui.PlayerActivity;

public class LocalController extends Controller {
    private static WeakReference<JuryActivity> juryActivity;
    private static WeakReference<PlayerActivity> playerActivity;

    public static JuryActivity getJuryActivity() {
        return juryActivity.get();
    }

    public static PlayerActivity getPlayerActivity() {
        return playerActivity.get();
    }

    public static void setUI(JuryActivity ui) {
        juryActivity = new WeakReference<>(ui);
    }

    public static void setUI(PlayerActivity ui) {
        playerActivity = new WeakReference<>(ui);
    }

    public static void initializeLocalGame(int firstTimer, int secondTimer) {
        LocalAdminLogicController.adminLogic = new LocalGameAdminLogic(firstTimer, secondTimer);
    }

    public static void finishLocalGameAsAdmin() {
        if (LocalAdminLogicController.adminLogic != null) {
            LocalAdminLogicController.adminLogic.finishGame();
            LocalAdminLogicController.adminLogic = null;
        }
        LocalNetworkAdminController.network = null;
        LocalNetworkController.network = null;
        if (juryActivity != null) {
            finishActivity(juryActivity.get());
        }
    }

    public static void initializeLocalPlayer() {
        LocalPlayerLogicController.playerLogic = new LocalGamePlayerLogic();
    }

    public static void finishLocalGameAsPlayer() {
        LocalPlayerLogicController.playerLogic = null;
        LocalNetworkController.network = null;
        LocalNetworkPlayerController.network = null;
        if (playerActivity != null) {
            finishActivity(playerActivity.get());
        }
    }

    public static class LocalAdminLogicController {
        private static LocalGameAdminLogic adminLogic;

        public static void onHandshakeAccept(String userId) {
            adminLogic.onHandshakeAccept(userId);
        }

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
            LocalAdminUIController.redraw();
        }

        public static void minusPoint(int userNumber) {
            adminLogic.minusPoint(userNumber);
            LocalAdminUIController.redraw();
        }
    }

    public static class LocalPlayerLogicController {
        private static LocalGamePlayerLogic playerLogic;

        public static void onForbiddenToAnswer() {
            playerLogic.onForbiddenToAnswer();
        }

        public static void onAllowedToAnswer() {
            playerLogic.onAllowedToAnswer();
        }

        public static void onFalseStart() {
            playerLogic.onFalseStart();
        }

        public static void onTimeStart() {
            playerLogic.onTimeStart();
        }

        public static void answerButtonPushed() {
            playerLogic.answerButtonPushed();
        }
    }

    public static class LocalAdminUIController {
        public static void redraw() {
            juryActivity.get().redrawLocation();
        }

        public static void setLocation(LocalGameLocation location) {
            juryActivity.get().setLocation(location);
        }

        public static void showTime(long time) {
            juryActivity.get().showTime(time);
        }

        public static void onReceivingAnswer(String color) {
            juryActivity.get().onReceivingAnswer(color);
        }

        public static void setGreenStatus(String status) {
            juryActivity.get().setGreenStatus(status);
        }

        public static void setRedStatus(String status) {
            juryActivity.get().setRedStatus(status);
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
            LocalAdminLogicController.adminLogic.addUsers(network.getGreenId(),
                    network.getRedId());
        }

        public static void handshake() {
            network.regularHandshake();
        }
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

        public static void leaveRoom() {
            if (network != null) {
                network.leaveRoom();
            }
        }

        public static void loggedIn(GoogleSignInAccount signedInAccount) {
            network.googleSignInAccount = signedInAccount;
            network.startQuickGame();
        }

        public static void sendMessageToConcreteUser(String userId, byte[] message) {
            network.sendMessageToConcreteUser(userId, message);
        }

        public static void sendMessageToOthers(byte[] message) {
            network.sendMessageToOthers(message);
        }
    }
}
