package ru.spbhse.brainring.controllers;

public interface GameController {
    GameController getInstance();

    void answerButtonPushed();

    void answerIsWritten(String answer);
}
