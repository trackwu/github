package com.tiho.dlplugin.exception;


import android.content.Context;
import android.text.TextUtils;

import com.tiho.base.base.HttpCommonUtil;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.util.TimeUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * @author user
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";
    private static CrashHandler instance;
    //系统默认的UncaughtException处理类
    private UncaughtExceptionHandler mDefaultHandler;
    private Context context;
    private CrashHandler(Context context) {
        this.context=context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public synchronized static CrashHandler getInstance(Context context) {
        if (instance == null) {
            instance = new CrashHandler(context);
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init() {
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);
        mDefaultHandler.uncaughtException(thread, ex);
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 异常
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
        ex.printStackTrace();
        //上传异常日志
        uploadCrashInfo(ex);
        return true;
    }

    /**
     * 上传异常日志
     *
     * @param ex 异常
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private void uploadCrashInfo(Throwable ex) {
        Map<String, String> map = HttpCommonUtil.getInstance().getCommonInfo();
        String result;
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        result = writer.toString();
        if (!TextUtils.isEmpty(result))
            map.put("crash", result);
        else {
            map.put("crash", "unknown");
        }
        map.put("causeTime", TimeUtil.getNowTime());
        LogUploadManager.getInstance(context).uploadCrashLog(map);

    }
}
