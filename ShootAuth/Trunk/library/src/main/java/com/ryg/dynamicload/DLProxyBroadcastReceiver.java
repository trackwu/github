package com.ryg.dynamicload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import com.ryg.utils.LOG;

import com.ryg.dynamicload.internal.DLIntent;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLPluginPackage;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ben on 2015/4/16.
 */
public class DLProxyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "DLBroadcast";

    public static DLProxyBroadcastReceiver instance;
    private static List<DLBroadcastReceiverPlugin> receiverPluginList = new ArrayList<DLBroadcastReceiverPlugin>();

    public static DLProxyBroadcastReceiver getInstance() {
        if (instance == null) {
            instance = new DLProxyBroadcastReceiver();
        }
        return instance;
    }

    public List<String> getActionList() {
        List<String> actionList = new ArrayList<String>();
        for (DLBroadcastReceiverPlugin dlBroadcastReceiverPlugin : receiverPluginList) {
            String[] actions = dlBroadcastReceiverPlugin.getActions();
            for (String action : actions) {
                actionList.add(action);
            }
        }
        return actionList;
    }

    public List<DLBroadcastReceiverPlugin> getReceiverPluginList() {
        return receiverPluginList;
    }

    public void onCreatePlugin(Context context, String packageName, String clazz, String[] actions) {
        DLPluginManager pluginManager = DLPluginManager.getInstance(context);
        DLPluginPackage pluginPackage = pluginManager.getPackage(packageName);
        try {
            Class<?> localClass = pluginPackage.classLoader.loadClass(clazz);
            Constructor<?> localConstructor = localClass.getConstructor(new Class[]{});
            Object instance = localConstructor.newInstance(new Object[]{});

            ((DLBroadcastReceiverPlugin) instance).setActions(actions);
            receiverPluginList.add((DLBroadcastReceiverPlugin) instance);
            LOG.d(TAG, "instance = " + instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        LOG.d(TAG, "onReceive action = " + intent.getAction());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    handleIntent(context, intent);
                }
            };
            timer.schedule(timerTask, 15000);
            return;
        }
        handleIntent(context, intent);
    }

    private void handleIntent(Context context, Intent intent) {
        if (!DLPluginLogic.isPluginLoaded(context)) {
            try {
                DLPluginPackage dlPluginPackage = DLPluginLogic.loadPluginApk(context);
                DLIntent dlIntent = new DLIntent(dlPluginPackage.packageName, dlPluginPackage.packageInfo.services[0].name);
                dlIntent.setAction(DLProxyService.ACTION_LOAD_PLUGIN_APK);
                dlIntent.putExtra("receive_action", intent.getAction());
                DLPluginManager.getInstance(context).startPluginService(context, dlIntent);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return;
            }
        }
        String action = intent.getAction();
        for (int i = 0; i < receiverPluginList.size(); i++) {
            DLBroadcastReceiverPlugin mRemoteBroadcastReceiver = receiverPluginList.get(i);
            String[] actions = mRemoteBroadcastReceiver.getActions();
            if (actions == null) {
                continue;
            }
            for (String actionPlugin : actions) {
                if (TextUtils.equals(actionPlugin, action)) {
                    mRemoteBroadcastReceiver.onReceive(context, intent);
                }
            }
        }
    }
}
