package com.tiho.dlplugin.bean.silentExport;

/**
 * Created by Jerry on 2016/5/12.
 */
public class SilentAuthInstallBean extends SilentAuthBean {


    private String packagePath;

    public SilentAuthInstallBean(String packageName, String authPackageName, String authKey) {
        super(packageName,authPackageName,authKey);
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }
}
