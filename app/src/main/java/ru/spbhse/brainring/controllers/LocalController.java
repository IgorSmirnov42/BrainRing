package ru.spbhse.brainring.controllers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

/** Controller for local game (for both player and jury) */
public class LocalController extends Controller {
    private static WeakReference<JuryActivity> juryActivity;
    private static WeakReference<PlayerActivity> playerActivity;

    public static JuryActivity getJuryActivity() {
        return juryActivity.get();
    }

    public static PlayerActivity getPlayerActivity() {
        return playerActivity.get();
    }

    public static void setUI(@Nullable JuryActivity ui) {
        juryActivity = new WeakReference<>(ui);
    }

    public static void setUI(@Nullable PlayerActivity ui) {
        playerActivity = new WeakReference<>(ui);
    }

    /** Initializes admin logic in local game */
    public static void initializeLocalGame(int firstTimer, int secondTimer) {
        LocalAdminLogicController.adminLogic = new LocalGameAdminLogic(firstTimer, secondTimer);
    }

    /** Finishes local game. If {@code clearUI} is true finishes current activity */
    public static void finishLocalGame(boolean clearUI) {
        if (LocalAdminLogicController.adminLogic != null) {
            LocalAdminLogicController.adminLogic.finishGame();
            LocalAdminLogicController.adminLogic = null;
        }
        LocalPlayerLogicController.playerLogic = null;
        if (LocalNetworkController.network != null) {
            LocalNetworkController.network.finish();
            LocalNetworkController.network = null;
        }
        LocalNetworkPlayerController.network = null;
        LocalNetworkAdminController.network = null;
        if (clearUI) {
            if (juryActivity != null) {
                finishActivity(juryActivity.get());
            }
            if (playerActivity != null) {
                finishActivity(playerActivity.get());
            }
        }
    }

    /** Initializes player logic */
    public static void initializeLocalPlayer() {
        LocalPlayerLogicController.playerLogic = new LocalGamePlayerLogic();
    }

    /** Functions to interact with admin's logic */
    public static class LocalAdminLogicController {
        private static LocalGameAdminLogic adminLogic;

        /** Changes user's information when he/she answers on handshake */
        public static void onHandshakeAccept(@NonNull String userId) {
            adminLogic.onHandshakeAccept(userId);
        }

        public static String getGreenScore() {
            return adminLogic.getGreenScore();
        }

        public static String getRedScore() {
            return adminLogic.getRedScore();
        }

        /** Called when jury accepts answer */
        public static void onAcceptAnswer() {
            adminLogic.onAcceptAnswer();
        }

        /** Called when jury rejects answer */
        public static void onRejectAnswer() {
            adminLogic.onRejectAnswer();
        }

        /**
         * Called when jury pushes button to switch location
         * @return true if location was switched
         */
        public static boolean toNextState() {
            return adminLogic.toNextState();
        }

        /**
         * Allows or forbids to answer team that pushed answer button
         * Determines false starts
         */
        public static void onAnswerIsReady(@NonNull String userId) {
            adminLogic.onAnswerIsReady(userId);
        }

        /** Determines if jury can change score now and pluses point if possible */
        public static void plusPoint(int userNumber) {
            adminLogic.plusPoint(userNumber);
            LocalAdminUIController.redraw();
        }

        /** Determines if jury can change score now and minuses point if possible */
        public static void minusPoint(int userNumber) {
            adminLogic.minusPoint(userNumber);
            LocalAdminUIController.redraw();
        }
    }

    /** Functions to interact with player's logic */
    public static class LocalPlayerLogicController {
        private static LocalGamePlayerLogic playerLogic;

        /** Shows toast with forbiddance to answer */
        public static void onForbiddenToAnswer() {
            playerLogic.onForbiddenToAnswer();
        }

        /** Shows toast with allowance to answer */
        public static void onAllowedToAnswer() {
            playerLogic.onAllowedToAnswer();
        }

        /** Shows toast with false start message */
        public static void onFalseStart() {
            playerLogic.onFalseStart();
        }

        /** Plays sound of time start */
        public static void onTimeStart() {
            playerLogic.onTimeStart();
        }

        /**
         * Sends message to server signalizing that team is ready to answer
         * Called when team pushed the button
         */
        public static void answerButtonPushed() {
            playerLogic.answerButtonPushed();
        }
    }

    /** Functions to interact with admin's UI */
    public static class LocalAdminUIController {
        public static void redraw() {
            juryActivity.get().redrawLocation();
        }

        public static void setLocation(@NonNull LocalGameLocation location) {
            juryActivity.get().setLocation(location);
        }

        public static void showTime(long time) {
            juryActivity.get().showTime(time);
        }

        public static void onReceivingAnswer(@NonNull String color) {
            juryActivity.get().onReceivingAnswer(color);
        }

        public static void setGreenStatus(@NonNull String status) {
            juryActivity.get().setGreenStatus(status);
        }

        public static void setRedStatus(@NonNull String status) {
            juryActivity.get().setRedStatus(status);
        }
    }

    /** Functions to interact with admin's part of network */
    public static class LocalNetworkAdminController {
        private static LocalNetworkAdmin network;

        /** Creates network and loges in */
        public static void createLocalGame() {
            network = new LocalNetworkAdmin();
            LocalNetworkController.network = network;
            juryActivity.get().signIn();
        }

        /** Initializes users, starts game cycle */
        public static void startGameCycle() {
            LocalAdminLogicController.adminLogic.addUsers(network.getGreenId(),
                    network.getRedId());
        }

        /** Sends regular handshake */
        public static void handshake() {
            network.regularHandshake();
        }
    }

    /** Functions to interact with player's part of network */
    public static class LocalNetworkPlayerController {
        private static LocalNetworkPlayer network;

        /** Initializes network and loges in */
        public static void createLocalGame(@NonNull String color) {
            network = new LocalNetworkPlayer(color);
            LocalNetworkController.network = network;
            playerActivity.get().signIn();
        }

        /** Sends message directly to server */
        public static void sendMessageToServer(@NonNull byte[] message) {
            network.sendMessageToServer(message);
        }
    }

    /** Functions that are common in network for player and jury */
    public static class LocalNetworkController {
        private static LocalNetwork network;

        public static void loggedIn(GoogleSignInAccount signedInAccount) {
            network.signIn(signedInAccount);
            network.startQuickGame();
        }

        public static void sendMessageToConcreteUser(@NonNull String userId,
                                                     @NonNull byte[] message) {
            network.sendMessageToConcreteUser(userId, message);
        }

        /** Sends message to everybody in a room except itself */
        public static void sendMessageToOthers(@NonNull byte[] message) {
            network.sendMessageToOthers(message);
        }
    }
}
