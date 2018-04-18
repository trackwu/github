/*
 * Copyright (C) 2014 singwhatiwanna(任玉刚) <singwhatiwanna@gmail.com>
 *
 * collaborator:zhangjie1980(张杰)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ryg.dynamicload;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.ryg.dynamicload.internal.DLPluginPackage;
import com.ryg.utils.DLConstants;
import com.ryg.utils.LOG;

public class DLBasePluginApplication extends Application implements DLApplicationPlugin {

    public static final String TAG = "DLBasePluginApplication";
    private Application mProxyApplication;
    private DLPluginPackage mPluginPackage;
    protected Application that = this;
    protected int mFrom = DLConstants.FROM_INTERNAL;

    @Override
    public void attach(Application proxyApplication, DLPluginPackage pluginPackage) {
        // TODO Auto-generated method stub
        LOG.d(TAG, TAG + " attach");
        mProxyApplication = proxyApplication;
        mPluginPackage = pluginPackage;
        that = mProxyApplication;
        mFrom = DLConstants.FROM_EXTERNAL;
    }

    protected boolean isInternalCall() {
        return mFrom == DLConstants.FROM_INTERNAL;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        LOG.d(TAG, TAG + " onCreate");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        LOG.d(TAG, TAG + " onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        LOG.d(TAG, TAG + " onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        // TODO Auto-generated method stub
        LOG.d(TAG, TAG + " onTrimMemory");
    }

    @Override
    public String getPackageName() {
        if (mFrom == DLConstants.FROM_INTERNAL) {
            return super.getPackageName();
        } else {
            return mPluginPackage.packageName;
        }
    }

    @Override
    public AssetManager getAssets() {
        if (mFrom == DLConstants.FROM_INTERNAL) {
            return super.getAssets();
        } else {
            return mPluginPackage.assetManager;
        }
    }

    @Override
    public Resources getResources() {
        if (mFrom == DLConstants.FROM_INTERNAL) {
            return super.getResources();
        } else {
            return mPluginPackage.resources;
        }
    }

    @Override
    public Context getApplicationContext() {
        if (mFrom == DLConstants.FROM_INTERNAL) {
            return super.getApplicationContext();
        } else {
            return this;
        }
    }
}
