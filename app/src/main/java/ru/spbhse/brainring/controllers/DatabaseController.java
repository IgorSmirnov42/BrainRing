package ru.spbhse.brainring.controllers;

import java.util.Random;

import ru.spbhse.brainring.database.QuestionDataBase;
import ru.spbhse.brainring.utils.Question;

public class DatabaseController {
    private static final Random RAND = new Random();
    /** Gets random question from database */
    public static Question getRandomQuestion() {
        QuestionDataBase dataBase = OnlineController.onlineGameActivity.get().dataBase;
        if (dataBase == null) {
            dataBase = new QuestionDataBase(OnlineController.onlineGameActivity.get());
        }
        dataBase.openDataBase();
        int questionId = RAND.nextInt((int) dataBase.size());
        return dataBase.getQuestion(questionId);
    }
}
