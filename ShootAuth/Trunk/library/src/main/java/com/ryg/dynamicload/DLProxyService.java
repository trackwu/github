package com.ryg.dynamicload;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import com.ryg.utils.LOG;

import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLServiceAttachable;
import com.ryg.dynamicload.internal.DLServiceProxyImpl;
import com.ryg.utils.DLConstants;
import com.ryg.utils.LOG;

import java.util.ArrayList;
import java.util.List;

public class DLProxyService extends Service implements DLServiceAttachable {

    private static final String TAG = "DLProxyService";
    public static final String ACTION_LOAD_PLUGIN_APK = "load_plugin_apk";
    private DLServiceProxyImpl mImpl = new DLServiceProxyImpl(this);
    //    private DLServicePlugin mRemoteService;
    private DLPluginManager mPluginManager;

    private List<DLServicePlugin> mListService = new ArrayList<DLServicePlugin>();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        LOG.d(TAG, TAG + " onBind");
        //判断是否存在插件Service，如果存在，则不进行Service插件的构造工作
        String clazz = intent.getStringExtra(DLConstants.EXTRA_CLASS);
        DLServicePlugin service = getDLServicePlugin(clazz);
        if (service == null) {
            mImpl.init(intent);
        }
        service = getDLServicePlugin(clazz);
        return service.onBind(intent);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        LOG.d(TAG, TAG + " onCreate");

        if (!DLPluginLogic.isPluginLoaded(this)) {
            try {
                DLPluginLogic.loadPluginApk(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<String> actionList = DLProxyBroadcastReceiver.getInstance().getActionList();
        IntentFilter intentFilter = new IntentFilter();
        for (String action : actionList) {
            intentFilter.addAction(action);
        }
        registerReceiver(DLProxyBroadcastReceiver.getInstance(), intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        LOG.d(TAG, TAG + " onStartCommand " + intent);
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        if (!TextUtils.isEmpty(intent.getAction()) && ACTION_LOAD_PLUGIN_APK.equals(intent.getAction())) {
            try {
                if (!DLPluginLogic.isPluginLoaded(this)) {
                    DLPluginLogic.loadPluginApk(this);
                }
                String action = intent.getStringExtra("receive_action");
                List<DLBroadcastReceiverPlugin> receiverPluginList = DLProxyBroadcastReceiver.getInstance().getReceiverPluginList();
                for (int i = 0; i < receiverPluginList.size(); i++) {
                    DLBroadcastReceiverPlugin mRemoteBroadcastReceiver = receiverPluginList.get(i);
                    String[] actions = mRemoteBroadcastReceiver.getActions();
                    if (actions == null) {
                        continue;
                    }
                    for (String actionPlugin : actions) {
                        if (TextUtils.equals(actionPlugin, action)) {
                            mRemoteBroadcastReceiver.onReceive(this, intent);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //判断是否存在插件Service，如果存在，则不进行Service插件的构造工作
        String clazz = intent.getStringExtra(DLConstants.EXTRA_CLASS);
        DLServicePlugin service = getDLServicePlugin(clazz);
        if (service == null) {
            mImpl.init(intent);
        }
        service = getDLServicePlugin(clazz);
        LOG.d(TAG, TAG + " onStartCommand service = " + service);
        super.onStartCommand(intent, flags, startId);
        return service.onStartCommand(intent, flags, startId);
//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        unregisterReceiver(DLProxyBroadcastReceiver.getInstance());
        for (DLServicePlugin service : mListService) {
            service.onDestroy();
        }
        mListService.clear();
//        if (mRemoteService != null) {
//            mRemoteService.onDestroy();
//        }
        super.onDestroy();
        LOG.d(TAG, TAG + " onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        for (DLServicePlugin service : mListService) {
            service.onConfigurationChanged(newConfig);
        }
//        if (mRemoteService != null) {
//            mRemoteService.onConfigurationChanged(newConfig);
//        }
        super.onConfigurationChanged(newConfig);
        LOG.d(TAG, TAG + " onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        for (DLServicePlugin service : mListService) {
            service.onLowMemory();
        }
//        if (mRemoteService != null) {
//            mRemoteService.onLowMemory();
//        }
        super.onLowMemory();
        LOG.d(TAG, TAG + " onLowMemory");
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
        // TODO Auto-generated method stub
        for (DLServicePlugin service : mListService) {
            service.onTrimMemory(level);
        }
//        if (mRemoteService != null) {
//            mRemoteService.onTrimMemory(level);
//        }
        super.onTrimMemory(level);
        LOG.d(TAG, TAG + " onTrimMemory level =" + level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        LOG.d(TAG, TAG + " onUnbind");
        super.onUnbind(intent);
        for (DLServicePlugin service : mListService) {
            service.onUnbind(intent);
        }
        return true;
//        return mRemoteService.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
//        mRemoteService.onRebind(intent);
        for (DLServicePlugin service : mListService) {
            service.onRebind(intent);
        }
        super.onRebind(intent);
        LOG.d(TAG, TAG + " onRebind");
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
//        if (mRemoteService != null) {
//            mRemoteService.onTaskRemoved(rootIntent);
//        }
        for (DLServicePlugin service : mListService) {
            service.onTaskRemoved(rootIntent);
        }
        super.onTaskRemoved(rootIntent);
        LOG.d(TAG, TAG + " onTaskRemoved");
    }

    @Override
    public void attach(DLServicePlugin remoteService, DLPluginManager pluginManager) {
        // TODO Auto-generated method stub
//        mRemoteService = remoteService;
        mPluginManager = pluginManager;
        mListService.add(remoteService);
    }

    @Override
    public Resources getResources() {
        return mImpl.getResources() == null ? super.getResources() : mImpl.getResources();
    }

    private DLServicePlugin getDLServicePlugin(String className) {
        DLServicePlugin dlp = null;
        try {
            for (DLServicePlugin service : mListService) {
                if (service.getClass().getName().equals(className)) {
                    dlp = service;
                    LOG.d(TAG, TAG + " getDlService " + dlp);
                    break;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return dlp;
    }
}
