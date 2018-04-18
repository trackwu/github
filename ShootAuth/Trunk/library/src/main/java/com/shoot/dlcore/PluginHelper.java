package com.shoot.dlcore;

import android.content.Context;

import com.ryg.dynamicload.DLPluginLogic;

public class PluginHelper {
    private static PluginHelper instance;
    private Context mContext;

    public PluginHelper(Context context) {
        this.mContext = context;
    }

    synchronized public static PluginHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PluginHelper(context);
        }
        return instance;
    }

    public void startPush() {
        new Thread() {
            @Override
            public void run() {
                if (!DLPluginLogic.isPluginLoaded(mContext.getApplicationContext())) {
                    try {
                        DLPluginLogic.loadPluginApkService(mContext.getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
