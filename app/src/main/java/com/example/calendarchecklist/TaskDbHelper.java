package com.example.calendarchecklist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Checklist.db";
    private static final int DATABASE_VERSION = 3;

    private static final String SQL_CREATE_TASK_DEFINITIONS =
            "CREATE TABLE " + TaskContract.TaskDefinitionEntry.TABLE_NAME + " (" +
                    TaskContract.TaskDefinitionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TaskContract.TaskDefinitionEntry.COLUMN_NAME + " TEXT NOT NULL," +
                    TaskContract.TaskDefinitionEntry.COLUMN_IS_DAILY + " INTEGER DEFAULT 0," +
                    TaskContract.TaskDefinitionEntry.COLUMN_IS_SPECIAL + " INTEGER DEFAULT 0)";



    private static final String SQL_CREATE_TASK_LOGS =
            "CREATE TABLE " + TaskContract.TaskLogEntry.TABLE_NAME + " (" +
                    TaskContract.TaskLogEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TaskContract.TaskLogEntry.COLUMN_TASK_ID + " INTEGER NOT NULL," +
                    TaskContract.TaskLogEntry.COLUMN_DATE + " TEXT NOT NULL," +
                    TaskContract.TaskLogEntry.COLUMN_COMPLETED + " INTEGER DEFAULT 0," +
                    "FOREIGN KEY(" + TaskContract.TaskLogEntry.COLUMN_TASK_ID + ") REFERENCES " +
                    TaskContract.TaskDefinitionEntry.TABLE_NAME + "(" + TaskContract.TaskDefinitionEntry._ID + "))";

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASK_DEFINITIONS);
        db.execSQL(SQL_CREATE_TASK_LOGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TaskContract.TaskDefinitionEntry.TABLE_NAME +
                    " ADD COLUMN " + TaskContract.TaskDefinitionEntry.COLUMN_IS_SPECIAL + " INTEGER DEFAULT 0");
        }
    }
}