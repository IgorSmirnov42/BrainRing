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
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.messageTypes.QuestionMessage;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.ui.OnlineGameActivity;

/** Controller for online game (both for server and user) */
public class OnlineController extends Controller {
    private static WeakReference<OnlineGameActivity> onlineGameActivity;

    /**
     * Returns stored online game activity
     *
     * @return stored online game activity
     */
    @NonNull
    public static OnlineGameActivity getOnlineGameActivity() {
        return onlineGameActivity.get();
    }

    /** Sets new online game activity */
    public static void setUI(@Nullable OnlineGameActivity onlineGameActivity) {
        OnlineController.onlineGameActivity = new WeakReference<>(onlineGameActivity);
    }

    /**
     * Initializes admin's logic and starts game.
     * Called by server only
     */
    public static void startOnlineGame() {
        OnlineAdminLogicController.adminLogic = new OnlineGameAdminLogic();
        OnlineAdminLogicController.adminLogic.newQuestion();
    }

    /** Finishes current game */
    public static void finishOnlineGame() {
        if (OnlineAdminLogicController.adminLogic != null) {
            Log.d(Controller.APP_TAG,"Clearing admin logic");
            OnlineAdminLogicController.adminLogic.finishGame();
            OnlineAdminLogicController.adminLogic = null;
        }
        if (OnlineUserLogicController.userLogic != null) {
            Log.d(Controller.APP_TAG,"Clearing user logic");
            OnlineUserLogicController.userLogic.finishGame();
            OnlineUserLogicController.userLogic = null;
        }
        if (NetworkController.network != null) {
            Log.d(Controller.APP_TAG,"Clearing network");
            NetworkController.network.finish();
            NetworkController.network = null;
        }
    }

    /** Switches to game finished activity, clearing current */
    public static void showGameFinishedActivity(@NonNull String message) {
        if (onlineGameActivity == null) {
            Log.wtf(Controller.APP_TAG, "Online activity is null but shouldn't");
            return;
        }
        onlineGameActivity.get().showGameFinishedActivity(message);
    }

    /** Methods to interact with admin's logic */
    public static class OnlineAdminLogicController {
        private static OnlineGameAdminLogic adminLogic;

        /** Reacts on one's false start. Can be called even after publishing */
        public static void onFalseStart(@NonNull String userId) {
            adminLogic.onFalseStart(userId);
        }

        /**
         * Allows or forbids to answer team that pushed answer button
         * Determines false starts
         */
        public static void onAnswerIsReady(@NonNull String userId, long time) {
            adminLogic.onAnswerIsReady(userId, time);
        }

        /** Rejects or accepts answer written by user */
        public static void onAnswerIsWritten(@NonNull String writtenAnswer, @NonNull String id) {
            adminLogic.onAnswerIsWritten(writtenAnswer, id);
        }

        /**
         * Gets message from user that he/she didn't push a button at time.
         * It is not equal incorrect answer because if opponent pushed a button then this user will have
         * second countdown
         */
        public static void onTimeLimit(int roundNumber, @NonNull String userId) {
            adminLogic.onTimeLimit(roundNumber, userId);
        }

        /** Counts {@code TIME_TO_READ_QUESTION} seconds and sends signal allowing pushing a button */
        public static void publishing() {
            adminLogic.publishing();
        }

        /** Marks user as ready for next question */
        public static void onReadyForQuestion(@NonNull String userId) {
            adminLogic.onReadyForQuestion(userId);
        }
    }

    /** Methods to interact with user's logic */
    public static class OnlineUserLogicController implements GameController {
        private static OnlineGameUserLogic userLogic;
        private static GameController gameController;

        /** Returns question data in format that is comfortable for complaining */
        public static ComplainedQuestion getQuestionData() {
            return userLogic.getQuestionData();
        }

        /** Signalizes server that user is now ready to continue a game */
        public static void readyForQuestion() {
            userLogic.readyForQuestion();
        }

        public static void onForbiddenToAnswer() {
            userLogic.onForbiddenToAnswer();
        }

        /**
         * Reacts on server's allowance to answer
         * Starts timer on {@code TIMER_TO_WRITE_ANSWER} seconds
         */
        public static void onAllowedToAnswer() {
            userLogic.onAllowedToAnswer();
        }

        /** Gets question and prints it on the screen. Sends handshake to server if needed */
        public static void onReceivingQuestion(int questionId, @NonNull String question) {
            if (userLogic == null) {
                userLogic = new OnlineGameUserLogic();
            }
            userLogic.onReceivingQuestion(questionId, question);
        }

        /**
         * Reacts on opponent's incorrect answer.
         * Shows opponent's answer, starts timer on {@code SECOND_COUNTDOWN} seconds
         */
        public static void onIncorrectOpponentAnswer(@NonNull String opponentAnswer) {
            userLogic.onIncorrectOpponentAnswer(opponentAnswer);
        }

        /** Shows answer and score on the screen, plays sound */
        public static void onReceivingAnswer(int firstUserScore,
                                             int secondUserScore,
                                             @NonNull String correctAnswer,
                                             @NonNull String comment,
                                             @NonNull String questionMessage) {
            userLogic.onReceivingAnswer(firstUserScore, secondUserScore, correctAnswer,
                    comment, questionMessage);
        }

        /** Reacts on opponent's pushing */
        public static void onOpponentIsAnswering() {
            userLogic.onOpponentIsAnswering();
        }

        public static GameController getInstance() {
            if (gameController == null) {
                gameController = new OnlineUserLogicController();
            }
            return gameController;
        }

        /**
         * Sends request to server trying to answer
         * Blocked in case of false start and if already answered
         */
        @Override
        public void answerButtonPushed() {
            userLogic.answerButtonPushed();
        }

        /** Sends written answer to server */
        @Override
        public void answerIsWritten(@NonNull String answer) {
            userLogic.answerIsWritten(answer);
        }

        /** Returns question data in format that is comfortable for complaining */
        @Override
        public ComplainedQuestion getCurrentQuestionData() {
            return OnlineUserLogicController.getQuestionData();
        }

        /**
         * Reacts on server's message about time start
         * Plays sound, changes button text, starts new timer on {@code FIRST_COUNTDOWN} seconds
         */
        public static void onTimeStart() {
            userLogic.onTimeStart();
        }
    }

    /** Class for controlling user interface in online game */
    public static class OnlineUIController {

        /** Asks stored activity to get what the user has written
         *
         * @return written text
         */
        @NonNull
        public static String getWhatWritten() {
            return onlineGameActivity.get().getWhatWritten();
        }

        /** Sets current question text */
        public static void setQuestionText(@NonNull String question) {
            onlineGameActivity.get().setQuestionText(question);
        }

        /** Sets current question result */
        public static void setQuestionResult(@NonNull String result) {
            onlineGameActivity.get().setQuestionResult(result);
        }

        /** Sets nick of the device owner */
        public static void setMyNick(@NonNull String nick) {
            onlineGameActivity.get().setMyNick(nick);
        }

        /** Sets nick of the opponent */
        public static void setOpponentNick(@NonNull String nick) {
            onlineGameActivity.get().setOpponentNick(nick);
        }

        /** Reacts on new question */
        public static void onNewQuestion() {
            onlineGameActivity.get().onNewQuestion();
        }

        /** Sets the answer button text*/
        public static void setAnswerButtonText(@NonNull String text) {
            onlineGameActivity.get().setAnswerButtonText(text);
        }

        /** Sets showed time */
        public static void setTime(@NonNull String time) {
            onlineGameActivity.get().setTime(time);
        }

        /** Sets question's answer */
        public static void setAnswer(@NonNull String answer) {
            onlineGameActivity.get().setAnswerText(answer);
        }

        /** Sets question's comment */
        public static void setComment(String comment) {
            onlineGameActivity.get().setCommentText(comment);
        }

        /** Changes current location */
        public static void setLocation(@NonNull GameActivityLocation location) {
            onlineGameActivity.get().setLocation(location);
        }

        /**
         * Changes current game score
         *
         * @param my new device owner score
         * @param opponent new score of the opponent
         */
        public static void setScore(int my, int opponent) {
            onlineGameActivity.get().setScore(String.valueOf(my), String.valueOf(opponent));
        }

        /** Sets opponent's answer */
        public static void setOpponentAnswer(@NonNull String answer) {
            onlineGameActivity.get().setOpponentAnswer(answer);
        }
    }

    /** Methods to interact with network */
    public static class NetworkController {
        private static Network network;

        /** Returns participant's name by his/her id */
        public static String getParticipantName(@NonNull String userId) {
            return network.getParticipantName(userId);
        }

        public static void continueGame() {
            network.continueGame();
        }

        /**
         * Finishes game
         *
         * @param message description of reason of finishing game
         */
        public static void finishImmediately(@NonNull String message) {
            network.finishImmediately(message);
        }

        /** Initializes network, user logic and loges in */
        public static void createOnlineGame() {
            network = new Network();
            OnlineUserLogicController.userLogic = new OnlineGameUserLogic();
            DatabaseController.generateNewSequence();
            onlineGameActivity.get().signIn();
        }

        public static void updateScore() {
            network.updateScore();
        }

        /** Sends question. Starts {@code handshakeTimer} */
        public static void sendQuestion(@NonNull QuestionMessage message) {
            network.sendQuestion(message);
        }

        /**
         * Finishes network part of online game
         * Cancels all timers, leaves room, updates rating
         */
        public static void finish() {
            network.finish();
        }

        /** Determines whether server of this game is on current device */
        public static boolean iAmServer() {
            return network.iAmServer();
        }

        /** Saves Google account, loads current score of user. On success starts searching for opponents */
        public static void loggedIn(@NonNull GoogleSignInAccount signedInAccount) {
            network.onSignedIn(signedInAccount);
        }

        /** Starts quick game with 1 auto matched player */
        public static void startGame() {
            network.startQuickGame();
        }

        /** Sends message directly to server */
        public static void sendMessageToServer(@NonNull Message message) {
            network.sendMessageToServer(message);
        }

        public static String getMyParticipantId() {
            return network.getMyParticipantId();
        }

        /**
         * Returns opponent's participant id
         * Returns null if was not found
         */
        public static String getOpponentParticipantId() {
            return network.getOpponentParticipantId();
        }

        /**
         * Sends message to user by id reliably.
         * If sending is unsuccessful repeats it {@code TIMES_TO_SEND} times until success
         * If there was no success, panics
         * Can send message to itself
         */
        public static void sendMessageToConcreteUser(@NonNull String userId, @NonNull Message message) {
            network.sendMessageToConcreteUser(userId, message);
        }

        /** Sends message to all users in a room (and to itself) */
        public static void sendMessageToAll(@NonNull Message message) {
            network.sendMessageToAll(message);
        }
    }
}
