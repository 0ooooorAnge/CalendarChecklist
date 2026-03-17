package com.example.calendarchecklist;

import static android.provider.UserDictionary.Words._ID;
import static com.example.calendarchecklist.TaskContract.TaskDefinitionEntry.COLUMN_END_DATE;
import static com.example.calendarchecklist.TaskContract.TaskDefinitionEntry.COLUMN_IS_DAILY;
import static com.example.calendarchecklist.TaskContract.TaskDefinitionEntry.COLUMN_IS_SPECIAL;
import static com.example.calendarchecklist.TaskContract.TaskDefinitionEntry.COLUMN_NAME;
import static com.example.calendarchecklist.TaskContract.TaskDefinitionEntry.TABLE_NAME;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TaskDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Checklist.db";
    private static final int DATABASE_VERSION = 5;
    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 使用 TaskContract 中定义的建表语句
        db.execSQL(TaskContract.TaskDefinitionEntry.CREATE_TABLE);
        db.execSQL(TaskContract.TaskLogEntry.CREATE_TABLE);
    }
    //保证数据库平滑升级(不能删除！！！)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("TaskDbHelper", "onUpgrade from " + oldVersion + " to " + newVersion);
        try {
            db.execSQL("ALTER TABLE " + TaskContract.TaskDefinitionEntry.TABLE_NAME +
                    " ADD COLUMN " + TaskContract.TaskDefinitionEntry.COLUMN_END_DATE + " TEXT");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}