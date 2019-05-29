package ru.spbhse.brainring.database;

import android.provider.BaseColumns;

import java.net.URL;

public class DatabaseTable implements BaseColumns {
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_ANSWER = "answer";
    public static final String COLUMN_COMMENT = "comment";

    private String tableName;
    private String url;

    public DatabaseTable(URL url) {
        this.tableName = "\"" + url.getFile() + "\"";
        this.url = url.toString();
    }

    public DatabaseTable(String tableName) {
        this.tableName = "\"" + tableName + "\"";
        this.url = "";
    }

    public String getTableName() {
        return tableName;
    }

    public String getURL() {
        return url;
    }
}
