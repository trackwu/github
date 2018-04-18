package com.ryg.dynamicload.internal;

import java.lang.reflect.Constructor;

import com.ryg.dynamicload.DLServicePlugin;
import com.ryg.utils.DLConfigs;
import com.ryg.utils.DLConstants;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import com.ryg.utils.LOG;

public class DLServiceProxyImpl {
    
    private static final String TAG = "DLServiceProxyImpl";
    private Service mProxyService;
    private DLServicePlugin mRemoteService;
    private Resources mResources;
    
    public DLServiceProxyImpl(Service service) {
        mProxyService = service;
    }
    
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void init(Intent intent) {
     // set the extra's class loader
        intent.setExtrasClassLoader(DLConfigs.sPluginClassloader);

        String packageName = intent.getStringExtra(DLConstants.EXTRA_PACKAGE);
        String clazz = intent.getStringExtra(DLConstants.EXTRA_CLASS);
        LOG.d(TAG, "clazz=" + clazz + " packageName=" + packageName);
        
        DLPluginManager pluginManager = DLPluginManager.getInstance(mProxyService);
        DLPluginPackage pluginPackage = pluginManager.getPackage(packageName);

        try {
            mResources = pluginPackage.resources;
            Class<?> localClass = pluginPackage.classLoader.loadClass(clazz);
            Constructor<?> localConstructor = localClass.getConstructor(new Class[] {});
            Object instance = localConstructor.newInstance(new Object[] {});
            mRemoteService = (DLServicePlugin) instance;
            ((DLServiceAttachable) mProxyService).attach(mRemoteService, pluginManager);
            LOG.d(TAG, "instance = " + instance);
            // attach the proxy activity and plugin package to the
            // mPluginActivity
            mRemoteService.attach(mProxyService, pluginPackage);

            Bundle bundle = new Bundle();
            bundle.putInt(DLConstants.FROM, DLConstants.FROM_EXTERNAL);
            mRemoteService.onCreate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Resources getResources() {
        return mResources;
    }
}
