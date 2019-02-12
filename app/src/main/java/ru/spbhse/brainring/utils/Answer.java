package ru.spbhse.brainring.utils;

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

    /** Checks if answers are similar. */
    private static boolean compareAnswers(String correctAnswer, String userAnswer) {
        /* TODO: better algorithm to check
            1. Not case-sensitive
            2. Allow some mistakes
         */
        return correctAnswer.equals(userAnswer);
    }

}
