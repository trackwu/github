package com.ryg.dynamicload;

import android.app.Application;

import com.ryg.dynamicload.internal.DLPluginPackage;

/**
 * Created by Administrator on 2015/4/10.
 */
public interface DLApplicationPlugin {
    public void onCreate();
    public void attach(Application proxyApplication, DLPluginPackage pluginPackage);
}
