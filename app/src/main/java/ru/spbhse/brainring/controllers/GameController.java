package ru.spbhse.brainring.controllers;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.files.ComplainedQuestion;

/** Provides interface to control the game, network or training one */
public interface GameController {
    /** Reacts on pushing answer button */
    void answerButtonPushed();

    /** Reacts on writing answer in the text field */
    void answerIsWritten(@NonNull String answer);

    /**
     * Returns current question's data to make a complain
     *
     * @return data of the current played question in a form of {@code ComplainedQuestion}
     */
    @NonNull
    ComplainedQuestion getCurrentQuestionData();
}
