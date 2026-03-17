package com.example.calendarchecklist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class DataPick extends AppCompatActivity {
    // 声明成员变量
    private NumberPicker npStartYear, npStartMonth, npStartDay;
    private NumberPicker npEndYear, npEndMonth, npEndDay;
    private CheckBox checkBoxNoEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datapick);

        // 初始化各 NumberPicker
        initNumberPickers();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 设置年月日联动
        setDatePickerListeners(npStartYear, npStartMonth, npStartDay);
        setDatePickerListeners(npEndYear, npEndMonth, npEndDay);

        checkBoxNoEnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 勾选：结束日期为无限远，且禁用选择器
                npEndYear.setValue(2099);
                npEndMonth.setValue(12);
                npEndDay.setValue(31);
                npEndYear.setEnabled(false);
                npEndMonth.setEnabled(false);
                npEndDay.setEnabled(false);
            } else {
                // 取消勾选：结束日期设为今天的下一天，并启用选择器
                Calendar today = Calendar.getInstance();
                // 增加一天
                today.add(Calendar.DAY_OF_MONTH, 1);
                int year = today.get(Calendar.YEAR);
                int month = today.get(Calendar.MONTH) + 1;
                int day = today.get(Calendar.DAY_OF_MONTH);

                npEndYear.setValue(year);
                npEndMonth.setValue(month);
                npEndDay.setValue(day);
                npEndYear.setEnabled(true);
                npEndMonth.setEnabled(true);
                npEndDay.setEnabled(true);
                // 确保天数范围正确
                updateDays(npEndYear, npEndMonth, npEndDay);
            }
        });

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> {
            // 获取选择的日期
            int startYear = npStartYear.getValue();
            int startMonth = npStartMonth.getValue();
            int startDay = npStartDay.getValue();

            int endYear = npEndYear.getValue();
            int endMonth = npEndMonth.getValue();
            int endDay = npEndDay.getValue();



            // 验证结束日期是否在开始日期之后
            if (!isEndDateAfterStartDate(startYear, startMonth, startDay, endYear, endMonth, endDay)) {
                // 显示错误提示
                new android.app.AlertDialog.Builder(this)
                        .setTitle("！！警告！！")
                        .setMessage("结束日期必须在开始日期之后")
                        .setPositiveButton("确定", null)
                        .show();
                return;
            }

            // 格式化日期
            String startDate = formatDate(startYear, startMonth, startDay);
            String endDate = checkBoxNoEnd.isChecked() ? "无限远" : formatDate(endYear, endMonth, endDay);

            // 添加日志
            android.util.Log.d("DataPick", "checkBoxNoEnd.isChecked() = " + checkBoxNoEnd.isChecked());
            android.util.Log.d("DataPick", "endDate = " + endDate);

            String dateRange = startDate + " 至 " + endDate;
            android.util.Log.d("DataPick", "dateRange = " + dateRange);


            // 返回结果
            Intent intent = new Intent();
            intent.putExtra("dateRange", dateRange);
            setResult(RESULT_OK, intent);
            finish();
        });


    }

    private void initNumberPickers() {
        // 获取当前日期
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        // 起始日期：默认为今天
        npStartYear = findViewById(R.id.npStartYear);
        npStartMonth = findViewById(R.id.npStartMonth);
        npStartDay = findViewById(R.id.npStartDay);

        npStartYear.setMinValue(1911);
        npStartYear.setMaxValue(2077);
        npStartYear.setValue(currentYear);

        npStartMonth.setMinValue(1);
        npStartMonth.setMaxValue(12);
        npStartMonth.setValue(currentMonth + 1);
        npStartMonth.setFormatter(value -> String.format("%02d", value)); // 显示两位

        npStartDay.setMinValue(1);
        npStartDay.setMaxValue(now.getActualMaximum(Calendar.DAY_OF_MONTH));
        int MaxValue = now.getActualMaximum(Calendar.DAY_OF_MONTH);
        npStartDay.setValue(currentDay);

        // 结束日期：默认为今天的下一天
        npEndYear = findViewById(R.id.npEndYear);
        npEndMonth = findViewById(R.id.npEndMonth);
        npEndDay = findViewById(R.id.npEndDay);

        npEndYear.setMinValue(1900);
        npEndYear.setMaxValue(2100);

        // 计算今天的下一天
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        int tomorrowYear = tomorrow.get(Calendar.YEAR);
        int tomorrowMonth = tomorrow.get(Calendar.MONTH) + 1;
        int tomorrowDay = tomorrow.get(Calendar.DAY_OF_MONTH);

        npEndYear.setValue(tomorrowYear);
        npEndMonth.setMinValue(1);
        npEndMonth.setMaxValue(12);
        npEndMonth.setValue(tomorrowMonth);
        npEndMonth.setFormatter(value -> String.format("%02d", value));

        npEndDay.setMinValue(1);
        npEndDay.setMaxValue(tomorrow.getActualMaximum(Calendar.DAY_OF_MONTH));
        npEndDay.setValue(tomorrowDay);

        checkBoxNoEnd = findViewById(R.id.checkBoxNoEnd);
        checkBoxNoEnd.setChecked(false);
        npEndYear.setEnabled(true);
        npEndMonth.setEnabled(true);
        npEndDay.setEnabled(true);
        // 更新起始日期的天数（根据年月）
        updateDays(npStartYear, npStartMonth, npStartDay);
        updateDays(npEndYear, npEndMonth, npEndDay);

    }

    private void setDatePickerListeners(NumberPicker year, NumberPicker month, NumberPicker day) {
        year.setOnValueChangedListener((picker, oldVal, newVal) -> {
            updateDays(year, month, day);
            validateDateRange();
        });
        month.setOnValueChangedListener((picker, oldVal, newVal) -> {
            updateDays(year, month, day);
            validateDateRange();
        });
        day.setOnValueChangedListener((picker, oldVal, newVal) -> {
            validateDateRange();
        });
    }

    private void validateDateRange() {
        if (checkBoxNoEnd.isChecked()) {
            return; // 无结束日期时不需要验证
        }

        int startYear = npStartYear.getValue();
        int startMonth = npStartMonth.getValue();
        int startDay = npStartDay.getValue();

        int endYear = npEndYear.getValue();
        int endMonth = npEndMonth.getValue();
        int endDay = npEndDay.getValue();

        if (!isEndDateAfterStartDate(startYear, startMonth, startDay, endYear, endMonth, endDay)) {
            // 可以在这里添加视觉提示，比如改变边框颜色或显示错误信息
        }
    }

    private void updateDays(NumberPicker yearPicker, NumberPicker monthPicker, NumberPicker dayPicker) {
        int year = yearPicker.getValue();
        int month = monthPicker.getValue(); // 1-12
        // 计算该月的天数
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(maxDays);
        if (dayPicker.getValue() > maxDays) {
            dayPicker.setValue(maxDays);
        }
    }

    private String formatDate(int year, int month, int day) {
        return String.format("%d-%02d-%02d", year, month, day);
    }


    private boolean isEndDateAfterStartDate(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        // 创建开始日期的 Calendar 对象
        Calendar startDate = Calendar.getInstance();
        startDate.set(startYear, startMonth - 1, startDay);

        // 创建结束日期的 Calendar 对象
        Calendar endDate = Calendar.getInstance();
        endDate.set(endYear, endMonth - 1, endDay);

        // 比较两个日期
        return endDate.after(startDate) || endDate.equals(startDate);
    }


}
