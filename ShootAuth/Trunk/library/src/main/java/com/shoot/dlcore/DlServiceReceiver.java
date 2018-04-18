/**
 * class name： PushReceiver
 * DATE: 2013-04-13
 * DES: 接收到开机等广播时开启push服务
 * Copyright (c) 2013 skymobi
 * All rights reserved.
 *
 * @version 1.00
 * @author hanfeng)
 */
package com.shoot.dlcore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.ryg.utils.LOG;

import java.util.Timer;
import java.util.TimerTask;


public class DlServiceReceiver extends BroadcastReceiver {

    final static String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    final static String USER_PRESENT = "android.intent.action.USER_PRESENT";
    final static String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    final static String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    private static final String TAG = DlServiceReceiver.class.getName();
    Context mContext;
    Timer t;

    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.i(TAG, "context = " + context + ",intent.getAction() = " + intent.getAction());
        if (TextUtils.equals(intent.getAction(),CONNECTIVITY_CHANGE)
                || TextUtils.equals(intent.getAction(),USER_PRESENT)
                || TextUtils.equals(intent.getAction(),PACKAGE_ADDED)
                || TextUtils.equals(intent.getAction(),PACKAGE_REMOVED)) {
            PluginHelper.getInstance(context.getApplicationContext()).startPush();
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LOG.i(TAG, "BOOT COMPLETED");
            mContext = context.getApplicationContext();
            t = new Timer();
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    PluginHelper.getInstance(mContext).startPush();
                    t.cancel();
                }
            };
            int delay = 600000;
            t.schedule(task, delay);
        }
    }

}
