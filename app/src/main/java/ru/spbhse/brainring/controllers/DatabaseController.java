package ru.spbhse.brainring.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ru.spbhse.brainring.database.DatabaseTable;
import ru.spbhse.brainring.database.QuestionDatabase;
import ru.spbhse.brainring.utils.Question;

public class DatabaseController {
    private static final Random RAND = new Random();
    private static QuestionDatabase database;
    private static List<Integer> questionSequence;
    private static int currentQuestion = 0;

    /** Gets random question from database */
    public static Question getRandomQuestion() {
        DatabaseTable gameTable = database.getGameTable();
        database.openDataBase();
        return database.getQuestion(gameTable, questionSequence.get(currentQuestion++));
    }

    public static void setDatabase(QuestionDatabase database) {
        DatabaseController.database = database;
    }

    public static void setGameTable(DatabaseTable table) {
        DatabaseController.database.setGameTable(table);
    }

    public static void generateNewSequence() {
        questionSequence = new ArrayList<>();
        for (int i = 0; i < database.size(database.getGameTable()); i++)
            questionSequence.add(i);
        Collections.shuffle(questionSequence, RAND);
        currentQuestion = 0;
    }
}
