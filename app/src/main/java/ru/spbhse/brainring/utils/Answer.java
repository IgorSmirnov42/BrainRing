package ru.spbhse.brainring.utils;


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
    Answer(String mainAnswer, String validAnswers) {
        if (mainAnswer == null) {
            throw new IllegalArgumentException("Answer constructor was given null as mainAnswer. It is forbidden.");
        }

        String[] validAnswersSplitted;
        if (validAnswers != null) {
            validAnswersSplitted = validAnswers.split("/");
        } else {
            validAnswersSplitted = new String[0];
        }

        possibleAnswers = new String[validAnswersSplitted.length + 1];

        int currentAnswerCounter = 0;
        possibleAnswers[currentAnswerCounter++] = mainAnswer;
        for (String currentAnswer : validAnswersSplitted) {
            possibleAnswers[currentAnswerCounter++] = currentAnswer;
        }
    }

    /** Checks if users answer is right */
    boolean checkAnswer(String userAnswer) {
        if (userAnswer == null) {
            return false;
        }
        for (String answer : possibleAnswers) {
            if (compareAnswers(answer, userAnswer)) {
                return true;
            }
        }
        return false;
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

    /** Checks if answers are similar. */
    private static boolean compareAnswers(String correctAnswer, String userAnswer) {
        return levenshteinDistance(correctAnswer, userAnswer) <= allowedDistance(correctAnswer);
    }
}
