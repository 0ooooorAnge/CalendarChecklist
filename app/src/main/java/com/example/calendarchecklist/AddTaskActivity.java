package com.example.calendarchecklist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    private EditText editTextNewTask;
    private RadioButton radioButtonDaily;
    private RadioButton radioButtonSpecial;
    private TaskDbHelper dbHelper;
    private String targetDate;

    private String startDate;  // 格式 yyyy-MM-dd
    private String endDate;    // 格式 yyyy-MM-dd 或 null（表示无限远）

    private TextView tvDateRange;
    private ActivityResultLauncher<Intent> datePickLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.md_theme_background));
        datePickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String dateRange = result.getData().getStringExtra("dateRange");
                        if (dateRange != null) {
                            tvDateRange.setText(dateRange);
                            parseDateRange(dateRange);   // 解析并保存 startDate, endDate
                        }
                    }
                }
        );


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true); // 让图标可点击
            getSupportActionBar().setDisplayShowTitleEnabled(false); //隐藏ActionBar
        }


        // 获取传递的日期
        targetDate = getIntent().getStringExtra("date");
        if (targetDate == null) {
            // 如果没有传递日期，默认使用今天
            targetDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(new java.util.Date());
        }

        editTextNewTask = findViewById(R.id.editTextNewTask);
        radioButtonSpecial=findViewById(R.id.radioButtonSpecial);
        radioButtonDaily=findViewById(R.id.radioButtonDaily);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button pickDate = findViewById(R.id.buttonPickDate);
        dbHelper = new TaskDbHelper(this);
        tvDateRange = findViewById(R.id.tvDateRange);
        updatePickDateState();
        radioButtonDaily.setOnCheckedChangeListener((buttonView, isChecked) -> updatePickDateState());
        radioButtonSpecial.setOnCheckedChangeListener((buttonView, isChecked) -> updatePickDateState());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        // 保存按钮点击事件
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskText = editTextNewTask.getText().toString().trim();
                if (taskText.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "请输入事项内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean isDaily = radioButtonDaily.isChecked();
                boolean isSpecial = radioButtonSpecial.isChecked();
                addTask(taskText, isDaily, isSpecial);
            }
        });

        //选择日期按钮点击事件
        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddTaskActivity.this,DataPick.class);
                datePickLauncher.launch(intent);

            }
        });

        //找到返回按钮并设置点击事件
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 关闭当前页面，返回上一页
            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1 && resultCode == RESULT_OK) {
//            String dateRange = data.getStringExtra("dateRange");
//            if (dateRange != null) {
//                tvDateRange.setText(dateRange);
//            }
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 自定义返回逻辑
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // 添加任务到数据库
    private void addTask(String taskName, boolean isDaily, boolean isSpecial) {
        if (!isDaily && !isSpecial) {
            Toast.makeText(this, "请选择事件类型！", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 插入任务定义
        ContentValues defValues = new ContentValues();
        defValues.put(TaskContract.TaskDefinitionEntry.COLUMN_NAME, taskName);
        defValues.put(TaskContract.TaskDefinitionEntry.COLUMN_IS_DAILY, isDaily ? 1 : 0);
        defValues.put(TaskContract.TaskDefinitionEntry.COLUMN_IS_SPECIAL, isSpecial ? 1 : 0);
        if (isDaily) {
            defValues.put(TaskContract.TaskDefinitionEntry.COLUMN_END_DATE, endDate);
        }

        long taskDefId = db.insert(TaskContract.TaskDefinitionEntry.TABLE_NAME, null, defValues);
        if (taskDefId == -1) {
            Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 处理日志记录
        if (isSpecial) {
            // 特殊任务：只插入当前选中日期的日志
            insertTaskLog(db, taskDefId, targetDate);
            Toast.makeText(this, "特殊任务添加成功", Toast.LENGTH_SHORT).show();
        } else if (isDaily) {
            if (startDate == null) {
                Toast.makeText(this, "请先选择日期范围", Toast.LENGTH_SHORT).show();
                db.delete(TaskContract.TaskDefinitionEntry.TABLE_NAME,
                        TaskContract.TaskDefinitionEntry._ID + " = ?",
                        new String[]{String.valueOf(taskDefId)});
                return;
            }
            if (endDate == null) {
                // 无限远：只添加定义，不插入日志（由 MainActivity 自动补全）
                Toast.makeText(this, "每日任务已添加，将自动延续", Toast.LENGTH_SHORT).show();
            } else {
                // 有限范围：批量插入日志
                ArrayList<String> dateList = getDateRangeList(startDate, endDate);
                if (dateList.isEmpty()) {
                    Toast.makeText(this, "日期范围无效", Toast.LENGTH_SHORT).show();
                    db.delete(TaskContract.TaskDefinitionEntry.TABLE_NAME,
                            TaskContract.TaskDefinitionEntry._ID + " = ?",
                            new String[]{String.valueOf(taskDefId)});
                    return;
                }
                db.beginTransaction();
                try {
                    for (String date : dateList) {
                        insertTaskLog(db, taskDefId, date);
                    }
                    db.setTransactionSuccessful();
                    Toast.makeText(this, "已添加 " + dateList.size() + " 天的任务", Toast.LENGTH_SHORT).show();
                } finally {
                    db.endTransaction();
                }
            }
        }

        setResult(RESULT_OK);
        finish();
    }



    /**
     * 根据RadioButton的选中状态，控制日期选择按钮的可用性，并清空无效的日期范围显示
     */
    private void updatePickDateState() {
        Button pickDate = findViewById(R.id.buttonPickDate);
        TextView tvDateRange = findViewById(R.id.tvDateRange);

        // 只有当“每日任务”被选中时，才允许选择日期范围
        boolean isDailyChecked = radioButtonDaily.isChecked();
        pickDate.setEnabled(isDailyChecked);

        // 如果不可用，清空已显示的日期范围
        if (!isDailyChecked && tvDateRange != null) {
            tvDateRange.setText("");
        }
    }

    private void parseDateRange(String dateRange) {
        // 格式示例: "2026-04-07 至 2026-04-08" 或 "2026-04-07 至 无限远"
        String[] parts = dateRange.split(" 至 ");
        if (parts.length == 2) {
            startDate = parts[0];
            String endPart = parts[1];
            if ("无限远".equals(endPart)) {
                endDate = null;
            } else {
                endDate = endPart;
            }
        }
    }

    private void insertTaskLog(SQLiteDatabase db, long taskDefId, String date) {
        ContentValues logValues = new ContentValues();
        logValues.put(TaskContract.TaskLogEntry.COLUMN_TASK_ID, taskDefId);
        logValues.put(TaskContract.TaskLogEntry.COLUMN_DATE, date);
        logValues.put(TaskContract.TaskLogEntry.COLUMN_COMPLETED, 0);
        db.insert(TaskContract.TaskLogEntry.TABLE_NAME, null, logValues);
    }

    private ArrayList<String> getDateRangeList(String startDateStr, String endDateStr) {
        ArrayList<String> dates = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date start = sdf.parse(startDateStr);
            Date end = sdf.parse(endDateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            while (!cal.getTime().after(end)) {
                dates.add(sdf.format(cal.getTime()));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dates;
    }

}