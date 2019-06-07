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

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.utils.Question;

public class QuestionDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Questions.db";
    private final String DATABASE_PATH;
    private static int databaseVersion;
    private DatabaseTable baseTable;
    private DatabaseTable gameTable;
    private DatabaseTable versionTable = new DatabaseTable("version");

    private SQLiteDatabase db;

    public QuestionDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
        Scanner versionScanner = new Scanner(
                context.getResources().openRawResource(R.raw.database_version));
        try {
            databaseVersion = versionScanner.nextInt();
        } catch(Exception e) {
            Log.wtf("BrainRing","couldn't read version from its resource");
        }
        baseTable = new DatabaseTable("baseTable");
        DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).getPath();
        int newVersion = getVersion();
        if (!alreadyExists(baseTable) || newVersion != databaseVersion) {
            try {
                createDatabase();

                InputStream in = context.getAssets().open("Questions.db");
                OutputStream out = new FileOutputStream(DATABASE_PATH);

                byte[] buffer = new byte[2048];
                int written;
                while ((written = in.read(buffer)) > 0) {
                    out.write(buffer, 0, written);
                }
                out.flush();

                DatabaseTable tmp = new DatabaseTable("tmp");
                db.execSQL(createEntries(tmp));

                String selectAll = "SELECT " + DatabaseTable.COLUMN_QUESTION + ", " +
                        DatabaseTable.COLUMN_ANSWER + ", " +
                        DatabaseTable.COLUMN_COMMENT + ", " +
                        DatabaseTable.COLUMN_PASS_CRITERIA + " FROM " + baseTable.getTableName() + ";";
                Cursor copyCursor = db.rawQuery(selectAll, null);
                if (copyCursor.moveToFirst()) {
                    do {
                        String question = copyCursor.getString(
                                copyCursor.getColumnIndex(DatabaseTable.COLUMN_QUESTION));

                        String answer = copyCursor.getString(
                                copyCursor.getColumnIndex(DatabaseTable.COLUMN_ANSWER));

                        String comment = copyCursor.getString(
                                copyCursor.getColumnIndex(DatabaseTable.COLUMN_COMMENT));

                        String passCriterion = copyCursor.getString(
                                copyCursor.getColumnIndex(DatabaseTable.COLUMN_PASS_CRITERIA));

                        ContentValues row = new ContentValues();
                        row.put(DatabaseTable.COLUMN_QUESTION, question);
                        row.put(DatabaseTable.COLUMN_ANSWER, answer);
                        row.put(DatabaseTable.COLUMN_COMMENT, comment);
                        row.put(DatabaseTable.COLUMN_PASS_CRITERIA, passCriterion);

                        db.insert(tmp.getTableName(), null, row);
                    } while (copyCursor.moveToNext());
                }
                copyCursor.close();

                String deleteBaseTable = "DROP TABLE " + baseTable.getTableName() + ";";
                String createNewBaseTable = "ALTER TABLE " + tmp.getTableName() +
                        " RENAME TO " + baseTable.getTableName() + ";";

                db.execSQL(deleteBaseTable);
                db.execSQL(createNewBaseTable);
                databaseVersion = newVersion;

                out.close();
                in.close();
            } catch (IOException e) {
                Log.wtf("BrainRing", "failed to read database");
            }
        }
    }

    private int getVersion() {
        if (!alreadyExists(versionTable)) {
            return -1;
        }
        db = this.getReadableDatabase();

        String selectAll = "SELECT * FROM " + versionTable.getTableName() + ";";

        Cursor cursor = db.rawQuery(selectAll, null);
        cursor.moveToFirst();
        int version = cursor.getInt(cursor.getColumnIndex("version"));
        cursor.close();
        return version;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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
        Cursor cursor = db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = "
                + table.getTableName() + ";", null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    private String createEntries(DatabaseTable table) {
        return "CREATE TABLE IF NOT EXISTS " +
                table.getTableName() + "(" +
                DatabaseTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DatabaseTable.COLUMN_QUESTION + " TEXT," +
                DatabaseTable.COLUMN_ANSWER + " TEXT," +
                DatabaseTable.COLUMN_COMMENT + " TEXT," +
                DatabaseTable.COLUMN_PASS_CRITERIA + " TEXT);";
    }

    public void deleteEntries(DatabaseTable table) {
        db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + table.getTableName() + ";");
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
        String selectQuestion = "SELECT * FROM " + table.getTableName() +
                " WHERE " + DatabaseTable._ID + "=" + (id + 1) + ";";
        Cursor cursor = db.rawQuery(selectQuestion, null);
        String question = "";
        String answer = "";
        String comment = "";
        String passCriterion = "";

        if (cursor.moveToFirst()) {
            do {
                question = cursor.getString(cursor.getColumnIndex(DatabaseTable.COLUMN_QUESTION)).
                        replaceFirst("Вопрос [0-9]*:", "");

                answer = cursor.getString(cursor.getColumnIndex(DatabaseTable.COLUMN_ANSWER)).
                        replaceFirst("Ответ:", "");
                answer = answer.substring(0, answer.length() - 1);

                comment = cursor.getString(cursor.getColumnIndex(DatabaseTable.COLUMN_COMMENT)).
                        replaceFirst("Комментарий:", "");

                passCriterion = cursor.getString(cursor.getColumnIndex(DatabaseTable.COLUMN_PASS_CRITERIA)).
                        replaceFirst("Зачёт:", "").replaceAll(";", "/");
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (passCriterion.equals("")) {
            passCriterion = null;
        }

        return new Question(question, answer, passCriterion, comment, id);
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(
                    DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            Log.wtf("BrainRing", "database not found");
        }

        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    public DatabaseTable getBaseTable() {
        return baseTable;
    }

    private void loadQuestions(Document doc, DatabaseTable table) {
        db = this.getWritableDatabase();
        Elements elements = doc.select("div.question");
        ArrayList<String> questions = new ArrayList<>();
        ArrayList<String> answers = new ArrayList<>();
        ArrayList<String> comments = new ArrayList<>();
        ArrayList<String> passCriteria = new ArrayList<>();

        for (Element element : elements) {
            String comment = "";
            Elements maybeComment = element.select("p:has(strong.Comments)");
            if (!maybeComment.isEmpty()) {
                comment = maybeComment.get(0).text();
            }

            String passCriterion = "";
            Elements maybePass = element.select("p:has(strong.PassCriteria)");
            if (!maybePass.isEmpty()) {
                passCriterion = maybePass.get(0).text();
            }

            questions.add(element.select("p:has(strong.Question)").get(0).text());
            answers.add(element.select("p:has(strong.Answer)").get(0).text());
            passCriteria.add(passCriterion);
            comments.add(comment);
        }

        for (int i = 0; i < questions.size(); i++) {
            ContentValues row = new ContentValues();
            row.put(DatabaseTable.COLUMN_QUESTION, questions.get(i));
            row.put(DatabaseTable.COLUMN_ANSWER, answers.get(i));
            row.put(DatabaseTable.COLUMN_COMMENT, comments.get(i));
            row.put(DatabaseTable.COLUMN_PASS_CRITERIA, passCriteria.get(i));
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
