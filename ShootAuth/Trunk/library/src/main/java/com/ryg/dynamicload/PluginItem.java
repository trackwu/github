package com.ryg.dynamicload;

import android.content.pm.PackageInfo;

/**
 * Created by Jerry on 2016/8/29.
 */
public class PluginItem {
    private PackageInfo packageInfo;
    private String pluginPath;
    private String launcherActivityName;
    private String launcherServiceName;
    private String providerName;

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath) {
        this.pluginPath = pluginPath;
    }

    public String getLauncherActivityName() {
        return launcherActivityName;
    }

    public void setLauncherActivityName(String launcherActivityName) {
        this.launcherActivityName = launcherActivityName;
    }

    public String getLauncherServiceName() {
        return launcherServiceName;
    }

    public void setLauncherServiceName(String launcherServiceName) {
        this.launcherServiceName = launcherServiceName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
