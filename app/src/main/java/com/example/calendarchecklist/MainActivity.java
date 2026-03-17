package com.example.calendarchecklist;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.ParseException;
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
    private FloatingActionButton buttonAdd;
    private TaskDbHelper dbHelper;
    private TaskAdapter adapter;
    private String currentDate; // yyyy-MM-dd
    private static final int REQUEST_CODE_ADD_TASK = 1; // 添加请求码
    private TextView textViewTitle;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 设置状态栏颜色
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.md_theme_background));

        recyclerView = findViewById(R.id.recyclerView);
        textViewTitle = findViewById(R.id.textViewTitle);
        dbHelper = new TaskDbHelper(this);

        // 使状态栏透明，并让内容延伸到状态栏
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            );
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
//        setContentView(R.layout.activity_main);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//            );
//        }


        calendarView = findViewById(R.id.calendarView);
        calendarView.setSelectedDate(CalendarDay.today());// 设置日历视图默认选中的日期为今天
        // 为日历视图设置日期选中监听器，当用户点击某个日期时触发
        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            // 将选中的日期格式化为 "yyyy-MM-dd" 格式的字符串
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                currentDate = String.format(Locale.getDefault(), "%d-%02d-%02d", date.getYear(), date.getMonth() + 1, date.getDay());
                // 根据当前选中的日期加载该日期的任务列表
                loadTasksForDate(currentDate);
            }
        });

        // 获取当前 Activity 的 ActionBar，如果不为空则隐藏其标题显示
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //指定日期数字颜色和背景颜色
        int dateTextColor = ContextCompat.getColor(this, R.color.calendar_date_text);
        int backgroundColor = ContextCompat.getColor(this, R.color.calendar_bg);
        // 为日历视图添加一个装饰器，用于设置日期数字颜色和背景颜色为刚刚获取的 dateTextColor和backgroundColor
        calendarView.addDecorator(new DateTextColorDecorator(dateTextColor));
        // 设置选中日期的背景颜色（例如：使用资源文件中定义的颜色）
        calendarView.setSelectionColor(ContextCompat.getColor(this, R.color.md_theme_inversePrimary_mediumContrast));

        FloatingActionButton buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        //添加事件按钮监听器
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putExtra("date", currentDate); // 传递当前选中日期
                startActivityForResult(intent, REQUEST_CODE_ADD_TASK);// 启动添加任务 Activity，并期望返回结果，请求码为 REQUEST_CODE_ADD_TASK
            }
        });

        loadTasksForDate(currentDate);
    }

    // 重写选项菜单项点击事件的处理方法
    // 当用户点击 ActionBar 上的菜单项（例如返回按钮）时，此方法会被自动调用
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 自定义返回按钮的行为：直接结束当前 Activity，返回到上一个 Activity
            finish();
            return true;// 返回 true 表示事件已被消费，不再继续传递
        }
        // 如果不是返回按钮，则调用父类的默认处理逻辑（例如处理其他菜单项）
        return super.onOptionsItemSelected(item);
    }

    //随着日期选择变更 更改“今日事项”后面的日期
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
            // 先删除所有关联的日志
            db.delete(TaskContract.TaskLogEntry.TABLE_NAME,
                    TaskContract.TaskLogEntry.COLUMN_TASK_ID + " = ?",
                    new String[]{String.valueOf(taskDefId)});
            // 再删除任务定义
            db.delete(TaskContract.TaskDefinitionEntry.TABLE_NAME,
                    TaskContract.TaskDefinitionEntry._ID + " = ?",
                    new String[]{String.valueOf(taskDefId)});
        } else {
            // 仅删除今天的日志
            db.delete(TaskContract.TaskLogEntry.TABLE_NAME,
                    TaskContract.TaskLogEntry.COLUMN_TASK_ID + " = ? AND " +
                            TaskContract.TaskLogEntry.COLUMN_DATE + " = ?",
                    new String[]{String.valueOf(taskDefId), currentDate});
        }
        loadTasksForDate(currentDate);
        updateCalendarDecorators();
    }

    private void generateDailyLogsForDate(SQLiteDatabase db, String date) {
        // 查询所有每日任务，同时获取结束日期
        Cursor dailyTasks = db.query(
                TaskContract.TaskDefinitionEntry.TABLE_NAME,
                new String[]{
                        TaskContract.TaskDefinitionEntry._ID,
                        TaskContract.TaskDefinitionEntry.COLUMN_END_DATE
                },
                TaskContract.TaskDefinitionEntry.COLUMN_IS_DAILY + " = 1",
                null, null, null, null
        );

        while (dailyTasks.moveToNext()) {
            long taskId = dailyTasks.getLong(0);
            String endDateStr = dailyTasks.getString(1); // 可能为 null

            // 如果有结束日期且当前日期大于结束日期，则跳过补全
            if (endDateStr != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date endDate = sdf.parse(endDateStr);
                    Date currentDateObj = sdf.parse(date);
                    if (currentDateObj.after(endDate)) {
                        continue; // 超出范围，不补全
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // 检查日志是否已存在
            Cursor log = db.query(
                    TaskContract.TaskLogEntry.TABLE_NAME,
                    new String[]{TaskContract.TaskLogEntry._ID},
                    TaskContract.TaskLogEntry.COLUMN_TASK_ID + " = ? AND " +
                            TaskContract.TaskLogEntry.COLUMN_DATE + " = ?",
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

    //更新事项状态
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

    //更新日历
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
    }

    //字体装饰器
    private class DateTextColorDecorator implements DayViewDecorator {
        private final int color;
        public DateTextColorDecorator(int color) {
            this.color = color;
        }
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return true; // 装饰所有日期
        }
        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(color));
        }
    }


}