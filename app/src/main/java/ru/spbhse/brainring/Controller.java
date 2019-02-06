package ru.spbhse.brainring;

import ru.spbhse.brainring.ui.GameActivity;
import ru.spbhse.brainring.utils.Question;

public class Controller {

    private static GameActivity gameActivity;

    /* layout 1 -- layout с кнопкой "Начать онлайн игру"
     layout 2 -- layout с 3 локациями
     a) Во время вопроса. Это поле с текстом (желательно с прокруткой,
        но не критично, если пока что будет без) и кнопка.
     b) Ответ. Поле для ввода и кнопка.
     c) Результат. Просто поле с текстом
    */

    public static void setUI(GameActivity ui) {
        gameActivity = ui;
    }

    // функция, которую должен вызывать UI при нажатии на кнопку в layout 1
    public static void createOnlineGame() {
        // реализацию писать не надо
    }

    // функция, которую дергают извне, чтобы начать игру
    public static void onlineGameCreated() {
        gameActivity.gameCreated();
    }

    // функция, которую дергают, чтобы отобразить текст вопроса в layout 2a
    public static void setQuestionText(String question) {
        gameActivity.setQuestionText(question);
    }

    // функция, которую должен вызывать UI при нажатии на кнопку в layout 2a
    public static void answerButtonPushed() {
        // Реализацию писать не надо
    }

    // меняет локацию ui на соответствующую номеру locationId
    public static void setLocation(int locationId) {
        gameActivity.setLocation(locationId);
    }

    // функция, которую должен вызывать UI при нажатии на кнопку в layout 2b
    // answer -- введенный текст
    public static void answerIsWritten(String answer) {
        // Реализацию писать не надо
    }

    // функция, которую дергают, чтобы отобразить текст ответа в layout 2c
    public static void setAnswer(String answer) {
        gameActivity.setAnswer(answer);
    }

    /** Gets question from database */
    public static Question getQuestion() {
        // TODO : get question from database
        return new Question("aa", "bb", "00", "gg");
    }

    public static void startLocalGameAsAdmin() {
        // TODO
        // Somehow creates game
    }

    /** Shows phase using UI */
    public static void showPhase(/* Phase somehow coded */) {
        // TODOs
    }
}
