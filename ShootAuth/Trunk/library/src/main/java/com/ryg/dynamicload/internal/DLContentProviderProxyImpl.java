package com.ryg.dynamicload.internal;

import android.app.Application;
import android.content.ContentProvider;
import com.ryg.utils.LOG;

import com.ryg.dynamicload.DLApplicationPlugin;
import com.ryg.dynamicload.DLContentProviderPlugin;

import java.lang.reflect.Constructor;

/**
 * Created by Ben on 2015/4/13.
 */
public class DLContentProviderProxyImpl {

    private static final String TAG = "DLContentProviderImpl";
    private ContentProvider mProxyContentProvider;
    private DLContentProviderPlugin mRemoteContentProvider;

    public DLContentProviderProxyImpl(ContentProvider contentProvider) {
        mProxyContentProvider = contentProvider;
    }

    public void init(String packageName, String clazz) {
        LOG.d(TAG, "clazz=" + clazz + " packageName=" + packageName);
        DLPluginManager pluginManager = DLPluginManager.getInstance(mProxyContentProvider.getContext());
        DLPluginPackage pluginPackage = pluginManager.getPackage(packageName);

        try {
            Class<?> localClass = pluginPackage.classLoader.loadClass(clazz);
            Constructor<?> localConstructor = localClass.getConstructor(new Class[]{});
            Object instance = localConstructor.newInstance(new Object[]{});
            mRemoteContentProvider = (DLContentProviderPlugin) instance;
            ((DLContentProviderAttachable) mProxyContentProvider).attach(mRemoteContentProvider, pluginManager);
            LOG.d(TAG, "instance = " + instance);
            // attach the proxy application and plugin package to the
            // mPluginActivity
            mRemoteContentProvider.attach(mProxyContentProvider, pluginPackage);

            mRemoteContentProvider.onCreate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
