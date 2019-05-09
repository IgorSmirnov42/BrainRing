package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.ui.GameActivityLocation;

/** Realizing user logic in online mode */
public class OnlineGameUserLogic {
    private long startQuestionTime;
    private boolean questionReceived;
    private boolean timeStarted;
    private static final byte[] HANDSHAKE = Message.generateMessage(Message.HANDSHAKE, "");
    private static final int TIME_TO_WRITE_ANSWER = 20;
    private static final int FIRST_COUNTDOWN = 20;
    private static final int SECOND_COUNTDOWN = 20;
    private static final int SENDING_COUNTDOWN = 5;
    private static final int SECOND = 1000;
    private static final byte[] FALSE_START = Message.generateMessage(Message.FALSE_START, "");

    private CountDownTimer timer;

    /** Reacts on server's forbiddance to answer (not false start) */
    public void onForbiddenToAnswer() {
        Toast.makeText(OnlineController.getOnlineGameActivity(), "Сервер запретил Вам отвечать",
                Toast.LENGTH_LONG).show();
    }

    private void onFalseStart() {
        Toast.makeText(OnlineController.getOnlineGameActivity(), "Фальстарт!", Toast.LENGTH_LONG).show();
        OnlineController.NetworkController.sendReliableMessageToServer(FALSE_START);
    }

    public void onTimeStart() {
        timeStarted = true;
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(), R.raw.start);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        OnlineController.NetworkUIController.setButtonText("ЖМЯК!!");
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
                    Log.d("BrainRing", "Finish first timer");
                    sendTimeLimitedAnswer(1);
                }
            }
        };
        timer.start();
    }

    private void onReceivingTick(long secondsLeft) {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(), R.raw.countdown);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        OnlineController.NetworkUIController.setTime(String.valueOf(secondsLeft));
    }

    /** Reacts on server's allowance to answer */
    public void onAllowedToAnswer() {
        Log.d("BrainRing","Allowed to answer");
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.WRITE_ANSWER);
        timer = new CountDownTimer(TIME_TO_WRITE_ANSWER * SECOND, SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timer == this) {
                    // TODO : отображать время
                }
            }

            @Override
            public void onFinish() {
                if (timer == this) {
                    answerIsWritten(OnlineController.NetworkUIController.getWhatWritten());
                }
            }
        };
        timer.start();
    }

    /** Gets question and prints it on the screen */
    public void onReceivingQuestion(String question) {
        OnlineController.NetworkUIController.onNewQuestion();
        OnlineController.NetworkUIController.setQuestionText(question);
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
        if (!OnlineController.NetworkController.iAmServer()) {
            OnlineController.NetworkController.sendReliableMessageToServer(HANDSHAKE);
        }

        questionReceived = true;
        timeStarted = false;
    }

    /** Reacts on opponent's incorrect answer */
    public void onIncorrectOpponentAnswer(String opponentAnswer) {
        OnlineController.NetworkUIController.setTime("");
        OnlineController.NetworkUIController.setOpponentAnswer(opponentAnswer);
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
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
                    Log.d("BrainRing", "Finish second timer");
                    sendTimeLimitedAnswer(2);
                }
            }
        };
        timer.start();
    }

    private void sendTimeLimitedAnswer(int roundNumber) {
        OnlineController.NetworkController.sendReliableMessageToServer(
                Message.generateMessageLongBody(Message.TIME_LIMIT, roundNumber));
    }

    /** Shows answer and score (no) on the screen */
    public void onReceivingAnswer(int firstUserScore, int secondUserScore, String correctAnswer) {
        questionReceived = false;
        timeStarted = false;
        timer = null;

        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(), R.raw.beep);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();

        if (OnlineController.NetworkController.iAmServer()) {
            OnlineController.NetworkUIController.setScore(firstUserScore, secondUserScore);
        } else {
            OnlineController.NetworkUIController.setScore(secondUserScore, firstUserScore);
        }

        OnlineController.NetworkUIController.setAnswer(correctAnswer);
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.SHOW_ANSWER);
    }

    /** Reacts on opponent's pushing */
    public void onOpponentIsAnswering() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
    }

    /** Sends request to server trying to answer */
    public void answerButtonPushed() {
        if (questionReceived && !timeStarted) {
            onFalseStart();
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        long time = System.currentTimeMillis() - startQuestionTime;
        OnlineController.NetworkController.sendReliableMessageToServer(
                Message.generateMessageLongBody(Message.ANSWER_IS_READY, time));
    }

    /** Sends written answer to server */
    public void answerIsWritten(String answer) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
        OnlineController.NetworkController.sendReliableMessageToServer(Message.generateMessage(
                Message.ANSWER_IS_WRITTEN, answer));
    }

    public void finishGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
