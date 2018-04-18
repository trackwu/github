package com.tiho.dlplugin.task.silentExport;

/**
 * Created by Jerry on 2016/5/12.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.Global;
import com.tiho.dlplugin.bean.silentExport.SilentAuthBean;
import com.tiho.dlplugin.bean.silentExport.SilentAuthInstallBean;
import com.tiho.dlplugin.bean.silentExport.SilentAuthUninstallBean;
import com.tiho.dlplugin.install.PackageUtilsEx;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.BaseTask;
import com.tiho.dlplugin.util.SilentInstall;

import java.io.File;
import java.util.List;

public class SilentAuthTask extends BaseTask {

    public static final String ACTION_SILENT_INSTALL_ACK = "com.timo.push.silent_install_ack";
    public static final String ACTION_SILENT_UNINSTALL_ACK = "com.timo.push.silent_uninstall_ack";
    public static final String EXTRA_SILENT_RESULT = "com.timo.push.extra.RESULT";
    public static final String EXTRA_SILENT_RESULT_INFO = "com.timo.push.extra.RESULT_INFO";
    public static final String EXTRA_SILENT_RESULT_PACKAGE = "com.timo.push.extra.RESULT_PACKAGE";
    public static final String EXTRA_SILENT_RESULT_HOST = "com.timo.push.extra.RESULT_HOST";
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAILED = -1;
    public static final int RESULT_ILLEGAL_AUTH = -2;
    public static final int RESULT_NOT_SUPPORT = -3;
    public static final int RESULT_ILLEGAL_ARGUMENT = -4;
    private final SilentAuthBean mSilentBean;

    public SilentAuthTask(Handler handler, Context context, SilentAuthBean silentBean) {
        super(handler, context);
        mSilentBean = silentBean;
    }

    @Override
    protected void doTask() {
        if (mSilentBean == null) {
            LogManager.LogShow("SilentBean is null");
        } else {
            String info;
            int result;
            if (TextUtils.isEmpty(mSilentBean.getAuthKey())
                    || TextUtils.isEmpty(mSilentBean.getAuthPackageName())
                    || TextUtils.isEmpty(mSilentBean.getPackageName())) {
                result=RESULT_ILLEGAL_ARGUMENT;
                info ="Silent auth illegal argument";
                sendResult(mSilentBean, result, info);
                return;
            }
            if (!checkSilentKey(mSilentBean.getAuthKey())) {
                result = RESULT_ILLEGAL_AUTH;
                info = "auth key is illegal!";
                sendResult(mSilentBean, result, info);
                return;
            }
            if (!SilentInstall.bgInstallSupport(context)) {
                result = RESULT_NOT_SUPPORT;
                info = "silent install not support!";
                sendResult(mSilentBean, result, info);
                return;
            }
            if (mSilentBean instanceof SilentAuthInstallBean) {
                SilentAuthInstallBean installBean = (SilentAuthInstallBean) mSilentBean;
                String packagePath=installBean.getPackagePath();
                if(TextUtils.isEmpty(packagePath)||!new File(packagePath).exists()){
                    result=RESULT_ILLEGAL_ARGUMENT;
                    info ="Silent auth illegal argument";
                    sendResult(mSilentBean, result, info);
                    return;
                }
                int ret = PackageUtilsEx.installSilent(context, packagePath);
                info = "silent install ret = " + ret;
                if (ret == PackageUtilsEx.INSTALL_SUCCEEDED) {
                    result=RESULT_SUCCESS;
                    sendResult(mSilentBean,result,info);
                } else {
                    result=RESULT_FAILED;
                    sendResult(mSilentBean, result, info);
                }
            } else if (mSilentBean instanceof SilentAuthUninstallBean) {
                SilentAuthUninstallBean silentUninstallBean = (SilentAuthUninstallBean) mSilentBean;
                int ret = PackageUtilsEx.uninstallSilent(context, silentUninstallBean.getPackageName());
                info = "silent install is ret = " + ret;
                if (ret == PackageUtilsEx.DELETE_SUCCEEDED) {
                    result=RESULT_SUCCESS;
                    sendResult(mSilentBean, result, info);
                } else {
                    result=RESULT_FAILED;
                    sendResult(mSilentBean, result, info);
                }
            }
        }
    }

    @Override
    protected void afterTask(Handler handler) {
        // do nothing
    }

    private boolean checkSilentKey(String authKey) {
        boolean isAuth = false;
        List<String> authList = SilentAuthUtil.getAuthList(context);
        for (String value : authList) {
            if (TextUtils.equals(value, authKey)) {
                isAuth = true;
                break;
            }
        }
        return isAuth;
    }

    private void sendResult(SilentAuthBean authBean, int result,String info) {
        Intent intent;
        if (authBean instanceof SilentAuthInstallBean) {
            intent = new Intent(ACTION_SILENT_INSTALL_ACK);
        } else if(authBean instanceof SilentAuthUninstallBean) {
            intent = new Intent(ACTION_SILENT_UNINSTALL_ACK);
        }else{
            LogManager.LogShow("SilentAuthBean type not support!");
            return;
        }
        if(!TextUtils.isEmpty(Global.sHostPackageName))
            intent.putExtra(EXTRA_SILENT_RESULT_HOST, Global.sHostPackageName);
        if(!TextUtils.isEmpty(authBean.getPackageName())){
            intent.putExtra(EXTRA_SILENT_RESULT_PACKAGE, authBean.getPackageName());
            LogManager.LogShow("Silent auth package = " + authBean.getPackageName()+"result = " + result);
        }
        intent.putExtra(EXTRA_SILENT_RESULT, result);
        if (!TextUtils.isEmpty(info))
            intent.putExtra(EXTRA_SILENT_RESULT_INFO, info);
        if(!TextUtils.isEmpty(authBean.getAuthPackageName()))
            intent.setPackage(authBean.getAuthPackageName());
        context.sendBroadcast(intent);
        LogUploadManager.getInstance(context).uploadSilentAuthLog(authBean,result,info);
    }

}
