package com.tiho.dlplugin.display;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.tiho.dlplugin.util.BroadCastUtil;
import com.tiho.dlplugin.util.EnvArgu;

public class NotifyBuilder {

    public static void build(Context c, String title, String desc, Bitmap icon, int noteId, BroadcastReceiver receiver) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
        if(TextUtils.isEmpty(title))
            title="";
        builder.setContentTitle(title);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(c.getApplicationInfo().icon);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true);
        RemoteViews pushView;
        if (EnvArgu.getPushLayoutId() != 0) {
            pushView = new RemoteViews(c.getPackageName(), EnvArgu.getPushLayoutId());
            pushView.setTextViewText(EnvArgu.getTitleId(), title);
            if(icon!=null)
                pushView.setImageViewBitmap(EnvArgu.getIconId(), icon);
            if (desc != null)
                pushView.setTextViewText(EnvArgu.getDesId(), desc);
            builder.setContent(pushView);
        }else{
            if(icon!=null)
                builder.setLargeIcon(icon);
            if (desc != null)
                builder.setContentText(desc);
        }
        String clickAction = "me.rui.playpush.push.click.silent" + noteId;
        PendingIntent pi = PendingIntent.getBroadcast(c, 0, new Intent(clickAction), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        BroadCastUtil.registerReceiverEvent(c, clickAction, receiver);
        Notification notification=builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        nm.notify(noteId, notification);
    }


    public static void cancel(Context c, int id) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);
    }

}
