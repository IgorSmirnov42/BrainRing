package ru.spbhse.brainring.utils;

/** Class to store questions. Allows to check if user's answer is right */
public class Question {
    final private String question;
    final private Answer answer;
    final private String comment;

    public String getQuestion() {
        return question;
    }

    public String getMainAnswer() {
        return answer.getMainAnswer();
    }

    public String getComment() {
        return comment;
    }

    /**
     * Constructs Question
     * @param question question as String. Mustn't be null
     * @param mainAnswer answer given in field "Answer" of the question from base. Mustn't be null
     * @param validAnswers answers given in field "Valid answers" of the question from base.
     *                     If there are several variants, they must be divided with "/"
     *                     Symbol "/" if forbidden as a part of answer. May be null
     * @param comment comment to answer. May be null
     */
    public Question(String question, String mainAnswer, String validAnswers, String comment) {
        if (question == null) {
            throw new IllegalArgumentException("Question constructor was given null as question. It is forbidden.");
        }

        this.question = question;
        this.answer = new Answer(mainAnswer, validAnswers);
        this.comment = comment == null ? "" : comment;
    }

    /** Checks if users answer is right */
    boolean checkAnswer(String userAnswer) {
        return answer.checkAnswer(userAnswer);
    }
}
