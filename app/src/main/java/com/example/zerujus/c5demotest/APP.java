package com.example.zerujus.c5demotest;

import android.app.Application;

/**
 * 日期：2019/5/5 0005 22:53
 * 包名：com.example.zerujus.c5demotest
 * 作者： wlznzx
 * 描述：
 */
public class APP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesUtil.getInstance(APP.this,SharedPreferencesUtil.NAME);
    }
}
