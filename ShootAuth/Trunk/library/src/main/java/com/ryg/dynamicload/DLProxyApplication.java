package com.ryg.dynamicload;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import com.ryg.utils.LOG;

import com.ryg.dynamicload.internal.DLApplicationAttachable;
import com.ryg.dynamicload.internal.DLApplicationProxyImpl;
import com.ryg.dynamicload.internal.DLPluginManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ben on 2015/4/10.
 */
public class DLProxyApplication extends Application implements DLApplicationAttachable {

    private static final String TAG = "DLProxyApplication";

    private List<Activity> mList = new LinkedList<Activity>();
    private List<Activity> mHostList = new LinkedList<Activity>();
    public static DLProxyApplication instance;
    private DLApplicationProxyImpl mImpl = new DLApplicationProxyImpl(this);
    private DLApplicationPlugin mRemoteApplication;
    private DLPluginManager mPluginManager;

    @Override
    public void onCreate() {
        super.onCreate();
        LOG.d(TAG, TAG + " onCreate");
        instance = this;

        mImpl.onCreate();
    }

//    @Override
//    public AssetManager getAssets() {
//        return mImpl.getAssets() == null ? super.getAssets() : mImpl.getAssets();
//    }

    @Override
    public Context getApplicationContext() {
        return instance;
    }

    public void onCreatePlugin(String packageName, String clazz) {
        if (mRemoteApplication == null) {
            mImpl.init(packageName, clazz);
        }
    }

    // add Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    public void deleteActivity(Activity activity) {
        mList.remove(activity);
    }

    public void addHostActivity(Activity activity) {
        mHostList.add(activity);
    }

    public void deleteHostActivity(Activity activity) {
        mHostList.remove(activity);
    }

    //关闭每一个list内的activity
    public void closeActivities() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeHostActivities() {
        try {
            for (Activity activity : mHostList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void attach(DLApplicationPlugin remoteApplication, DLPluginManager pluginManager) {
        mRemoteApplication = remoteApplication;
        mPluginManager = pluginManager;
    }

    @Override
    public Resources getResources() {
        return mImpl.getResources() == null ? super.getResources() : mImpl.getResources();
    }

}
