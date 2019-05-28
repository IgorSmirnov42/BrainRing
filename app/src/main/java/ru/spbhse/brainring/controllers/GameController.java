package ru.spbhse.brainring.controllers;

import ru.spbhse.brainring.files.ComplainedQuestion;

public interface GameController {
    void answerButtonPushed();

    void answerIsWritten(String answer);

    ComplainedQuestion getCurrentQuestionData();
}
