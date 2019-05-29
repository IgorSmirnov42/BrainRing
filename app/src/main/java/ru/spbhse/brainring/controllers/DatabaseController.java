package ru.spbhse.brainring.controllers;

import java.util.Random;

import ru.spbhse.brainring.database.QuestionDatabase;
import ru.spbhse.brainring.database.DatabaseTable;
import ru.spbhse.brainring.utils.Question;

public class DatabaseController {
    private static final Random RAND = new Random();
    private static QuestionDatabase database;

    /** Gets random question from database */
    public static Question getRandomQuestion() {
        DatabaseTable gameTable = database.getGameTable();
        database.openDataBase();
        int questionId = RAND.nextInt((int) database.size(gameTable));
        return database.getQuestion(gameTable, questionId);
    }

    public static void setDatabase(QuestionDatabase database) {
        DatabaseController.database = database;
    }

    public static void setGameTable(DatabaseTable table) {
        DatabaseController.database.setGameTable(table);
    }
}
