package ru.spbhse.brainring.controllers;

import java.util.Random;

import ru.spbhse.brainring.database.QuestionDataBase;
import ru.spbhse.brainring.utils.DataBaseTableEntry;
import ru.spbhse.brainring.utils.Question;

public class DatabaseController {
    private static final Random RAND = new Random();
    private static QuestionDataBase database;

    /** Gets random question from database */
    public static Question getRandomQuestion() {
        DataBaseTableEntry baseTable = database.getBaseTable();
        database.openDataBase();
        int questionId = RAND.nextInt((int) database.size(baseTable));
        return database.getQuestion(baseTable, questionId);
    }

    public static void setDatabase(QuestionDataBase database) {
        DatabaseController.database = database;
    }
}
