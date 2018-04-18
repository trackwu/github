package com.tiho.dlplugin.bean.silentExport;

/**
 * Created by Jerry on 2016/5/12.
 */
public abstract class SilentAuthBean {

    private String packageName;
    private String authPackageName;
    private String authKey;

    public SilentAuthBean(String packageName, String authPackageName, String authKey) {
        this.packageName=packageName;
        this.authPackageName = authPackageName;
        this.authKey = authKey;
    }

    /**
     * @return 需要安装或卸载的包名
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @return 第3方调用的包名
     */
    public String getAuthPackageName() {
        return authPackageName;
    }

    /**
     * @return 授权全给第3方的key
     */
    public String getAuthKey() {
        return authKey;
    }
}
