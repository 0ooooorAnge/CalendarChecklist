package com.example.calendarchecklist;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 读取保存的主题模式
        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int savedMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }
}


//package com.example.calendarchecklist;
//
//import android.app.Application;
//import android.content.Context;
//import android.content.SharedPreferences;
//
//import androidx.appcompat.app.AppCompatDelegate;
//
//public class MyApp extends Application {
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        // 读取保存的主题模式并设置
//        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//        int themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//    }
//}

//// 在你自定义的 Application 类 或 MainActivity 的 onCreate 之前调用
//public class MyApp extends Application {
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        // 让 App 跟随系统设置 (推荐)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//    }
//}