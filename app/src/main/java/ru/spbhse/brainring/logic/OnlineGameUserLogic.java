package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.ui.GameActivityLocation;

/**
 * Realizing user logic in online mode.
 * Most of timers are placed here
 */
public class OnlineGameUserLogic {
    private String currentQuestionText;
    private int currentQuestionId;
    private String currentQuestionAnswer;
    private long startQuestionTime;
    private boolean questionReceived;
    private boolean readyForQuestion;
    private boolean timeStarted;
    private static final int TIME_TO_SHOW_ANSWER = 10;
    private static final int TIME_TO_WRITE_ANSWER = 20;
    private static final int FIRST_COUNTDOWN = 20;
    private static final int SECOND_COUNTDOWN = 20;
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

    public ComplainedQuestion getQuestionData() {
        return new ComplainedQuestion(currentQuestionText,
                currentQuestionAnswer, currentQuestionId);
    }

    /** Reacts on server's forbiddance to answer (not false start) */
    public void onForbiddenToAnswer() {
        Toast.makeText(OnlineController.getOnlineGameActivity(), "Сервер запретил Вам отвечать",
                Toast.LENGTH_LONG).show();
    }

    private void onFalseStart() {
        Toast.makeText(OnlineController.getOnlineGameActivity(), "Фальстарт!",
                Toast.LENGTH_LONG).show();
        OnlineController.NetworkController.sendMessageToServer(FALSE_START);
    }

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

    public void onTimeStart() {
        timeStarted = true;
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(),
                    R.raw.start);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        OnlineController.OnlineUIController.setButtonText("ЖМЯК!!");
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
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(),
                    R.raw.countdown);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        OnlineController.OnlineUIController.setTime(String.valueOf(secondsLeft));
    }

    /** Reacts on server's allowance to answer */
    public void onAllowedToAnswer() {
        Log.d("BrainRing","Allowed to answer");
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

    /** Gets question and prints it on the screen */
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
    }

    /** Reacts on opponent's incorrect answer */
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
                    Log.d("BrainRing", "Finish second timer");
                    sendTimeLimitedAnswer(2);
                }
            }
        };
        timer.start();
    }

    private void sendTimeLimitedAnswer(int roundNumber) {
        OnlineController.NetworkController.sendMessageToServer(
                MessageGenerator.create()
                        .writeInt(Message.TIME_LIMIT)
                        .writeInt(roundNumber)
                        .toByteArray()
        );
    }

    /** Shows answer and score on the screen */
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

    public void finishGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        currentQuestionAnswer = null;
        currentQuestionText = null;
    }
}
