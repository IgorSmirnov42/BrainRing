package ru.spbhse.brainring.logic;

import android.media.MediaPlayer;
import android.widget.Toast;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.R;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.ui.GameActivityLocation;

/** Realizing user logic in online mode */
public class OnlineGameUserLogic {
    private UserStatus userStatus;
    private String currentQuestion;
    private static final byte[] IS_READY = Message.generateMessage(Message.ANSWER_IS_READY, "");

    public OnlineGameUserLogic() {
        userStatus = new UserStatus(Controller.NetworkController.getMyParticipantId());
    }

    /** Reacts on server's forbiddance to answer (not false start) */
    public void onForbiddenToAnswer() {
        Toast.makeText(Controller.getOnlineGameActivity(), "Сервер запретил Вам отвечать",
                Toast.LENGTH_LONG).show();
    }

    public void onFalseStart() {
        Toast.makeText(Controller.getOnlineGameActivity(), "Фальстарт!", Toast.LENGTH_LONG).show();
    }

    public void onTimeStart() {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(Controller.getOnlineGameActivity(), R.raw.start);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        Controller.NetworkUIController.setButtonText("ЖМЯК!!");
    }

    public void onReceivingTick(String secondsLeft) {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(Controller.getOnlineGameActivity(), R.raw.countdown);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        Controller.NetworkUIController.setTime(secondsLeft);
    }

    /** Reacts on server's allowance to answer */
    public void onAllowedToAnswer() {
        Controller.NetworkUIController.setLocation(GameActivityLocation.WRITE_ANSWER);
    }

    /** Gets question and prints it on the screen */
    public void onReceivingQuestion(String question) {
        userStatus.onNewQuestion();
        currentQuestion = question;
        Controller.NetworkUIController.onNewQuestion();
        Controller.NetworkUIController.setQuestionText(question);
        Controller.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
    }

    /** Reacts on opponent's incorrect answer */
    public void onIncorrectOpponentAnswer(String opponentAnswer) {
        userStatus.opponentAnswer = opponentAnswer;
        Controller.NetworkUIController.setTime("");
        Controller.NetworkUIController.setOpponentAnswer(opponentAnswer);
        Controller.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
    }

    /** Shows answer and score (no) on the screen */
    public void onReceivingAnswer(int firstUserScore, int secondUserScore, String correctAnswer) {
        new Thread(() -> {
            MediaPlayer player = MediaPlayer.create(Controller.getOnlineGameActivity(), R.raw.beep);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        }).start();
        if (Controller.NetworkController.iAmServer()) {
            Controller.NetworkUIController.setScore(firstUserScore, secondUserScore);
        } else {
            Controller.NetworkUIController.setScore(secondUserScore, firstUserScore);
        }
        Controller.NetworkUIController.setAnswer(correctAnswer);
        Controller.NetworkUIController.setLocation(GameActivityLocation.SHOW_ANSWER);
    }

    /** Reacts on opponent's pushing */
    public void onOpponentIsAnswering() {
        Controller.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
    }

    /** Sends request to server trying to answer */
    public void answerButtonPushed() {
        if (userStatus.alreadyAnswered) {
            // Вообще, кнопка пользователю недоступна, но мало ли...
            onForbiddenToAnswer();
            return;
        }
        Controller.NetworkController.sendMessageToServer(IS_READY);
    }

    public void onTimeToWriteAnswerIsOut() {
        Toast.makeText(Controller.getOnlineGameActivity(), "Время на ввод ответа истекло",
                Toast.LENGTH_LONG).show();
        Controller.NetworkUIController.hideKeyboard();
        Controller.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
    }

    /** Sends written answer to server */
    public void answerIsWritten(String answer) {
        Controller.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
        Controller.NetworkController.sendMessageToServer(Message.generateMessage(
                Message.ANSWER_IS_WRITTEN, answer));
    }
}
