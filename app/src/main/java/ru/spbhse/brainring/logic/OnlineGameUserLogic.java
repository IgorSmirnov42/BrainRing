package ru.spbhse.brainring.logic;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.network.messages.Message;
import ru.spbhse.brainring.ui.GameActivityLocation;

/** Realizing user logic in online mode */
public class OnlineGameUserLogic {
    private UserStatus userStatus;
    private String currentQuestion;

    public OnlineGameUserLogic() {
        userStatus = new UserStatus(Controller.NetworkController.getMyParticipantId());
    }

    /** Reacts on server's forbiddance to answer */
    public void onForbiddenToAnswer() {
        // Возможно сделать тост
    }

    /** Reacts on server's allowance to answer */
    public void onAllowedToAnswer() {
        Controller.NetworkUIController.setLocation(GameActivityLocation.WRITE_ANSWER);
    }

    /** Gets question and prints it on the screen */
    public void onReceivingQuestion(String question) {
        Controller.NetworkUIController.clearEditText();
        userStatus.onNewQuestion();
        currentQuestion = question;
        Controller.NetworkUIController.setQuestionText(question);
        Controller.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
    }

    /** Reacts on opponent's incorrect answer */
    public void onIncorrectOpponentAnswer(String opponentAnswer) {
        userStatus.opponentAnswer = opponentAnswer;
        // TODO: вывод на экран ответа соперника
        Controller.NetworkUIController.setLocation(GameActivityLocation.SHOW_QUESTION);
    }

    /** Shows answer and score (no) on the screen */
    public void onReceivingAnswer(int firstUserScore, int secondUserScore, String correctAnswer) {
        // TODO: вывод на экран счета
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
            onForbiddenToAnswer();
            return;
        }
        Controller.NetworkController.sendMessageToServer(
                Message.generateMessage(Message.ANSWER_IS_READY, ""));
    }

    /** Sends written answer to server */
    public void answerIsWritten(String answer) {
        Controller.NetworkUIController.setLocation(GameActivityLocation.OPPONENT_IS_ANSWERING);
        Controller.NetworkController.sendMessageToServer(Message.generateMessage(
                Message.ANSWER_IS_WRITTEN, answer));
    }
}
