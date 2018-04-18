package com.ryg.dynamicload.internal;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import com.ryg.utils.LOG;

import com.ryg.dynamicload.DLApplicationPlugin;

import java.lang.reflect.Constructor;

public class DLApplicationProxyImpl {

    private static final String TAG = "DLApplicationProxyImpl";
    private Application mProxyApplication;
    private DLApplicationPlugin mRemoteApplication;

    private String mPackageName;
    private AssetManager mAssetManager;
    private Resources mResources;

    public DLApplicationProxyImpl(Application application) {
        mProxyApplication = application;
    }

    public void onCreate() {

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void init(String packageName, String clazz) {
        LOG.d(TAG, "clazz=" + clazz + " packageName=" + packageName);
        DLPluginManager pluginManager = DLPluginManager.getInstance(mProxyApplication);
        DLPluginPackage pluginPackage = pluginManager.getPackage(packageName);
        mPackageName = packageName;
        try {
            Class<?> localClass = pluginPackage.classLoader.loadClass(clazz);
            Constructor<?> localConstructor = localClass.getConstructor(new Class[]{});
            Object instance = localConstructor.newInstance(new Object[]{});
            mRemoteApplication = (DLApplicationPlugin) instance;
            ((DLApplicationAttachable) mProxyApplication).attach(mRemoteApplication, pluginManager);
            LOG.d(TAG, "instance = " + instance);
            // attach the proxy application and plugin package to the
            // mPluginActivity
            mRemoteApplication.attach(mProxyApplication, pluginPackage);

            //get the asset manager
            mAssetManager = pluginPackage.assetManager;
            mResources = pluginPackage.resources;

            mRemoteApplication.onCreate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AssetManager getAssets() {
        return mAssetManager;
    }

    public Resources getResources() {
        return mResources;
    }

    public String getPackageName() {
        return mPackageName;
    }

}
