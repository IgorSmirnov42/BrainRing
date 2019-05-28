package ru.spbhse.brainring.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.net.URL;

import ru.spbhse.brainring.utils.Question;
import ru.spbhse.brainring.utils.DataBaseTableEntry;

public class QuestionDataBase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Questions.db";
    private static final String DATABASE_PATH = "/data/data/ru.spbhse.brainring/databases/";
    private static final int DATABASE_VERSION = 1;
    private DataBaseTableEntry baseTable;

    private SQLiteDatabase db;


    public QuestionDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        try {
            baseTable = new DataBaseTableEntry(new URL("https://db.chgk.info/tour/120br"));
            Log.d("BrainRing", "created basetable with name" + baseTable.getTableName());
        } catch (MalformedURLException e) {
            Log.wtf("BrainRing", e.getMessage());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void createDatabase() {
        db = this.getWritableDatabase();
    }

    public void createTable(DataBaseTableEntry table) {
        db = this.getWritableDatabase();
        if (table == null) {
            table = getBaseTable();
        }

        if (alreadyExists(table)) {
            Log.d("BrainRing","Check successful");
            return;
        }
        db.execSQL(createEntries(table));
        try {
            Document doc = Jsoup.connect(table.getURL()).get();
            Elements elements = doc.select("div.question");
            ArrayList<String> questions = new ArrayList<>();
            ArrayList<String> answers = new ArrayList<>();

            for (Element element : elements.select("p:has(strong.Question)")) {
                questions.add(element.text());
            }

            for (Element element : elements.select("p:has(strong.Answer)")) {
                answers.add(element.text());
            }

            for (int i = 0; i < questions.size(); i++) {
                db.execSQL("INSERT INTO " + table.getTableName() +
                        " (" + DataBaseTableEntry.COLUMN_QUESTION + ", " + DataBaseTableEntry.COLUMN_ANSWER + ")" +
                        " VALUES(\"" + questions.get(i).replace("\"", "") + "\", \"" +
                        answers.get(i).replace("\"", "") + "\")");
            }

        } catch (IOException e) {
            Log.wtf("BrainRing", "Error occurred while connecting to the url " + table.getURL());
        }
    }

    private boolean alreadyExists(DataBaseTableEntry table) {
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = "
                + table.getTableName(), null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public String createEntries(DataBaseTableEntry table) {
        return "CREATE TABLE IF NOT EXISTS " +
                table.getTableName() + "(" +
                DataBaseTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DataBaseTableEntry.COLUMN_QUESTION + " TEXT," +
                DataBaseTableEntry.COLUMN_ANSWER + " TEXT)";
    }

    public String deleteEntries(DataBaseTableEntry table) {
        return "DROP TABLE IF EXISTS " + table.getTableName();
    }

    public void openDataBase() {
        if (!checkDataBase()) {
            createDatabase();
        }
    }

    public long size(DataBaseTableEntry table) {
        db = this.getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, table.getTableName());
    }

    public Question getQuestion(DataBaseTableEntry table, int id) {
        db = this.getReadableDatabase();
        String query = "SELECT * FROM " + table.getTableName() + " WHERE " + DataBaseTableEntry._ID + "=" + id + ";";
        Cursor cursor = db.rawQuery(query, null);
        String question = "";
        String answer = "";
        if (cursor.moveToFirst()) {
            do {
                question = cursor.getString(cursor.getColumnIndex(DataBaseTableEntry.COLUMN_QUESTION)).
                        replaceAll("Вопрос [0-9]*:", "");
                answer = cursor.getString(cursor.getColumnIndex(DataBaseTableEntry.COLUMN_ANSWER)).
                        replaceAll("Ответ:", "");
            } while (cursor.moveToNext());
        }
        return new Question(question, answer, null, "", id);
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

    public boolean loadPackage(String url) {
        try {
            URL packageUrl = new URL(url);
            DataBaseTableEntry tableEntry = new DataBaseTableEntry(packageUrl);
            if (tableEntry.getTableName().equals("")) {
                throw new FileNotFoundException("Error occurred while parsing the URL");
            }
            createTable(tableEntry);
        } catch (Exception e) {
            Log.wtf("BrainRing", e.getMessage());
            return false;
        }
        return true;
    }

    public DataBaseTableEntry getBaseTable() {
        return baseTable;
    }

    public void setBaseTable(DataBaseTableEntry baseTable) {
        this.baseTable = baseTable;
    }
}
