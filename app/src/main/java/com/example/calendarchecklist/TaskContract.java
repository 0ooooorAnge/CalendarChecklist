package com.example.calendarchecklist;

import android.provider.BaseColumns;

public final class TaskContract {
    private TaskContract() {}

    public static class TaskDefinitionEntry implements BaseColumns {
        public static final String TABLE_NAME = "task_definitions";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IS_DAILY = "is_daily";
        public static final String COLUMN_IS_SPECIAL = "is_special";
    }

    public static class TaskLogEntry implements BaseColumns {
        public static final String TABLE_NAME = "task_logs";
        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_COMPLETED = "completed";
    }
}