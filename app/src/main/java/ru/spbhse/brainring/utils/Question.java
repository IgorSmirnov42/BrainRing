package ru.spbhse.brainring.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/** Class to store questions. Allows to check if user's answer is right */
public class Question {
    @NonNull final private String question;
    @NonNull final private Answer answer;
    @Nullable final private String comment;
    final private int id;

    @NonNull
    public String getQuestion() {
        return question;
    }

    @NonNull
    public String getAllAnswers() {
        return answer.getAllAnswers();
    }

    @NonNull
    public String getMainAnswer() {
        return answer.getMainAnswer();
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    public int getId() {
        return id;
    }

    /**
     * Constructs Question
     * @param question question as String. Mustn't be null
     * @param mainAnswer answer given in field "Answer" of the question from base. Mustn't be null
     * @param validAnswers answers given in field "Valid answers" of the question from base.
 *                     If there are several variants, they must be divided with "/"
 *                     Symbol "/" if forbidden as a part of answer. May be null
     * @param comment comment to answer. May be null
     * @param id id of the question in the database containing this question
     */
    public Question(@NonNull String question, @NonNull String mainAnswer,
                    @Nullable String validAnswers, @Nullable String comment, int id) {
        this.question = question;
        this.answer = new Answer(mainAnswer, validAnswers);
        this.comment = comment == null ? "" : comment;
        this.id = id;
    }

    /** Checks if users answer is right */
    public boolean checkAnswer(String userAnswer) {
        return answer.checkAnswer(userAnswer);
    }
}
