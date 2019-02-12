package ru.spbhse.brainring.logic;

import ru.spbhse.brainring.utils.Question;

public class GameStatus {
    private Question currentQuestion;

    public void setQuestion(Question question) {
        currentQuestion = question;
    }
}
