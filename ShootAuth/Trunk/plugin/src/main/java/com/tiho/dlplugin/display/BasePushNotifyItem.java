package com.tiho.dlplugin.display;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

import com.tiho.base.base.http.HttpManager;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.http.RequestCallback;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.log.bean.OperateItem;
import com.tiho.dlplugin.util.BitmapUtil;
import com.tiho.dlplugin.util.BroadCastUtil;
import com.tiho.dlplugin.util.EnvArgu;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.NetworkUtil;
import com.tiho.dlplugin.util.NumberUtil;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;

import java.io.File;


public abstract class BasePushNotifyItem extends BroadcastReceiver implements PushNotifyItem {

    protected PushNotifyManager notifyManager;
    protected NotificationManager manager;
    protected NotificationCompat.Builder builder;
    protected RemoteViews pushView;
    private Context context;
    private String PUSH_CLICK_ACTION_PREFIX = "me.rui.playpush.push.click.";
    private String PUSH_DELETE_ACTION_PREFIX = "me.rui.playpush.push.delete.";
    private PushMessageBean msg;
    private String clickActiion;//点击时的action
    private String deleteAction;//通知栏清除action

    private Intent clickIntent;
    private Intent deleteIntent;

    private String description;

    private boolean hasSound;


    public BasePushNotifyItem(Context c, PushNotifyManager notifyManager, PushMessageBean msg) {
        super();
        setContext(c);
        setMessage(msg);
        this.notifyManager = notifyManager;
        this.manager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (EnvArgu.getPushLayoutId() != 0) {
            pushView = new RemoteViews(c.getPackageName(), EnvArgu.getPushLayoutId());
        }

        clickActiion = PUSH_CLICK_ACTION_PREFIX + msg.getPushId();
        deleteAction = PUSH_DELETE_ACTION_PREFIX + msg.getPushId();
        clickIntent = new Intent(clickActiion);
        deleteIntent = new Intent(deleteAction);

        description = msg.getSlogan();

        initNotification();

        BroadCastUtil.registerReceiverEvent(context, clickActiion, this);
        BroadCastUtil.registerReceiverEvent(context, deleteAction, this);
    }

    public boolean isHasSound() {
        return hasSound;
    }

    public void setHasSound(boolean hasSound) {
        this.hasSound = hasSound;
    }

    private void initNotification() {
        builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(msg.getTitle());
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(context.getApplicationInfo().icon);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true);
        PendingIntent delInt = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(delInt);
        if (pushView != null) {
            pushView.setTextViewText(EnvArgu.getTitleId(), msg.getTitle());
            pushView.setTextViewText(EnvArgu.getTimeId(), DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
            if (msg.getPushType() == PushMessageBean.TYPE_APP) {
                Bitmap bm = BitmapUtil.getBitmap("download");
                if (bm != null)
                    pushView.setImageViewBitmap(EnvArgu.getDownloadImageId(), bm);
            }
            builder.setContent(pushView);
        }
        resetContentIntent();
    }

    protected void resetContentIntent() {
//		int flags = msg.getPushType() == PushMessageBean.TYPE_AD ? PendingIntent.FLAG_CANCEL_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, clickIntent, flags);
        builder.setContentIntent(pi);
//		pushView.setOnClickPendingIntent(viewId, pendingIntent)
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public PushMessageBean getMessage() {
        return msg;
    }

    @Override
    public void setMessage(PushMessageBean msg) {
        this.msg = msg;
    }

    @Override
    public void doNotify() {

        if (msg.getPushType() == PushMessageBean.TYPE_APP && PackageUtil.isInstalled(msg.getPackName(), context)) {

            LogManager.LogShow(msg.getPackName() + "已经安装，不再推送");

            //应用已经安装的不推送
            LogUploadManager.getInstance(context).addOperateLog(msg.getPushId(), msg.getPushType(), OperateItem.OP_TYPE_INSTALLED, msg.getPackName(), -1, "APP_ALREADY_INSTALLED");
            return;
        }

        File dir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.ICON_DIR);
        final File iconFile = new File(dir, String.valueOf(msg.getIcon().hashCode()));

        if (iconFile.exists()) {
            // 如果有的话，直接显示
            LogManager.LogShow(msg.getPackName() + " iconFile.exists() , path：" + iconFile.getAbsolutePath());
            doNotify(BitmapFactory.decodeFile(iconFile.getAbsolutePath()));
        } else if (!NetworkUtil.isNetworkOk(context)) {
            // 如果网络不可用，用默认图标
            LogManager.LogShow(msg.getPackName() + "!NetworkUtil.isNetworkOk(context)");
            doNotify(BitmapUtil.getBitmap("icon_default"));

        } else {
            HttpManager.getInstance(context).rawGet(msg.getIcon(), notifyManager.getHandler(), new RequestCallback() {

                @Override
                public void run() {
                    byte[] data = (byte[]) getData();
                    try {
                        FileUtil.writeToFile(iconFile, data, false);

                        Bitmap icon = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
//						Bitmap icon = BitmapFactory.decodeByteArray(data, 0, data.length);
                        icon = icon == null ? BitmapUtil.getBitmap("icon_default") : icon;

                        // 下载完了再显示
                        doNotify(icon);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    protected void doNotify(final Bitmap icon) {
        if (pushView != null) {
            pushView.setImageViewBitmap(EnvArgu.getIconId(), icon);
            pushView.setTextViewText(EnvArgu.getDesId(), getDescription());
        } else {
            builder.setLargeIcon(icon);
            builder.setContentText(getDescription());
        }
        LogManager.LogShow(msg.getPackName() + " doNotify");
        if (!isHasSound()) {
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
        }

        LogManager.LogShow("推送通知:" + msg.getPushId());
        try {
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            manager.notify(msg.getPushId().intValue(), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public long getId() {
        return msg.getPushId();
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public void setContext(Context c) {
        this.context = c;
    }

    @Override
    public void remove() {
        manager.cancel(msg.getPushId().intValue());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.contains(PUSH_DELETE_ACTION_PREFIX)) {

            long pushId = NumberUtil.toLong(action.substring(action.lastIndexOf(".") + 1));

            LogManager.LogShow("通知栏清除:" + pushId);

            notifyManager.removeItem(pushId);
        }
    }


    protected boolean isClickAction(Intent intent) {
        return intent.getAction().contains(PUSH_CLICK_ACTION_PREFIX);
    }


}
