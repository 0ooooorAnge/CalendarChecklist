package com.example.calendarchecklist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class MainActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private EditText editTextNewTask;
    private CheckBox checkBoxDaily;
    private Button buttonAdd;

    private TaskDbHelper dbHelper;
    private TaskAdapter adapter;

    private String currentDate; // yyyy-MM-dd

    private CheckBox checkBoxSpecial;

    private static final int REQUEST_CODE_ADD_TASK = 1; // 添加请求码
    private TextView textViewTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerView);
        Button buttonAdd = findViewById(R.id.buttonAdd); // 获取按钮
        textViewTitle = findViewById(R.id.textViewTitle);
        dbHelper = new TaskDbHelper(this);

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        loadTasksForDate(currentDate);
        calendarView.setSelectedDate(CalendarDay.today());
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                currentDate = String.format(Locale.getDefault(), "%d-%02d-%02d", date.getYear(), date.getMonth() + 1, date.getDay());
                loadTasksForDate(currentDate);
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putExtra("date", currentDate); // 传递当前选中日期
                startActivityForResult(intent, REQUEST_CODE_ADD_TASK);
            }
        });

        //updateCalendarDecorators();
    }
    private void updateTitleForDate(String date) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateObj = originalFormat.parse(date);
            SimpleDateFormat titleFormat = new SimpleDateFormat("M月d日", Locale.getDefault());
            String formattedDate = titleFormat.format(dateObj);
            textViewTitle.setText("今日事项（" + formattedDate + "）");
        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析失败，回退到原标题
            textViewTitle.setText("今日事项");
        }
    }



    private void loadTasksForDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        generateDailyLogsForDate(db, date);

        String query = "SELECT l." + TaskContract.TaskLogEntry._ID + ", " +
                "l." + TaskContract.TaskLogEntry.COLUMN_TASK_ID + ", " +  // 添加这一行
                "d." + TaskContract.TaskDefinitionEntry.COLUMN_NAME + " AS task_name, " +
                "l." + TaskContract.TaskLogEntry.COLUMN_COMPLETED + ", " +
                "d." + TaskContract.TaskDefinitionEntry.COLUMN_IS_SPECIAL + ", " +
                "d." + TaskContract.TaskDefinitionEntry.COLUMN_IS_DAILY +
                " FROM " + TaskContract.TaskLogEntry.TABLE_NAME + " l " +
                " JOIN " + TaskContract.TaskDefinitionEntry.TABLE_NAME + " d" +
                " ON l." + TaskContract.TaskLogEntry.COLUMN_TASK_ID + " = d." + TaskContract.TaskDefinitionEntry._ID +
                " WHERE l." + TaskContract.TaskLogEntry.COLUMN_DATE + " = ?" +
                " ORDER BY l." + TaskContract.TaskLogEntry._ID + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (adapter == null) {
            adapter = new TaskAdapter(this, cursor);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.swapCursor(cursor);
        }
        updateTitleForDate(date);
    }

    public void showDeleteDialog(long taskDefId, boolean isDaily) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (isDaily) {
            builder.setTitle("删除重复任务")
                    .setMessage("这是一个每日重复任务，请选择删除范围：")
                    .setPositiveButton("删除全部", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteTask(taskDefId, true); // true表示删除全部
                        }
                    })
                    .setNegativeButton("仅删除今天", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteTask(taskDefId, false); // false表示仅删除今天
                        }
                    })
                    .setNeutralButton("取消", null)
                    .show();
        } else {
            builder.setTitle("删除任务")
                    .setMessage("确定要删除这个任务吗？")
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteTask(taskDefId, true); // 非每日任务，删除定义即删除全部
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    private void deleteTask(long taskDefId, boolean deleteAll) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (deleteAll) {
            // 删除任务定义（级联删除关联的日志记录）
            db.delete(TaskContract.TaskDefinitionEntry.TABLE_NAME,
                    TaskContract.TaskDefinitionEntry._ID + " = ?",
                    new String[]{String.valueOf(taskDefId)});
        } else {
            // 仅删除今天的日志记录
            db.delete(TaskContract.TaskLogEntry.TABLE_NAME,
                    TaskContract.TaskLogEntry.COLUMN_TASK_ID + " = ? AND " +
                            TaskContract.TaskLogEntry.COLUMN_DATE + " = ?",
                    new String[]{String.valueOf(taskDefId), currentDate});
        }
        // 刷新当前日期的列表和日历标记
        loadTasksForDate(currentDate);
        updateCalendarDecorators();
    }

    private void generateDailyLogsForDate(SQLiteDatabase db, String date) {
        Cursor dailyTasks = db.query(
                TaskContract.TaskDefinitionEntry.TABLE_NAME,
                new String[]{TaskContract.TaskDefinitionEntry._ID},
                TaskContract.TaskDefinitionEntry.COLUMN_IS_DAILY + " = 1",
                null, null, null, null
        );

        while (dailyTasks.moveToNext()) {
            long taskId = dailyTasks.getLong(0);
            Cursor log = db.query(
                    TaskContract.TaskLogEntry.TABLE_NAME,
                    new String[]{TaskContract.TaskLogEntry._ID},
                    TaskContract.TaskLogEntry.COLUMN_TASK_ID + " = ? AND " + TaskContract.TaskLogEntry.COLUMN_DATE + " = ?",
                    new String[]{String.valueOf(taskId), date},
                    null, null, null
            );
            if (!log.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(TaskContract.TaskLogEntry.COLUMN_TASK_ID, taskId);
                values.put(TaskContract.TaskLogEntry.COLUMN_DATE, date);
                values.put(TaskContract.TaskLogEntry.COLUMN_COMPLETED, 0);
                db.insert(TaskContract.TaskLogEntry.TABLE_NAME, null, values);
            }
            log.close();
        }
        dailyTasks.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_TASK && resultCode == RESULT_OK) {
            // 添加成功，刷新当前日期的列表和日历标记
            loadTasksForDate(currentDate);
            updateCalendarDecorators();
        }
    }

    public void updateTaskStatus(long logId, boolean isCompleted) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskLogEntry.COLUMN_COMPLETED, isCompleted ? 1 : 0);
        db.update(
                TaskContract.TaskLogEntry.TABLE_NAME,
                values,
                TaskContract.TaskLogEntry._ID + " = ?",
                new String[]{String.valueOf(logId)}
        );
    }

    private void updateCalendarDecorators() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // 查询所有非特殊事件的任务日期
        String query = "SELECT DISTINCT l." + TaskContract.TaskLogEntry.COLUMN_DATE +
                " FROM " + TaskContract.TaskLogEntry.TABLE_NAME + " l" +
                " JOIN " + TaskContract.TaskDefinitionEntry.TABLE_NAME + " d" +
                " ON l." + TaskContract.TaskLogEntry.COLUMN_TASK_ID + " = d." + TaskContract.TaskDefinitionEntry._ID +
                " WHERE d." + TaskContract.TaskDefinitionEntry.COLUMN_IS_SPECIAL + " = 0";
        Cursor cursor = db.rawQuery(query, null);

        HashSet<CalendarDay> datesWithLogs = new HashSet<>();
        while (cursor.moveToNext()) {
            String dateStr = cursor.getString(0);
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int day = Integer.parseInt(parts[2]);
                datesWithLogs.add(CalendarDay.from(year, month, day));
            }
        }
        cursor.close();

        calendarView.removeDecorators();
        //calendarView.addDecorator(new EventDecorator(datesWithLogs));
    }

//    private class EventDecorator implements DayViewDecorator {
//        private final HashSet<CalendarDay> dates;
//
//        EventDecorator(HashSet<CalendarDay> dates) {
//            this.dates = dates;
//        }
//
//        @Override
//        public boolean shouldDecorate(CalendarDay day) {
//            return dates.contains(day);
//        }
//
//        @Override
//        public void decorate(DayViewFacade view) {
//            view.addSpan(new DotSpan(5, getResources().getColor(R.color.colorAccent)));
//        }
//    }
}