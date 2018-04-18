package com.tiho.dlplugin.task.silentExport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.tiho.dlplugin.bean.silentExport.SilentAuthInstallBean;
import com.tiho.dlplugin.bean.silentExport.SilentAuthUninstallBean;

/**
 *
 * Created by Jerry on 2016/5/13.
 */
public class SilentAuthReceiver extends BroadcastReceiver {

    public static final String ACTION_SILENT_INSTALL = "com.timo.push.silent_install";
    public static final String ACTION_SILENT_UNINSTALL = "com.timo.push.silent_uninstall";
    public static final String EXTRA_AUTH_PACKAGE = "com.timo.push.extra.AUTH_PACKAGE";
    public static final String EXTRA_AUTH_KEY = "com.timo.push.extra.AUTH_KEY";
    public static final String EXTRA_PACKAGE = "com.timo.push.extra.PACKAGE";
    public static final String EXTRA_PACKAGE_PATH = "com.timo.push.extra.PACKAGE_PATH";
    public final Handler mHandler;
    public final Context mContext;

    public SilentAuthReceiver(Handler mHandler, Context mContext) {
        super();
        this.mHandler = mHandler;
        this.mContext = mContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, ACTION_SILENT_INSTALL)) {
            SilentAuthInstallBean installBean = new SilentAuthInstallBean(
                    intent.getStringExtra(EXTRA_PACKAGE),
                    intent.getStringExtra(EXTRA_AUTH_PACKAGE),
                    intent.getStringExtra(EXTRA_AUTH_KEY));
            installBean.setPackagePath(intent.getStringExtra(EXTRA_PACKAGE_PATH));
            mHandler.post(new SilentAuthTask(mHandler,mContext,installBean));
        } else if (TextUtils.equals(action, ACTION_SILENT_UNINSTALL)) {
            SilentAuthUninstallBean uninstallBean = new SilentAuthUninstallBean(
                    intent.getStringExtra(EXTRA_PACKAGE),
                    intent.getStringExtra(EXTRA_AUTH_PACKAGE),
                    intent.getStringExtra(EXTRA_AUTH_KEY));
            mHandler.post(new SilentAuthTask(mHandler, mContext, uninstallBean));
        }
    }

}