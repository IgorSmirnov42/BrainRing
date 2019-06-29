package ru.spbhse.brainring.logic;

import android.support.annotation.NonNull;

import ru.spbhse.brainring.files.ComplainedQuestion;

public interface PlayerLogic {
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
