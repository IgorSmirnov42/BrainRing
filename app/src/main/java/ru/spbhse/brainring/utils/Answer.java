package ru.spbhse.brainring.utils;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.Character.toLowerCase;
import static java.lang.Math.min;

/**
 * Class to store correct answers to questions and to check if given answer is right
 */
class Answer {

    final private String[] possibleAnswers;

    String getMainAnswer() {
        return possibleAnswers[0];
    }

    String getAllAnswers() {
        StringBuilder answer = new StringBuilder();
        answer.append(possibleAnswers[0]);
        for (int i = 1; i < possibleAnswers.length; i++) {
            answer.append("/");
            answer.append(possibleAnswers[i]);
        }
        return answer.toString();
    }

    /**
     * Builds Answer
     * @param mainAnswer answer given in field "Answer" of the question from base. Mustn't be null
     * @param validAnswers answers given in field "Valid answers" of the question from base.
     *                     If there are several variants, they must be divided with "/"
     *                     Symbol "/" if forbidden as a part of answer
     */
    Answer(@NonNull String mainAnswer, @Nullable String validAnswers) {
        String[] validAnswersSplit;
        if (validAnswers != null) {
            validAnswersSplit = validAnswers.split("/");
            for (int i = 0; i < validAnswers.length(); i++) {
                validAnswersSplit[i] = validAnswersSplit[i].
                        substring(0, validAnswersSplit[i].length() - 1);
            }
        } else {
            validAnswersSplit = new String[0];
        }

        possibleAnswers = new String[validAnswersSplit.length + 1];

        int currentAnswerCounter = 0;
        possibleAnswers[currentAnswerCounter++] = mainAnswer;
        for (String currentAnswer : validAnswersSplit) {
            possibleAnswers[currentAnswerCounter++] = currentAnswer;
        }
    }

    /** Checks if users answer is right */
    boolean checkAnswer(@NonNull String userAnswer) {
        for (String answer : possibleAnswers) {
            if (canCompare(answer, userAnswer, 0, new StringBuilder())) {
                return true;
            }
        }
        return false;
    }

    private boolean canCompare(@NonNull String answer, @NonNull String userAnswer, int position,
                               @NonNull StringBuilder builder) {
        if (position == answer.length()) {
            return compareAnswers(builder.toString(), userAnswer);
        }
        if (answer.charAt(position) == ']') {
            return canCompare(answer, userAnswer, position + 1, builder);
        }
        boolean result;
        int builderSize = builder.length();
        if (answer.charAt(position) != '[') {
            builder.append(answer.charAt(position));
            result = canCompare(answer, userAnswer, position + 1, builder);
            builder.deleteCharAt(builderSize);
            return result;
        } else {
            if (canCompare(answer, userAnswer, position + 1, builder)) {
                return true;
            } else {
                while (position < answer.length() && answer.charAt(position) != ']') {
                    ++position;
                }
                return canCompare(answer, userAnswer, position, builder);
            }
        }
    }

    private static int allowedDistance(String answer) {
        return answer.length() / 5;
    }

    private static int levenshteinDistance(String correctAnswer, String userAnswer) {
        int[][] distance = new int[correctAnswer.length() + 1][userAnswer.length() + 1];
        for (int row = 0; row <= correctAnswer.length(); row++) {
            distance[row][0] = row;
        }
        for (int column = 0; column <= userAnswer.length(); ++column) {
            distance[0][column] = column;
        }

        final int inf = correctAnswer.length() * userAnswer.length();

        for (int row = 1; row <= correctAnswer.length(); row++) {
            for (int column = 1; column <= userAnswer.length(); column++) {
                char currentCorrectAnswerChar = toLowerCase(correctAnswer.charAt(row - 1));
                char currentUserAnswerChar = toLowerCase(userAnswer.charAt(column - 1));
                distance[row][column] = inf;
                if (currentCorrectAnswerChar == currentUserAnswerChar) {
                    distance[row][column] = distance[row - 1][column - 1];
                }
                distance[row][column] = min(distance[row][column - 1] + 1, distance[row][column]);
                distance[row][column] = min(distance[row - 1][column] + 1, distance[row][column]);
                distance[row][column] = min(distance[row - 1][column - 1] + 1, distance[row][column]);
            }
        }
        return distance[correctAnswer.length()][userAnswer.length()];
    }

    /** Leaves only letters and digits */
    private static String prepareString(@NonNull String string) {
        StringBuilder builder = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (isLetter(c) || isDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /** Checks if answers are similar. */
    private static boolean compareAnswers(@NonNull String correctAnswer, @NonNull String userAnswer) {
        correctAnswer = prepareString(correctAnswer);
        userAnswer = prepareString(userAnswer);
        return levenshteinDistance(correctAnswer, userAnswer) <= allowedDistance(correctAnswer);
    }
}
