package ru.spbhse.brainring;

import ru.spbhse.brainring.utils.Question;

public class Controller {

    /* layout 1 -- layout с кнопкой "Начать онлайн игру"
     layout 2 -- layout с 3 локациями
     a) Во время вопроса. Это поле с текстом (желательно с прокруткой,
        но не критично, если пока что будет без) и кнопка.
     b) Ответ. Поле для ввода и кнопка.
     c) Результат. Просто поле с текстом
    */

    // функция, которую должен вызывать UI при нажатии на кнопку в layout 1
    public static void createOnlineGame() {
        // реализацию писать не надо
    }

    // функция, которую дергают извне, чтобы переключиться из layout 1 в layout 2
    public static void onlineGameCreated() {
        // Серёжа, напиши реализацию
    }

    // функция, которую дергают, чтобы отобразить текст вопроса в layout 2a
    public static void setQuestionText(String question) {
        // Серёжа, напиши реализацию
        // Тут тебе скорее всего понадобится static поле у контроллера, хранящее ссылку на ui
    }

    // функция, которую должен вызывать UI при нажатии на кнопку в layout 2a
    public static void answerButtonPushed() {
        // Реализацию писать не надо
    }

    // меняет локацию ui на соответствующую номеру locationId
    public static void setLocation(int locationId) {
        // Серёжа, напиши реализацию
    }

    // функция, которую должен вызывать UI при нажатии на кнопку в layout 2b
    // answer -- введенный текст
    public static void answerIsWritten(String answer) {
        // Реализацию писать не надо
    }

    // функция, которую дергают, чтобы отобразить текст ответа в layout 2c
    public static void setAnswer(String answer) {
        // Серёжа, напиши реализацию
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
