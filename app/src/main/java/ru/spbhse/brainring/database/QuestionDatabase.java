package ru.spbhse.brainring.database;

import android.content.ContentValues;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.net.URL;
import java.util.List;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.utils.Question;

public class QuestionDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Questions.db";
    private final String DATABASE_PATH;
    private static final int DATABASE_VERSION = 1;
    private DatabaseTable baseTable;
    private DatabaseTable gameTable;

    private SQLiteDatabase db;


    public QuestionDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        baseTable = new DatabaseTable("baseTable");
        DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).getPath();
        Log.d("BrainRing", "created basetable with name" + baseTable.getTableName());
        createDatabase();
        if (!alreadyExists(baseTable)) {
            InputStream in = context.getResources().openRawResource(R.raw.questions);
            try {
                Document doc = Jsoup.parse(in, "UTF-8", "");
                db.execSQL(createEntries(baseTable));
                loadQuestions(doc, baseTable);
            } catch (IOException e) {
                Log.wtf("BrainRing", "failed to parse questions");
            }
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

    public void createTable(DatabaseTable table) {
        db = this.getWritableDatabase();
        if (table == null) {
            table = getBaseTable();
        }

        if (alreadyExists(table)) {
            Log.d("BrainRing", "Check successful");
            return;
        }
        db.execSQL(createEntries(table));
        try {
            Document doc = Jsoup.connect(table.getURL()).get();
            loadQuestions(doc, table);
        } catch (IOException e) {
            Log.wtf("BrainRing", "Error occurred while connecting to the url " + table.getURL());
        }
    }

    private boolean alreadyExists(DatabaseTable table) {
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

    public String createEntries(DatabaseTable table) {
        return "CREATE TABLE IF NOT EXISTS " +
                table.getTableName() + "(" +
                DatabaseTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DatabaseTable.COLUMN_QUESTION + " TEXT," +
                DatabaseTable.COLUMN_ANSWER + " TEXT," +
                DatabaseTable.COLUMN_COMMENT + " TEXT)";
    }

    public void deleteEntries(DatabaseTable table) {
        db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + table.getTableName());
    }

    public void openDataBase() {
        if (!checkDataBase()) {
            createDatabase();
        }
    }

    public long size(DatabaseTable table) {
        db = this.getReadableDatabase();

        return DatabaseUtils.queryNumEntries(db, table.getTableName());
    }

    public Question getQuestion(DatabaseTable table, int id) {
        db = this.getReadableDatabase();
        String query = "SELECT * FROM " + table.getTableName() + " WHERE " + DatabaseTable._ID + "=" + (id + 1) + ";";
        Cursor cursor = db.rawQuery(query, null);
        String question = "";
        String answer = "";
        String comment = "";
        if (cursor.moveToFirst()) {
            do {
                question = cursor.getString(cursor.getColumnIndex(DatabaseTable.COLUMN_QUESTION)).
                        replaceAll("Вопрос [0-9]*:", "");
                answer = cursor.getString(cursor.getColumnIndex(DatabaseTable.COLUMN_ANSWER)).
                        replaceAll("Ответ:", "");
                comment = cursor.getString(cursor.getColumnIndex(DatabaseTable.COLUMN_COMMENT));
            } while (cursor.moveToNext());
        }
        return new Question(question, answer, null, comment, id);
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);
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
            DatabaseTable tableEntry = new DatabaseTable(packageUrl);
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

    public DatabaseTable getBaseTable() {
        return baseTable;
    }

    public void setBaseTable(DatabaseTable baseTable) {
        this.baseTable = baseTable;
    }

    private void loadQuestions(Document doc, DatabaseTable table) {
        db = this.getWritableDatabase();
        Elements elements = doc.select("div.question");
        ArrayList<String> questions = new ArrayList<>();
        ArrayList<String> answers = new ArrayList<>();
        ArrayList<String> comments = new ArrayList<>();

        for (Element element : elements) {
            questions.addAll(element.select("p:has(strong.Question)").eachText());
            answers.addAll(element.select("p:has(strong.Answer)").eachText());
            List<String> tryComments = element.select("p:has(strong.Comments)").eachText();
            if (tryComments.isEmpty()) {
                comments.add("");
            } else {
                comments.addAll(tryComments);
            }
        }

        for (int i = 0; i < questions.size(); i++) {
            ContentValues row = new ContentValues();
            row.put(DatabaseTable.COLUMN_QUESTION, questions.get(i));
            row.put(DatabaseTable.COLUMN_ANSWER, answers.get(i));
            row.put(DatabaseTable.COLUMN_COMMENT, comments.get(i));
            db.insert(table.getTableName(), null, row);
        }
    }

    public void setGameTable(DatabaseTable table) {
        gameTable = table;
    }

    public DatabaseTable getGameTable() {
        if (gameTable == null) {
            return baseTable;
        } else {
            return gameTable;
        }
    }
}
