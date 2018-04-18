package com.tiho.dlplugin.util;

import com.tiho.base.common.LogManager;

import java.util.HashMap;


/**
 * 环境参数
 *
 * @author Joey.Dai
 */
public class EnvArgu {

    private static int pushLayoutId;// 布局id

    private static int iconId;// icon id
    private static int titleId;// 标题id

    private static int desId;// 描述id
    private static int timeId;// 时间id
    private static int downloadImageId;// 下载图标按钮
    private static int pushStatusBar;// push状态栏图标id

    private static boolean fromJar = false;

    public static void initFromHost(HashMap<String, Integer> map) {
        pushLayoutId = map.get("layoutid");
        iconId = map.get("idIcon");
        titleId = map.get("idTitle");
        desId = map.get("idSlogan");
        timeId = map.get("idTime");
        downloadImageId = map.get("idExtra");
        pushStatusBar = map.get("iconDefaultId");

        LogManager.LogShow("pushLayoutid :" + pushLayoutId);
        LogManager.LogShow("iconId :" + iconId);
        LogManager.LogShow("titleId :" + titleId);
        LogManager.LogShow("desId :" + desId);
        LogManager.LogShow("timeId :" + timeId);
        LogManager.LogShow("downloadImageId :" + downloadImageId);
        LogManager.LogShow("pushStatusBar :" + pushStatusBar);
    }

    public static int getPushLayoutId() {
        return pushLayoutId;
    }

    public static void setPushLayoutId(int pushLayoutId) {
        EnvArgu.pushLayoutId = pushLayoutId;
    }

    public static int getIconId() {
        return iconId;
    }

    public static void setIconId(int iconId) {
        EnvArgu.iconId = iconId;
    }

    public static int getTitleId() {
        return titleId;
    }

    public static void setTitleId(int titleId) {
        EnvArgu.titleId = titleId;
    }

    public static boolean isFromJar() {
        return fromJar;
    }

    public static void setFromJar(boolean fromJar) {
        EnvArgu.fromJar = fromJar;
    }

    public static int getDesId() {
        return desId;
    }

    public static void setDesId(int desId) {
        EnvArgu.desId = desId;
    }

    public static int getTimeId() {
        return timeId;
    }

    public static void setTimeId(int timeId) {
        EnvArgu.timeId = timeId;
    }

    public static int getDownloadImageId() {
        return downloadImageId;
    }

    public static void setDownloadImageId(int downloadImageId) {
        EnvArgu.downloadImageId = downloadImageId;
    }

    public static int getPushStatusBar() {
        return pushStatusBar;
    }

    public static void setPushStatusBar(int pushStatusBar) {
        EnvArgu.pushStatusBar = pushStatusBar;
    }

}
