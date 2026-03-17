package com.example.calendarchecklist;

import android.provider.BaseColumns;

public final class TaskContract {
    private TaskContract() {}

    public static class TaskDefinitionEntry implements BaseColumns {
        public static final String TABLE_NAME = "task_definitions";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IS_DAILY = "is_daily";
        public static final String COLUMN_IS_SPECIAL = "is_special";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_IS_DAILY + " INTEGER DEFAULT 0, " +
                        COLUMN_IS_SPECIAL + " INTEGER DEFAULT 0, " +
                        COLUMN_END_DATE + " TEXT" +
                        ")";
    }
    public static class TaskLogEntry implements BaseColumns {
        public static final String TABLE_NAME = "task_logs";
        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_COMPLETED = "completed";
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TASK_ID + " INTEGER NOT NULL, " +
                        COLUMN_DATE + " TEXT NOT NULL, " +
                        COLUMN_COMPLETED + " INTEGER DEFAULT 0, " +
                        "FOREIGN KEY(" + COLUMN_TASK_ID + ") REFERENCES " +
                        TaskDefinitionEntry.TABLE_NAME + "(" + TaskDefinitionEntry._ID + ") ON DELETE CASCADE" +
                        ")";
    }
}