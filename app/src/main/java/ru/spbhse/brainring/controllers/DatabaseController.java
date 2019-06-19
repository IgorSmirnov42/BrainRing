package ru.spbhse.brainring.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import ru.spbhse.brainring.database.DatabaseTable;
import ru.spbhse.brainring.database.QuestionDatabase;
import ru.spbhse.brainring.utils.Question;

/** Redirects queries of different classes to stored database */
public class DatabaseController {
    private static final Random RAND = new Random();
    private static QuestionDatabase database;
    private static final Queue<Integer> questionSequence = new LinkedList<>();

    /** Gets random question from database
     *  Random is specified by the {@code questionSequence},
     *  hence generateNewSequence must be called before every new game
     */
    public static Question getRandomQuestion() {
        DatabaseTable gameTable = database.getGameTable();
        return database.getQuestion(gameTable, questionSequence.poll());
    }

    /**
     * Sets stored database to the given database
     *
     * @param database database to store
     */
    public static void setDatabase(QuestionDatabase database) {
        DatabaseController.database = database;
    }

    /**
     * Sets stored gameTable to the given table
     *
     * @param table table to store
     */
    public static void setGameTable(DatabaseTable table) {
        DatabaseController.database.setGameTable(table);
    }

    /** Generates new sequence of non-repeating questions, based on stored gameTable **/
    public static void generateNewSequence() {
        List<Integer> newSequence = new ArrayList<>();
        for (int i = 0; i < database.size(database.getGameTable()); i++) {
            newSequence.add(i);
        }
        Collections.shuffle(newSequence, RAND);

        questionSequence.addAll(newSequence);
    }

    /**
     * Returns number of not yet played questions
     *
     * @return number of not yet played questions
     */
    public static int getNumberOfRemainingQuestions() {
        return questionSequence.size();
    }
}
