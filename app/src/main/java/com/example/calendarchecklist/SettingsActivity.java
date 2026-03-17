package com.example.calendarchecklist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup radioGroupTheme;
    private RadioButton radioFollowSystem, radioLight, radioDark;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_THEME = "theme_mode";
    private boolean isFirstSetup = false; // 标记是否为初始化

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        radioGroupTheme = findViewById(R.id.radioGroupTheme);
        radioFollowSystem = findViewById(R.id.radioFollowSystem);
        radioLight = findViewById(R.id.radioLight);
        radioDark = findViewById(R.id.radioDark);
        // 找到返回按钮并设置点击事件
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 关闭当前页面，返回上一页
            }
        });
        setupRadioGroup();
    }

    private void setupRadioGroup() {
        // 1. 读取保存的主题模式
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedMode = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // 2. 暂时移除监听器，避免设置选中时触发
        radioGroupTheme.setOnCheckedChangeListener(null);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true); // 可选，让图标可点击
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        // 3. 根据保存的模式设置选中项
        switch (savedMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                radioLight.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                radioDark.setChecked(true);
                break;
            default:
                radioFollowSystem.setChecked(true);
                break;
        }

        // 4. 设置监听器，处理用户选择
        radioGroupTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 跳过初始化时的触发
                if (isFirstSetup) {
                    isFirstSetup = false;
                    return;
                }

                // 确定用户选择的主题模式
                int selectedMode;
                if (checkedId == R.id.radioLight) {
                    selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
                } else if (checkedId == R.id.radioDark) {
                    selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                }

                // 保存并应用
                saveAndApplyTheme(selectedMode);
                recreate();
            }
        });
    }

    private void saveAndApplyTheme(int mode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
        // 重启当前 Activity 以应用新主题
        recreate();
    }
}