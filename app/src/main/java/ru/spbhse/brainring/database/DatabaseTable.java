package ru.spbhse.brainring.database;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.net.URL;

/** Contains base info about tables in database */
public class DatabaseTable implements BaseColumns {
    /** Name of column with questions in the database */
    public static final String COLUMN_QUESTION = "question";

    /** Name of column with answers in the database */
    public static final String COLUMN_ANSWER = "answer";

    /** Name of column with comments in the database */
    public static final String COLUMN_COMMENT = "comment";

    /** Name of column with pass criteria in the database */
    public static final String COLUMN_PASS_CRITERIA = "passcriteria";

    private String tableName;
    private String url;

    /**
     * Constructs database table from the url
     * Table name is {@code url.getFile()}
     */
    public DatabaseTable(@NonNull URL url) {
        this.tableName = "\"" + url.getFile() + "\"";
        this.url = url.toString();
    }

    /**
     * Constructs database table from string
     * {@code url} is set to ""
     */
    public DatabaseTable(@NonNull String tableName) {
        this.tableName = "\"" + tableName + "\"";
        this.url = "";
    }

    /** Return name of this table*/
    @NonNull
    public String getTableName() {
        return tableName;
    }

    /** Returns url of this table. Shouldn't be called, if table was constructed from the string */
    @NonNull
    public String getURL() {
        return url;
    }
}
