package ru.spbhse.brainring.controllers;

import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.lang.ref.WeakReference;

import ru.spbhse.brainring.logic.LocalGameAdminLogic;
import ru.spbhse.brainring.logic.LocalGamePlayerLogic;
import ru.spbhse.brainring.network.LocalNetwork;
import ru.spbhse.brainring.network.LocalNetworkAdmin;
import ru.spbhse.brainring.network.LocalNetworkPlayer;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.ui.JuryActivity;
import ru.spbhse.brainring.ui.LocalGameLocation;
import ru.spbhse.brainring.ui.PlayerActivity;

/** Controller for local game (for both player and jury) */
public class LocalController extends Controller {
    private static WeakReference<JuryActivity> juryActivity;
    private static WeakReference<PlayerActivity> playerActivity;

    /**
     * Returns stored jury activity
     *
     * @return stored jury activity
     */
    @NonNull
    public static JuryActivity getJuryActivity() {
        return juryActivity.get();
    }

    /**
     * Returns stored player activity
     *
     * @return stored jury activity
     */
    @NonNull
    public static PlayerActivity getPlayerActivity() {
        return playerActivity.get();
    }

    /**
     * Stores new jury activity
     *
     * @param juryActivity jury activity to store
     */
    public static void setUI(@NonNull JuryActivity juryActivity) {
        LocalController.juryActivity = new WeakReference<>(juryActivity);
    }

    /**
     * Stores new player activity
     *
     * @param playerActivity player activity to store
     */
    public static void setUI(@NonNull PlayerActivity playerActivity) {
        LocalController.playerActivity = new WeakReference<>(playerActivity);
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

    /** Methods to interact with admin's logic */
    public static class LocalAdminLogicController {
        private static LocalGameAdminLogic adminLogic;

        /** Changes user's information when he/she answers on handshake */
        public static void onHandshakeAccept(@NonNull String userId) {
            adminLogic.onHandshakeAccept(userId);
        }

        /**
         * Returns score of the green team
         *
         * @return score of the green team
         */
        @NonNull
        public static String getGreenScore() {
            return adminLogic.getGreenScore();
        }

        /**
         * Returns score of the red team
         *
         * @return score of the red team
         */
        @NonNull
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
         *
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

    /** Methods to interact with player's logic */
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
         * Called when some team pushed the button
         */
        public static void answerButtonPushed() {
            playerLogic.answerButtonPushed();
        }
    }

    /** Methods to interact with admin's UI */
    public static class LocalAdminUIController {
        static void redraw() {
            juryActivity.get().redrawLocation();
        }

        /**
         * Stores new {@code LocalGameLocation}
         *
         * @param location location to store
         */
        public static void setLocation(@NonNull LocalGameLocation location) {
            juryActivity.get().setLocation(location);
        }

        /**
         * Asks stored jury activity to show {@code time}
         *
         * @param time time to show
         */
        public static void showTime(long time) {
            juryActivity.get().showTime(time);
        }

        /**
         * Asks stored jury activity to react on answer of the team, specified by the color
         *
         * @param color color of the answering team. Must be {@literal red} or {@literal green}
         */
        public static void onReceivingAnswer(@NonNull String color) {
            juryActivity.get().onReceivingAnswer(color);
        }

        /**
         * Asks stored jury activity to change green team status to the given one
         *
         * @param status new status
         */
        public static void setGreenStatus(@NonNull String status) {
            juryActivity.get().setGreenStatus(status);
        }

        /**
         * Asks stored jury activity to change red team status to the given one
         *
         * @param status new status
         */
        public static void setRedStatus(@NonNull String status) {
            juryActivity.get().setRedStatus(status);
        }
    }

    /** Methods to interact with admin's part of network */
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

        public static void setRedPlayer(@NonNull String userId) {
            network.setRedPlayer(userId);
        }

        public static void setGreenPlayer(@NonNull String userId) {
            network.setGreenPlayer(userId);
        }
    }

    /** Methods to interact with player's part of network */
    public static class LocalNetworkPlayerController {
        private static LocalNetworkPlayer network;

        public static void doInitialHandshake(@NonNull String serverId) {
            network.doInitialHandshake(serverId);
        }

        /** Initializes network and loges in */
        public static void createLocalGame(@NonNull String color) {
            network = new LocalNetworkPlayer(color);
            LocalNetworkController.network = network;
            playerActivity.get().signIn();
        }

        /** Sends message directly to server */
        public static void sendMessageToServer(@NonNull Message message) {
            network.sendMessageToServer(message);
        }
    }

    /** Methods that are common in network for player and jury */
    public static class LocalNetworkController {
        private static LocalNetwork network;

        /** Starts quick game after logging in */
        public static void loggedIn(GoogleSignInAccount signedInAccount) {
            network.signIn(signedInAccount);
            network.startQuickGame();
        }

        /** Sends message to the user specified by {@code userId} */
        public static void sendMessageToConcreteUser(@NonNull String userId,
                                                     @NonNull Message message) {
            network.sendMessageToConcreteUser(userId, message);
        }

        /** Sends message to everybody in a room except itself */
        public static void sendMessageToOthers(@NonNull Message message) {
            network.sendMessageToOthers(message);
        }
    }
}
