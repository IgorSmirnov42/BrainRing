package ru.spbhse.brainring;

import ru.spbhse.brainring.utils.Question;

public class Controller {

    /** Gets question from database */
    public static Question getQuestion() {
        // TODO : get question from database
        return new Question("aa", "bb", "00", "gg");
    }

    public static void startLocalGameAsAdmin() {
        // TODO
        // Somehow creates game
    }

    /** Shows phase using UI */
    public static void showPhase(/* Phase somehow coded */) {
        // TODOs
    }
}
