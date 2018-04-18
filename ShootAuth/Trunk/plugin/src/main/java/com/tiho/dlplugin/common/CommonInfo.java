package com.tiho.dlplugin.common;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLPluginPackage;
import com.tiho.base.base.HttpCommonUtil;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.Global;

public class CommonInfo {

    private static CommonInfo instance;
    private HttpCommonUtil commonUtil;
    private String mPluginVer;
    private Context mContext;
    private String mPluginpkg = "";

    private CommonInfo(Context context) {
        commonUtil = HttpCommonUtil.getInstance();
        commonUtil.init(context);
        mContext = context;
        try {
            getPluginPackageInfo();
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
    }

    public synchronized static CommonInfo getInstance(Context context) {
        if (instance == null)
            instance = new CommonInfo(context);

        return instance;
    }

    public String getCommonkey() {
        return commonUtil.commonkey();
    }

    public String getXplayAgent() {
        return commonUtil.xPlayAgent();
    }

    public String getPluginVer() {
        return mPluginVer;
    }

    public void setPluginVer(String pushVer) {
        this.mPluginVer = pushVer;
    }

    public String getPluginName() {
        return mPluginpkg;
    }

    public String getImsi() {
        return commonUtil.getImsi();
    }


    private void getPluginPackageInfo() {
        LogManager.LogShow("getPluginPackageInfo " + mContext.getPackageManager());
        DLPluginPackage dlp = DLPluginManager.getInstance(mContext).getPackage(Global.sPackageName);
        PackageInfo packageInfo = dlp.packageInfo;
        mPluginVer = packageInfo.versionCode + "";
        mPluginpkg = packageInfo.packageName;
        LogManager.LogShow("getPluginPackageInfo packageName = " + packageInfo.packageName + ", versionCode = " + packageInfo.versionCode);
    }
}
