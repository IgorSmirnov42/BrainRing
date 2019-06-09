package ru.spbhse.brainring.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.utils.Question;

/** This class provides methods for managing database with questions */
public class QuestionDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Questions.db";
    private static int databaseVersion;
    private static QuestionDatabase database;
    private static DatabaseTable baseTable;
    private static DatabaseTable gameTable;
    private static DatabaseTable versionTable = new DatabaseTable("version");

    private static SQLiteDatabase db;

    /**
     * Constructs a database, using given context
     * Namely, checks whether the current database version,
     * stored on device is equal to actual database, stored in resources, and if not,
     * copies those questions to the device's database
     */
    private QuestionDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);

        baseTable = new DatabaseTable("baseTable");
        if (db == null) {
            db = this.getWritableDatabase();
        }
        int neededVersion = getNeededVersion(context);
        int currentVersion = getCurrentVersion();


        if (!alreadyExists(baseTable) || currentVersion != neededVersion) {
            try {
                InputStream in = context.getAssets().open("Questions.db");
                OutputStream out = new FileOutputStream(context.getDatabasePath(DATABASE_NAME).getPath());

                byte[] buffer = new byte[2048];
                int written;
                while ((written = in.read(buffer)) > 0) {
                    out.write(buffer, 0, written);
                }
                out.flush();

                DatabaseTable tmp = new DatabaseTable("tmp");
                db.execSQL(createEntries(tmp));

                String resetVersion = "UPDATE version SET version = -1;";
                db.execSQL(resetVersion);

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
                databaseVersion = currentVersion;
                updateVersion(context);

                out.close();
                in.close();
            } catch (IOException e) {
                Log.wtf(Controller.APP_TAG, "failed to read database");
            }
        }
    }

    private int getNeededVersion(Context context) {
        Scanner versionScanner = new Scanner(
                context.getResources().openRawResource(R.raw.database_version));
        int version = -1;
        try {
             version = versionScanner.nextInt();
        } catch (Exception e) {
            Log.wtf(Controller.APP_TAG, "couldn't read version from its resource");
        }
        return version;
    }

    /** Returns single instance of database, or constructs a new one, if there was no such */
    public static QuestionDatabase getInstance(Context context) {
        if (database == null) {
            database = new QuestionDatabase(context);
        }
        return database;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    /** {@inheritDoc} */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.disableWriteAheadLogging();
    }

    /** Creates a new table in database, based on the given table */
    public void createTable(@Nullable DatabaseTable table) {
        if (table == null) {
            table = getBaseTable();
        }

        if (alreadyExists(table)) {
            Log.d(Controller.APP_TAG, "Table already exists");
            return;
        }

        db.execSQL(createEntries(table));
        try {
            Document doc = Jsoup.connect(table.getURL()).get();
            loadQuestions(doc, table);
        } catch (IOException e) {
            Log.wtf(Controller.APP_TAG, "Error occurred while connecting to the url " + table.getURL());
        }
    }

    /**
     * Updates database's version to a new one, written in res/raw/database_version.
     *
     * @param context context used to get the new version from resources
     */
    public void updateVersion(Context context) {
        int newVersion = getNeededVersion(context);
        String updateVersion = "UPDATE version SET version = " + newVersion + ";";
        db.execSQL(updateVersion);
    }

    /**
     * Shows whether the database contains the given table
     *
     * @param table table to look up
     * @return {@code true} if the database contains the table
     */
    private boolean alreadyExists(@NonNull DatabaseTable table) {
        String queryTablesTitles = "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = "
                + table.getTableName() + ";";
        Cursor cursor = db.rawQuery(queryTablesTitles, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    /** Removes the given table from the database */
    public void deleteEntries(@NonNull DatabaseTable table) {
        db.execSQL("DROP TABLE IF EXISTS " + table.getTableName() + ";");
    }

    /** Returns number of questions in specified table */
    public long size(@NonNull DatabaseTable table) {
        return DatabaseUtils.queryNumEntries(db, table.getTableName());
    }

    /**
     * Selects a question from the table by its id
     *
     * @param table table to select from
     * @param id {@code _ID} of the question, must be in range [0..size of the table)
     * @return question
     */
    @NonNull
    public Question getQuestion(@NonNull DatabaseTable table, int id) {
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
                if (Pattern.compile("[^.]\\.$").matcher(answer).find()) {
                    answer = answer.substring(0, answer.length() - 1);
                }


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

    /** Returns base table */
    public DatabaseTable getBaseTable() {
        return baseTable;
    }

    /** Returns current game table. If user hasn't selected any special package, returns base table */
    public DatabaseTable getGameTable() {
        if (gameTable == null) {
            return baseTable;
        } else {
            return gameTable;
        }
    }

    /** Sets the game table */
    public void setGameTable(DatabaseTable table) {
        gameTable = table;
    }

    private void loadQuestions(Document doc, DatabaseTable table) {
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

    private int getCurrentVersion() {
        if (!alreadyExists(versionTable)) {
            return -1;
        }
        String selectAll = "SELECT * FROM " + versionTable.getTableName() + ";";
        Cursor cursor = db.rawQuery(selectAll, null);
        cursor.moveToFirst();
        int version = cursor.getInt(cursor.getColumnIndex("version"));
        cursor.close();
        return version;
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
}
