package com.tiho.dlplugin;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;

import com.ryg.dynamicload.DLBasePluginService;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.exception.CrashHandler;
import com.tiho.dlplugin.export.PushServiceImp;
import com.tiho.dlplugin.util.EnvArgu;

import java.util.HashMap;


public class PluginService extends DLBasePluginService {

    public static Context sThisContext;
    private static boolean sIsRunning = false;


    @Override
    public void onCreate() {
        LogManager.setSaveName("com.shoot.dlplugin.txt");
        LogManager.LogShow("PluginService onCreate sIsRunning" + sIsRunning);
        Global.sPackageName = getPackageName();
        Global.sHostPackageName=that.getPackageName();
        if (sIsRunning) {
            LogManager.LogShow("PluginService isRunning, return");
            return;
        }
        super.onCreate();
        CommonInfo.getInstance(that);
        PushServiceImp.getInstance(that).onCreate();
        CrashHandler.getInstance(that).init();
        sThisContext = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogManager.LogShow("PluginService onBind intent = " + intent);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManager.LogShow("PluginService onStartCommand sIsRunning" + sIsRunning);
        if (sIsRunning) {
            LogManager.LogShow("PluginService isRunning, return");
            return START_STICKY;
        }
        LogManager.LogShow("PluginService onStartCommand intent = " + intent + ", flags = " + flags + ", startId = " + startId);
        EnvArgu.initFromHost(getEnvArgus());
        int rs = PushServiceImp.getInstance(that).onStartCommand();
        LogManager.LogShow("PluginService onStartCommand rs = " + rs);
        sIsRunning = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogManager.LogShow("PluginService onDestroy");
        PushServiceImp.getInstance(that).onDestroy();
        sIsRunning = false;
        super.onDestroy();
    }

    private  HashMap<String, Integer> getEnvArgus(){
        Resources res = that.getBaseContext().getResources();
        String pkgName = that.getBaseContext().getPackageName();
        HashMap<String, Integer> map = new HashMap<>();
        map.put("layoutid", res.getIdentifier("layout_pushnotification", "layout", pkgName));
        map.put("idIcon", res.getIdentifier("push_iv_icon", "id", pkgName));
        map.put("idTitle", res.getIdentifier("push_tv_title", "id", pkgName));
        map.put("idTime", res.getIdentifier("push_tv_time", "id", pkgName));
        map.put("idSlogan", res.getIdentifier("push_tv_des", "id", pkgName));
        map.put("idExtra", res.getIdentifier("push_iv_download", "id", pkgName));
        map.put("iconDefaultId", res.getIdentifier("push_notificationicon", "drawable", pkgName));
        LogManager.LogShow("PluginService onStartCommand map = " + map);
        return map;
    }
}
