package com.example.calendarchecklist;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextNewTask;
    private CheckBox checkBoxDaily;
    private CheckBox checkBoxSpecial;
    private Button buttonSave;
    private TaskDbHelper dbHelper;
    private String targetDate; // 从主界面传递过来的日期

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true); // 可选，让图标可点击
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // 获取传递的日期
        targetDate = getIntent().getStringExtra("date");
        if (targetDate == null) {
            // 如果没有传递日期，默认使用今天
            targetDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(new java.util.Date());
        }

        editTextNewTask = findViewById(R.id.editTextNewTask);
        checkBoxDaily = findViewById(R.id.checkBoxDaily);
        checkBoxSpecial = findViewById(R.id.checkBoxSpecial);
        buttonSave = findViewById(R.id.buttonSave);


        dbHelper = new TaskDbHelper(this);

        // 保存按钮点击事件
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskText = editTextNewTask.getText().toString().trim();
                if (taskText.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "请输入事项内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean isDaily = checkBoxDaily.isChecked();
                boolean isSpecial = checkBoxSpecial.isChecked();
                addTask(taskText, isDaily, isSpecial);
            }
        });

        // 找到返回按钮并设置点击事件
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 关闭当前页面，返回上一页
            }
        });


    }
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
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 插入任务定义
        ContentValues defValues = new ContentValues();
        defValues.put(TaskContract.TaskDefinitionEntry.COLUMN_NAME, taskName);
        defValues.put(TaskContract.TaskDefinitionEntry.COLUMN_IS_DAILY, isDaily ? 1 : 0);
        defValues.put(TaskContract.TaskDefinitionEntry.COLUMN_IS_SPECIAL, isSpecial ? 1 : 0);
        long taskDefId = db.insert(TaskContract.TaskDefinitionEntry.TABLE_NAME, null, defValues);

        if (taskDefId == -1) {
            Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 为当前日期创建记录
        ContentValues logValues = new ContentValues();
        logValues.put(TaskContract.TaskLogEntry.COLUMN_TASK_ID, taskDefId);
        logValues.put(TaskContract.TaskLogEntry.COLUMN_DATE, targetDate);
        logValues.put(TaskContract.TaskLogEntry.COLUMN_COMPLETED, 0);
        db.insert(TaskContract.TaskLogEntry.TABLE_NAME, null, logValues);

        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK); // 设置结果码，让主界面知道添加成功
        finish(); // 关闭当前页面，返回主界面
    }
}