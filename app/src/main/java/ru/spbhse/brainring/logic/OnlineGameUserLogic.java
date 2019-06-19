package ru.spbhse.brainring.logic;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.logic.timers.OnlineGameTimer;
import ru.spbhse.brainring.logic.timers.OnlineShowingAnswerTimer;
import ru.spbhse.brainring.logic.timers.OnlineWritingTimer;
import ru.spbhse.brainring.managers.OnlineGameManager;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerReadyMessage;
import ru.spbhse.brainring.network.messages.messageTypes.AnswerWrittenMessage;
import ru.spbhse.brainring.network.messages.messageTypes.FalseStartMessage;
import ru.spbhse.brainring.network.messages.messageTypes.HandshakeMessage;
import ru.spbhse.brainring.network.messages.messageTypes.ReadyForQuestionMessage;
import ru.spbhse.brainring.network.messages.messageTypes.TimeLimitMessage;
import ru.spbhse.brainring.ui.GameActivityLocation;
import ru.spbhse.brainring.utils.SoundPlayer;

/**
 * Realizing user logic in online mode.
 * Controls most of the timers in a game (timer on reading question, timer on writing answer,
 * timer to show answer, timer on thinking)
 * Depends on user actions (pushed buttons) and server commands (answering opponent, new question etc)
 */
public class OnlineGameUserLogic implements PlayerLogic {
    private OnlineGameManager manager;
    private SoundPlayer player = new SoundPlayer();
    private String currentQuestionText;
    /** Id of question from database. Need for complaining */
    private int currentQuestionId;
    private String currentQuestionAnswer;
    /** Time when received message on time start from server */
    private long startQuestionTime;
    /** Flag to determine if user have received new question from server */
    private boolean questionReceived;
    /**
     * Flag to determine whether user is ready for next question
     * User is ready if he/she pushed such button or {@code TIME_TO_SHOW_ANSWER} seconds passed
     */
    private boolean readyForQuestion;
    /** Flag to determine whether server has already sent signal allowing to push a button */
    private boolean timeStarted;
    /** Flag to determine whether user has already answered (or had false start) in this round */
    private boolean alreadyAnswered;

    private static final int TIME_TO_SHOW_ANSWER = 10;
    private static final int TIME_TO_WRITE_ANSWER = 20;
    /** Countdown on thinking after reading question */
    private static final int FIRST_COUNTDOWN = 20;
    /** Countdown on thinking after incorrect opponent's answer */
    private static final int SECOND_COUNTDOWN = 20;
    /** Time when timer starts showing left time to think */
    private static final int SENDING_COUNTDOWN = 5;

    private static final Message FALSE_START = new FalseStartMessage();
    private static final Message HANDSHAKE = new HandshakeMessage();
    private static final Message READY_FOR_QUESTION = new ReadyForQuestionMessage();

    private CountDownTimer timer;

    public OnlineGameUserLogic(OnlineGameManager onlineGameManager) {
        manager = onlineGameManager;
    }

    /** Returns question data in format that is comfortable for complaining */
    @NonNull
    @Override
    public ComplainedQuestion getCurrentQuestionData() {
        return new ComplainedQuestion(currentQuestionText, currentQuestionAnswer, currentQuestionId);
    }

    /**
     * Reacts on server's forbiddance to answer
     * Called when:
     * 1. User has already answered
     * 2. Other user is answering now
     * 3. User did false start before
     */
    public void onForbiddenToAnswer() {
        Toast.makeText(manager.getActivity(),
                manager.getActivity().getString(R.string.forbidden_answer),
                Toast.LENGTH_SHORT).show();
    }

    /** Reacts on user's false start */
    private void onFalseStart() {
        if (!alreadyAnswered) {
            alreadyAnswered = true;
            manager.getNetwork().sendMessageToServer(FALSE_START);
        }
        Toast.makeText(manager.getActivity(),
                manager.getActivity().getString(R.string.false_start),
                Toast.LENGTH_SHORT).show();
    }

    /** Signalizes server that user is now ready to continue a game */
    public void readyForQuestion() {
        if (readyForQuestion) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        readyForQuestion = true;
        manager.getNetwork().sendMessageToServer(READY_FOR_QUESTION);
    }

    /**
     * Reacts on server's message about time start
     * Plays sound, changes button text, starts new timer on {@code FIRST_COUNTDOWN} seconds
     */
    public void onTimeStart() {
        timeStarted = true;
        player.play(manager.getActivity(), R.raw.start);
        manager.getActivity().setAnswerButtonText(manager.getActivity()
                .getString(R.string.button_push_text));
        startQuestionTime = System.currentTimeMillis();
        timer = new OnlineGameTimer(FIRST_COUNTDOWN, SENDING_COUNTDOWN, 1, this);
        timer.start();
    }

    /**
     * Reacts on tick that means that {@code secondsLeft} time is left.
     * Plays sound, shows time on a screen
     */
    public void onReceivingTick(long secondsLeft) {
        player.play(manager.getActivity(), R.raw.countdown);
        manager.getActivity().setTime(String.valueOf(secondsLeft));
    }

    /**
     * Reacts on server's allowance to answer
     * Starts timer on {@code TIMER_TO_WRITE_ANSWER} seconds
     */
    public void onAllowedToAnswer() {
        alreadyAnswered = true;
        Log.d(Controller.APP_TAG,"Allowed to answer");
        manager.getActivity().setLocation(GameActivityLocation.WRITE_ANSWER);
        timer = new OnlineWritingTimer(TIME_TO_WRITE_ANSWER, this);
        timer.start();
    }

    /** Gets question and prints it on the screen. Sends handshake to server if needed */
    public void onReceivingQuestion(int questionId, @NonNull String question) {
        if (!manager.getNetwork().iAmServer()) {
            manager.getNetwork().sendMessageToServer(HANDSHAKE);
        }
        currentQuestionId = questionId;
        currentQuestionText = question;
        currentQuestionAnswer = null;
        manager.getActivity().onNewQuestion();
        manager.getActivity().setQuestionText(question);
        manager.getActivity().setLocation(GameActivityLocation.SHOW_QUESTION);

        questionReceived = true;
        timeStarted = false;
        alreadyAnswered = false;
    }

    /**
     * Reacts on opponent's incorrect answer.
     * Shows opponent's answer, starts timer on {@code SECOND_COUNTDOWN} seconds
     */
    public void onIncorrectOpponentAnswer(@NonNull String opponentAnswer) {
        manager.getActivity().setTime("");
        manager.getActivity().setOpponentAnswer(opponentAnswer);
        manager.getActivity().setLocation(GameActivityLocation.SHOW_QUESTION);
        startQuestionTime = System.currentTimeMillis();
        timer = new OnlineGameTimer(SECOND_COUNTDOWN, SENDING_COUNTDOWN, 2, this);
        timer.start();
    }

    /** Signalizes server that user haven't pushed the button */
    public void sendTimeLimitedAnswer(int roundNumber) {
        manager.getNetwork().sendMessageToServer(new TimeLimitMessage(roundNumber));
    }

    /** Shows answer and score on the screen, plays sound */
    public void onReceivingAnswer(int firstUserScore,
                                  int secondUserScore,
                                  @NonNull String correctAnswer,
                                  @NonNull String comment,
                                  @NonNull String questionMessage) {
        readyForQuestion = false;
        questionReceived = false;
        timeStarted = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        currentQuestionAnswer = correctAnswer;

        player.play(manager.getActivity(), R.raw.beep);

        if (manager.getNetwork().iAmServer()) {
            manager.getActivity().setScore(String.valueOf(firstUserScore),
                    String.valueOf(secondUserScore));
        } else {
            manager.getActivity().setScore(String.valueOf(secondUserScore),
                    String.valueOf(firstUserScore));
        }

        timer = new OnlineShowingAnswerTimer(TIME_TO_SHOW_ANSWER, this);
        timer.start();

        manager.getActivity().setQuestionResult(questionMessage);
        manager.getActivity().setCommentText(comment);
        manager.getActivity().setAnswerText(correctAnswer);
        manager.getActivity().setLocation(GameActivityLocation.SHOW_ANSWER);
    }

    /** Reacts on opponent's pushing */
    public void onOpponentIsAnswering() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        manager.getActivity().setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
    }

    /**
     * Sends request to server trying to answer
     * Blocked in case of false start and if already answered
     */
    @Override
    public void answerButtonPushed() {
        if (questionReceived && !timeStarted) {
            onFalseStart();
            return;
        }
        if (alreadyAnswered) {
            onForbiddenToAnswer();
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        long time = System.currentTimeMillis() - startQuestionTime;
        manager.getNetwork().sendMessageToServer(new AnswerReadyMessage(time));
    }

    /** Sends written answer to server */
    @Override
    public void answerIsWritten(@NonNull String answer) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        manager.getActivity().setTime("");
        manager.getActivity().setLocation(GameActivityLocation.SHOW_QUESTION);
        manager.getNetwork().sendMessageToServer(new AnswerWrittenMessage(answer));
    }

    /** Finishes user-logic part of game. Cancels all timers */
    public void finishGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        currentQuestionAnswer = null;
        currentQuestionText = null;
        player.finish();
    }

    public CountDownTimer getTimer() {
        return timer;
    }

    public OnlineGameManager getManager() {
        return manager;
    }
}
