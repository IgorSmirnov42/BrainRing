package ru.spbhse.brainring.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import ru.spbhse.brainring.utils.Question;

public class QuestionDataBase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Questions.db";
    private static final String DATABASE_PATH = "/data/data/ru.spbhse.brainring/databases/";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " +
                    Entry.TABLE_NAME + "(" +
                    Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Entry.COLUMN_QUESTION + " TEXT," +
                    Entry.COLUMN_ANSWER + " TEXT)";
    private static final String DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Entry.TABLE_NAME;
    private SQLiteDatabase db;


    public QuestionDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void createDatabase() {
        db = this.getWritableDatabase();
        db.execSQL(CREATE_ENTRIES);
        try {
            Document doc = Jsoup.connect("https://db.chgk.info/tour/120br").get();
            Elements elems = doc.select("div.question");
            ArrayList<String> questions = new ArrayList<>();
            ArrayList<String> answers = new ArrayList<>();

            for (Element element : elems.select("p:has(strong.Question)")) {
                questions.add(element.text());
            }

            for(Element element : elems.select("p:has(strong.Answer)")) {
                answers.add(element.text());
            }

            for (int i = 0; i < questions.size(); i++) {
                db.execSQL("INSERT INTO " + Entry.TABLE_NAME +
                        " (" + Entry.COLUMN_QUESTION + ", " + Entry.COLUMN_ANSWER + ")" +
                        " VALUES(\"" + questions.get(i).replace("\"", "") + "\", \"" +
                        answers.get(i).replace("\"", "") + "\")");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openDataBase() {
        if (!checkDataBase()) {
            createDatabase();
        }
    }

    public long size() {
        db = this.getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, Entry.TABLE_NAME);
    }

    public Question getQuestion(int id) {
        db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Entry.TABLE_NAME + " WHERE " + Entry._ID + "=" + id + ";";
        Cursor cursor = db.rawQuery(query, null);
        String question = "";
        String answer = "";
        if (cursor.moveToFirst()) {
            do {
                question = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_QUESTION)).
                        replaceAll("Вопрос [0-9]*:", "");
                answer = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_ANSWER)).
                        replaceAll("Ответ:", "");
            } while (cursor.moveToNext());
        }
        return new Question(question, answer, "", "");
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String path = DATABASE_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException ignored) {

        }

        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    private static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "questions";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_ANSWER = "answer";
    }
}
