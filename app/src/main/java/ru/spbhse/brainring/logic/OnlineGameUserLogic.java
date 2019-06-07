package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.ui.GameActivityLocation;

/**
 * Realizing user logic in online mode.
 * Controls most of the timers in a game (timer on reading question, timer on writing answer,
 * timer to show answer, timer on thinking)
 * Depends on user actions (pushed buttons) and server commands (answering opponent, new question etc)
 */
public class OnlineGameUserLogic {
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
    private static final int SECOND = 1000;

    private static final byte[] FALSE_START;
    private static final byte[] HANDSHAKE;
    private static final byte[] READY_FOR_QUESTION;

    static {
        HANDSHAKE = MessageGenerator.create()
                .writeInt(Message.HANDSHAKE)
                .toByteArray();
        FALSE_START = MessageGenerator.create()
                .writeInt(Message.FALSE_START)
                .toByteArray();
        READY_FOR_QUESTION = MessageGenerator.create()
                .writeInt(Message.READY_FOR_QUESTION)
                .toByteArray();
    }

    private CountDownTimer timer;

    /** Returns question data in format that is comfortable for complaining */
    public ComplainedQuestion getQuestionData() {
        return new ComplainedQuestion(currentQuestionText,
                currentQuestionAnswer, currentQuestionId);
    }

    /**
     * Reacts on server's forbiddance to answer
     * Called when:
     * 1. User has already answered
     * 2. Other user is answering now
     * 3. User did false start before
     */
    public void onForbiddenToAnswer() {
        Toast.makeText(OnlineController.getOnlineGameActivity(),
                OnlineController.getOnlineGameActivity().getString(R.string.forbidden_answer),
                Toast.LENGTH_SHORT).show();
    }

    /** Reacts on user's false start */
    private void onFalseStart() {
        if (!alreadyAnswered) {
            alreadyAnswered = true;
            OnlineController.NetworkController.sendMessageToServer(FALSE_START);
        }
        Toast.makeText(OnlineController.getOnlineGameActivity(), 
                OnlineController.getOnlineGameActivity().getString(R.string.false_start),
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
        OnlineController.NetworkController.sendMessageToServer(READY_FOR_QUESTION);
    }

    /**
     * Reacts on server's message about time start
     * Plays sound, changes button text, starts new timer on {@code FIRST_COUNTDOWN} seconds
     */
    public void onTimeStart() {
        timeStarted = true;
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(),
                    R.raw.start);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        OnlineController.OnlineUIController.setButtonText(OnlineController.getOnlineGameActivity()
                .getString(R.string.button_push_text));
        startQuestionTime = System.currentTimeMillis();
        timer = new CountDownTimer(FIRST_COUNTDOWN * SECOND,
                SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timer == this) {
                    if (millisUntilFinished <= SENDING_COUNTDOWN * SECOND) {
                        onReceivingTick(millisUntilFinished / SECOND);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (timer == this) {
                    Log.d(Controller.APP_TAG, "Finish first timer");
                    sendTimeLimitedAnswer(1);
                }
            }
        };
        timer.start();
    }

    /**
     * Reacts on tick that means that {@code secondsLeft} time is left.
     * Plays sound, shows time on a screen
     */
    private void onReceivingTick(long secondsLeft) {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(),
                    R.raw.countdown);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        OnlineController.OnlineUIController.setTime(String.valueOf(secondsLeft));
    }

    /**
     * Reacts on server's allowance to answer
     * Starts timer on {@code TIMER_TO_WRITE_ANSWER} seconds
     */
    public void onAllowedToAnswer() {
        alreadyAnswered = true;
        Log.d(Controller.APP_TAG,"Allowed to answer");
        OnlineController.OnlineUIController.setLocation(GameActivityLocation.WRITE_ANSWER);
        timer = new CountDownTimer(TIME_TO_WRITE_ANSWER * SECOND, SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (timer == this) {
                    answerIsWritten(OnlineController.OnlineUIController.getWhatWritten());
                }
            }
        };
        timer.start();
    }

    /** Gets question and prints it on the screen. Sends handshake to server if needed */
    public void onReceivingQuestion(int questionId, @NonNull String question) {
        if (!OnlineController.NetworkController.iAmServer()) {
            OnlineController.NetworkController.sendMessageToServer(HANDSHAKE);
        }
        currentQuestionId = questionId;
        currentQuestionText = question;
        currentQuestionAnswer = null;
        OnlineController.OnlineUIController.onNewQuestion();
        OnlineController.OnlineUIController.setQuestionText(question);
        OnlineController.OnlineUIController.setLocation(GameActivityLocation.SHOW_QUESTION);

        questionReceived = true;
        timeStarted = false;
        alreadyAnswered = false;
    }

    /**
     * Reacts on opponent's incorrect answer.
     * Shows opponent's answer, starts timer on {@code SECOND_COUNTDOWN} seconds
     */
    public void onIncorrectOpponentAnswer(@NonNull String opponentAnswer) {
        OnlineController.OnlineUIController.setTime("");
        OnlineController.OnlineUIController.setOpponentAnswer(opponentAnswer);
        OnlineController.OnlineUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
        startQuestionTime = System.currentTimeMillis();
        timer = new CountDownTimer(SECOND_COUNTDOWN * SECOND,
                SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timer == this) {
                    if (millisUntilFinished <= SENDING_COUNTDOWN * SECOND) {
                        onReceivingTick(millisUntilFinished / SECOND);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (timer == this) {
                    Log.d(Controller.APP_TAG, "Finish second timer");
                    sendTimeLimitedAnswer(2);
                }
            }
        };
        timer.start();
    }

    /** Signalizes server that user haven't pushed the button */
    private void sendTimeLimitedAnswer(int roundNumber) {
        OnlineController.NetworkController.sendMessageToServer(
                MessageGenerator.create()
                        .writeInt(Message.TIME_LIMIT)
                        .writeInt(roundNumber)
                        .toByteArray()
        );
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

        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(),
                    R.raw.beep);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();

        if (OnlineController.NetworkController.iAmServer()) {
            OnlineController.OnlineUIController.setScore(firstUserScore, secondUserScore);
        } else {
            OnlineController.OnlineUIController.setScore(secondUserScore, firstUserScore);
        }

        timer = new CountDownTimer(TIME_TO_SHOW_ANSWER * SECOND,
                TIME_TO_SHOW_ANSWER * SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (timer == this) {
                    readyForQuestion();
                }
            }
        };
        timer.start();

        OnlineController.OnlineUIController.setQuestionResult(questionMessage);
        OnlineController.OnlineUIController.setComment(comment);
        OnlineController.OnlineUIController.setAnswer(correctAnswer);
        OnlineController.OnlineUIController.setLocation(GameActivityLocation.SHOW_ANSWER);
    }

    /** Reacts on opponent's pushing */
    public void onOpponentIsAnswering() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        OnlineController.OnlineUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
    }

    /**
     * Sends request to server trying to answer
     * Blocked in case of false start and if already answered
     */
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
        OnlineController.NetworkController.sendMessageToServer(
                MessageGenerator.create()
                        .writeInt(Message.ANSWER_IS_READY)
                        .writeLong(time)
                        .toByteArray()
        );
    }

    /** Sends written answer to server */
    public void answerIsWritten(@NonNull String answer) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        OnlineController.OnlineUIController.setTime("");
        OnlineController.OnlineUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
        OnlineController.NetworkController.sendMessageToServer(
                MessageGenerator.create()
                        .writeInt(Message.ANSWER_IS_WRITTEN)
                        .writeString(answer)
                        .toByteArray()
        );
    }

    /** Finishes user-logic part of game. Cancels all timers */
    public void finishGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        currentQuestionAnswer = null;
        currentQuestionText = null;
    }
}
