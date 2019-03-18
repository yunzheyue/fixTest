package com.example.myfixtest;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.example.myfixtest.utils.FixDexUtils;

/**
 * autour : lbing
 * date : 2018/5/31 0031 11:28
 * className :
 * version : 1.0
 * description :
 */


public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        FixDexUtils.loadFixedDex(base);
        super.attachBaseContext(base);
    }
}
