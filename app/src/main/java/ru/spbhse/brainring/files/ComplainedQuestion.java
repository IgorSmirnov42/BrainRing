package ru.spbhse.brainring.files;

import android.support.annotation.NonNull;

public class ComplainedQuestion {
    private static final int SHORTEN_LENGTH = 20;
    @NonNull private final String questionText;
    @NonNull private final String questionAnswer;
    private final int questionId;
    @NonNull private String complainText = "";

    @NonNull
    public String getQuestionAnswer() {
        return questionAnswer;
    }

    public int getQuestionId() {
        return questionId;
    }

    @NonNull
    public String getComplainText() {
        return complainText;
    }

    @NonNull
    public String getQuestionText() {
        return questionText;
    }

    public ComplainedQuestion(@NonNull String questionText,
                              @NonNull String questionAnswer,
                              int questionId) {
        this.questionText = questionText;
        this.questionAnswer = questionAnswer;
        this.questionId = questionId;
    }

    public ComplainedQuestion(@NonNull String questionText,
                              @NonNull String questionAnswer,
                              int questionId,
                              @NonNull String complainText) {
        this.questionText = questionText;
        this.questionAnswer = questionAnswer;
        this.questionId = questionId;
        this.complainText = complainText;
    }

    public void setComplainText(@NonNull String complainText) {
        this.complainText = complainText;
    }

    public String humanReadable() {
        String readableComplain = "";
        readableComplain += "Id вопроса: " + questionId + "\n";
        readableComplain += "Текст вопроса: " + questionText + "\n";
        readableComplain += "Ответ на вопрос: " + questionAnswer + "\n";
        readableComplain += "Жалоба: " + complainText + "\n";
        return readableComplain;
    }

    @NonNull
    private String shortenForm(@NonNull String string) {
        if (string.length() <= SHORTEN_LENGTH) {
            return string;
        }
        return string.substring(0, SHORTEN_LENGTH - 3) + "...";
    }

    /** Used for presenting short information about question on a screen */
    @NonNull
    public String toString() {
        return "Id вопроса: " + questionId + "\n" +
                "Текст вопроса: " + shortenForm(questionText) + "\n" +
                "Ответ на вопрос: " + shortenForm(questionAnswer) + "\n" +
                "Жалоба: " + shortenForm(complainText);
    }
}
