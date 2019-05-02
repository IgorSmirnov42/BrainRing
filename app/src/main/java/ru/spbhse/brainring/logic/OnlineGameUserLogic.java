package ru.spbhse.brainring.logic;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.ui.GameActivityLocation;

public class OnlineGameUserLogic {
    public UserStatus userStatus;
    public String currentQuestion;

    public OnlineGameUserLogic() {
        userStatus = new UserStatus(Controller.NetworkController.getMyParticipantId());
    }

    public void onForbiddenToAnswer() {
        // Возможно сделать тост
    }

    public void onAllowedToAnswer() {
        Controller.NetworkUIController.setLocation(GameActivityLocation.WRITE_ANSWER);
    }

    public void onReceivingQuestion(String question) {
        Controller.NetworkUIController.clearEditText();
        userStatus.onNewQuestion();
        currentQuestion = question;
        Controller.NetworkUIController.setQuestionText(question);
        Controller.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
    }

    public void onIncorrectOpponentAnswer(String opponentAnswer) {
        userStatus.opponentAnswer = opponentAnswer;
        // TODO: вывод на экран ответа соперника
        Controller.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
    }

    public void onReceivingAnswer(int firstUserScore, int secondUserScore, String correctAnswer) {
        // TODO: вывод на экран счета
        Controller.NetworkUIController.setAnswer(correctAnswer);
        Controller.NetworkUIController.setLocation(GameActivityLocation.SHOW_ANSWER);
    }

    public void onOpponentIsAnswering() {
        Controller.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
    }

    // функция, которую должен вызывать UI при нажатии на кнопку в layout 2a
    public void answerButtonPushed() {
        if (userStatus.alreadyAnswered) {
            onForbiddenToAnswer();
            return;
        }
        byte[] message;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeInt(Message.ANSWER_IS_READY);
            message = bout.toByteArray();
        } catch (IOException e) {
            message = null;
            e.printStackTrace();
        }
        System.out.println("SEND ANSWER BUTTON PUSHED");
        Controller.NetworkController.sendMessageToServer(message);
    }

    // функция, которую должен вызывать UI при нажатии на кнопку в layout 2b
    // answer -- введенный текст
    public void answerIsWritten(String answer) {
        byte[] message;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeInt(Message.ANSWER_IS_WRITTEN);
            dout.writeChars(answer);
            dout.flush();
            message = bout.toByteArray();
        } catch (IOException e) {
            message = null;
            e.printStackTrace();
        }
        Controller.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
        Controller.NetworkController.sendMessageToServer(message);
    }
}
