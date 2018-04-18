package com.tiho.dlplugin.util;


import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.text.TextUtils;

import com.tiho.base.common.LogManager;

import java.util.List;

public class CommonUtil {
    public static boolean newTaskOpen = false;

    public static long getFirstTime(Context context) {
        long firstTime = 0;
        try {
            SharedPreferences spf = context.getSharedPreferences("pushdatas", Context.MODE_PRIVATE);
            if (spf != null) {
                firstTime = spf.getLong("firsttime", 0);
                if (firstTime == 0) {
                    Long elapsedRealtime = SystemClock.elapsedRealtime();
                    Editor editor = spf.edit();
                    editor.putLong("firsttime", elapsedRealtime);
                    editor.apply();
                    firstTime = elapsedRealtime;
                    LogManager.LogShow("getFirstTime init. elapsedRealTime = " + elapsedRealtime);
                }
            } else {
                LogManager.LogShow("onCreate error spf is null.");
            }

        } catch (Exception e) {
            LogManager.LogShow(e);
        }
        LogManager.LogShow("getFirstTime firstTime = " + firstTime);
        return firstTime;
    }


    public static String getWebSession(Context context) {
        String webSession = "";
        try {
            SharedPreferences spf = context.getSharedPreferences("pushdatas", Context.MODE_PRIVATE);
            if (spf != null) {
                webSession = spf.getString("webSession", "");
            } else {
                LogManager.LogShow("onCreate error spf is null.");
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
        LogManager.LogShow("getWebSession = " + webSession);
        return webSession;
    }

    public static void setWebSession(String webSession, Context context) {
        if (!TextUtils.isEmpty(webSession)) {
            try {
                SharedPreferences spf = context.getSharedPreferences("pushdatas", Context.MODE_PRIVATE);
                if (spf != null) {
                    Editor editor = spf.edit();
                    editor.putString("webSession", webSession);
                    editor.apply();
                } else {
                    LogManager.LogShow("onCreate error spf is null.");
                }
            } catch (Exception e) {
                LogManager.LogShow(e);
            }
        }
    }

    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

}
