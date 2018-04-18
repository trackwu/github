package com.tiho;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.tiho.multidex.MultiDex;


/**
 * 加载更多类
 * Created by Jerry on 2016/8/18.
 */
public class GameApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }

    public void onCreate() {
        super.onCreate();
        long start= System.currentTimeMillis();
        MultiDex.install(this, "dlShootCore","base2","dlShootCore.dat");
        long end=System.currentTimeMillis();
        Log.e("DexTime","Total="+String.valueOf(end-start));
    }



}


