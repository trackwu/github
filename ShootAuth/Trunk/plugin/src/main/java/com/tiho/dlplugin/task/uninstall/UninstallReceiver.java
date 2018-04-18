package com.tiho.dlplugin.task.uninstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.display.NotifyBuilder;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.util.PackageUtil;

/**
 * 应用卸载广播接收器.
 * 应用被卸载后会发送action = android.intent.action.PACKAGE_REMOVED的广播，
 * 我们程序接收后，记录到卸载记录的文件中。
 * <p/>
 * 应用安装广播接收器
 *
 * @author Joey.Dai
 */
public class UninstallReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        // 被卸载的应用包名或者安装的包名
        String packName = intent.getData().getSchemeSpecificPart();

        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {

            if (!PackageUtil.isInstalled(packName, context)) {
                LogManager.LogShow(packName + "加入卸载列表");
                UnInstallList.add(context, packName);
            }
            LogUploadManager.getInstance(context).addUninstallLog(packName, 1, "DELETE_DETECTED_BY_BROADCASTRECEIVER");

        } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            //如果playapp安装成功，就把通知栏取消掉
            if (isPlayApp(packName)) {
                NotifyBuilder.cancel(context, packName.hashCode());
            }

            LogUploadManager.getInstance(context).addSilentInstallLog(1, "PACKAGE_INSTALL_DETECTED_BY_BROADCASTRECEIVER", packName, 0, "P", 0);
        }
    }


    private boolean isPlayApp(String pack) {
        return "me.pplay.playapp".equals(pack) || "me.powerplay.playapp".equals(pack);
    }


}
