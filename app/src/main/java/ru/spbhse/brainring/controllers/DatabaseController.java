package ru.spbhse.brainring.controllers;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import ru.spbhse.brainring.database.DatabaseTable;
import ru.spbhse.brainring.database.QuestionDatabase;
import ru.spbhse.brainring.utils.Constants;
import ru.spbhse.brainring.utils.Question;

/** Redirects queries of different classes to stored database */
public class DatabaseController {
    private static final Random RAND = new Random();
    private static final Queue<Integer> questionSequence = new LinkedList<>();

    /** Gets random question from database
     *  Random is specified by the {@code questionSequence},
     *  hence generateNewSequence must be called before every new game
     */
    public static Question getRandomQuestion() {
        QuestionDatabase database = QuestionDatabase.getInstanceUnsafe();
        if (database == null) {
            Log.wtf(Constants.APP_TAG, "Database is null at the point where it shouldn't be");
            throw new IllegalStateException();
        }
        DatabaseTable gameTable = database.getGameTable();
        Log.d(Constants.APP_TAG, "table is " + gameTable.getTableName());
        return database.getQuestion(gameTable, questionSequence.poll());
    }

    /**
     * Sets stored gameTable to the given table
     *
     * @param table table to store
     */
    public static void setGameTable(DatabaseTable table) {
        QuestionDatabase database = QuestionDatabase.getInstanceUnsafe();
        if (database == null) {
            Log.wtf(Constants.APP_TAG, "Database is null at the point where it shouldn't be");
            throw new IllegalStateException();
        }
        database.setGameTable(table);
    }

    /** Generates new sequence of non-repeating questions, based on stored gameTable **/
    public static void generateNewSequence() {
        QuestionDatabase database = QuestionDatabase.getInstanceUnsafe();
        if (database == null) {
            Log.wtf(Constants.APP_TAG, "Database is null at the point where it shouldn't be");
            throw new IllegalStateException();
        }

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
