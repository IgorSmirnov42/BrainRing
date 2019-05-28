package ru.spbhse.brainring.utils;

import android.provider.BaseColumns;

import java.net.URL;

public class DataBaseTableEntry implements BaseColumns {
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_ANSWER = "answer";

    private String tableName;
    private String url;

    public DataBaseTableEntry(URL tableName) {
        this.tableName = "\"" + tableName.getFile() + "\"";
        this.url = tableName.toString();
    }

    public String getTableName() {
        return tableName;
    }

    public String getURL() {
        return url;
    }
}
