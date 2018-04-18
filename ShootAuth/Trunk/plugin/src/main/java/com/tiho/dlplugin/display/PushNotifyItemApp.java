package com.tiho.dlplugin.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushConfigBean;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.export.ShortcutActivity;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.log.bean.OperateItem;
import com.tiho.dlplugin.observer.download.DownloadProgress;
import com.tiho.dlplugin.observer.download.DownloadStat;
import com.tiho.dlplugin.observer.download.FileDownloader;
import com.tiho.dlplugin.task.activate.ActivatedRecord;
import com.tiho.dlplugin.util.NetworkUtil;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.StringUtils;

/**
 * 应用的push
 *
 * @author Joey.Dai
 */
public class PushNotifyItemApp extends BasePushNotifyItem implements DownloadProgress, Runnable {

    private FileDownloader downloader;

    private DownloadStat stat;

    private Object lock;
    private boolean downloading = false;

    public PushNotifyItemApp(Context c, PushNotifyManager notifyManager, PushMessageBean msg) {
        super(c, notifyManager, msg);
        this.downloader = new FileDownloader();
        this.downloader.setDownloadProgress(this);

        stat = DownloadStat.initFrom(getContext(), msg, false);

        lock = new Object();
        NetworkUtil.registerNetworkRecoverEvent(c, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });
    }

    private void updateDownloadProgress(DownloadStat stat) {
        int rate = (int) (stat.getCurrent() * 1.0 / stat.getTotal() * 100);

        String prog = rate + "%";
        LogManager.LogShow(stat.getPackageName() + "下载进度:" + prog);

        setDescription(prog);
        setHasSound(false);

        if (rate == 100) {
            resetContentIntent();
            downloading = false;
        }

        doNotify();
    }

    @Override
    public void downloaded(long total, long now) {
        updateDownloadProgress(stat);
    }

    private Pair<String, String[]> extraHost(Context context) {
        PushConfigDAO dao = DAOFactory.getConfigDAO(context);
        PushConfigBean config = dao.getConfigByKey(PushConfigDAO.TYPE_CONFIG, PushConfigBean.class);

        String ips = config.getDownIp();
        String[] ipList = StringUtils.isEmpty(ips) ? new String[0] : ips.split(",");

        return Pair.create(config.getDownHost(), ipList);

    }

    //下载APK
    @Override
    public void run() {
        try {
            Context context = getContext();
            PushMessageBean msg = getMessage();

            if (!NetworkUtil.isNetworkOk(context)) {
                LogUploadManager.getInstance(context).addOperateLog(msg.getPushId(), PushMessageBean.TYPE_APP, OperateItem.OP_TYPE_DOWNLOAD_NO_NETWORK, msg.getPackName(), -1, "NETWORK_UNAVAILABLE");
            } else {

                downloader.startDownload(getContext(), stat, extraHost(context));

                LogManager.LogShow("下载结果:" + stat.isComplete());
                if (stat.isComplete()) {

                    // 下载完成后，调用系统安装界面
                    stat.renameToFinalName();
                    ShortcutActivity.gotoShortcutActivity(getContext(), msg.getPushId(), false);
                }

            }


        } catch (Exception e) {
            LogManager.LogShow(e);
        }
    }


    /**
     * 消息点击事件
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);// 必须要加

        // 普通应用push点击后就开始下载
        LogManager.LogShow("应用消息点击:" + isClickAction(intent));
        if (isClickAction(intent)) {

            LogManager.LogShow("应用消息点击:" + intent.getAction());

            if (stat.isComplete()) {
                try {
                    LogManager.LogShow("应用下载完成:准备安装");
                    if (PackageUtil.isInstalled(stat.getPackageName(), context)) {
                        LogManager.LogShow(stat.getPackageName() + "已经安装，不再提示安装");

                        if (!ActivatedRecord.getInstance(context).activated(stat.getPackageName())) {


                            ActivatedRecord.getInstance(context).addRecord(context, stat.getPackageName(), System.currentTimeMillis());
                            // 激活日志
                            LogUploadManager.getInstance(context).addOperateLog(stat.getPushId(), PushMessageBean.TYPE_APP, OperateItem.OP_TYPE_ACTIVATED, stat.getPackageName(), 2, "APP_ACTIVATED_BY_USER");
                        }

                        PackageUtil.openApp(context, stat.getPackageName());

                    } else {

                        LogManager.LogShow(stat.getPackageName() + "未安装，走起 , msg=" + getMessage());
                        ShortcutActivity.gotoShortcutActivity(getContext(), getMessage().getPushId(), false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (!downloading) {
                downloading = true;

                new Thread(this).start();
                updateDownloadProgress(stat);

            }
        }

    }

}
