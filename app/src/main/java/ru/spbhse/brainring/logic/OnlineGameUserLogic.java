package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.widget.Toast;

import ru.spbhse.brainring.controllers.OnlineController;
import ru.spbhse.brainring.R;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.ui.GameActivityLocation;

/** Realizing user logic in online mode */
public class OnlineGameUserLogic {
    private UserStatus userStatus;
    private String currentQuestion;
    private static final byte[] IS_READY = Message.generateMessage(Message.ANSWER_IS_READY, "");
    private static final byte[] HANDSHAKE = Message.generateMessage(Message.HANDSHAKE, "");

    public OnlineGameUserLogic() {
        userStatus = new UserStatus(OnlineController.NetworkController.getMyParticipantId());
    }

    /** Reacts on server's forbiddance to answer (not false start) */
    public void onForbiddenToAnswer() {
        Toast.makeText(OnlineController.getOnlineGameActivity(), "Сервер запретил Вам отвечать",
                Toast.LENGTH_LONG).show();
    }

    public void onFalseStart() {
        Toast.makeText(OnlineController.getOnlineGameActivity(), "Фальстарт!", Toast.LENGTH_LONG).show();
    }

    public void onTimeStart() {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(), R.raw.start);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        OnlineController.NetworkUIController.setButtonText("ЖМЯК!!");
    }

    public void onReceivingTick(String secondsLeft) {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(OnlineController.getOnlineGameActivity(), R.raw.countdown);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        OnlineController.NetworkUIController.setTime(secondsLeft);
    }

    /** Reacts on server's allowance to answer */
    public void onAllowedToAnswer() {
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.WRITE_ANSWER);
    }

    /** Gets question and prints it on the screen */
    public void onReceivingQuestion(String question) {
        userStatus.onNewQuestion();
        currentQuestion = question;
        OnlineController.NetworkUIController.onNewQuestion();
        OnlineController.NetworkUIController.setQuestionText(question);
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
        if (!OnlineController.NetworkController.iAmServer()) {
            OnlineController.NetworkController.sendMessageToServer(HANDSHAKE);
        }
    }

    /** Reacts on opponent's incorrect answer */
    public void onIncorrectOpponentAnswer(String opponentAnswer) {
        userStatus.opponentAnswer = opponentAnswer;
        OnlineController.NetworkUIController.setTime("");
        OnlineController.NetworkUIController.setOpponentAnswer(opponentAnswer);
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
    }

    /** Shows answer and score (no) on the screen */
    public void onReceivingAnswer(int firstUserScore, int secondUserScore, String correctAnswer) {
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
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
    }

    /** Sends request to server trying to answer */
    public void answerButtonPushed() {
        if (userStatus.alreadyAnswered) {
            // Вообще, кнопка пользователю недоступна, но мало ли...
            onForbiddenToAnswer();
            return;
        }
        OnlineController.NetworkController.sendMessageToServer(IS_READY);
    }

    public void onTimeToWriteAnswerIsOut() {
        Toast.makeText(OnlineController.getOnlineGameActivity(), "Время на ввод ответа истекло",
                Toast.LENGTH_LONG).show();
        OnlineController.NetworkUIController.hideKeyboard();
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
    }

    /** Sends written answer to server */
    public void answerIsWritten(String answer) {
        OnlineController.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
        OnlineController.NetworkController.sendMessageToServer(Message.generateMessage(
                Message.ANSWER_IS_WRITTEN, answer));
    }
}
