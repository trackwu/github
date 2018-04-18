package com.tiho.dlplugin.update;

import android.content.Context;
import android.os.Handler;

public class UpdateMethod {


    public void checkversion(final Context context, final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                new HostApkUpdate(context).checkVersion(handler);
                new PushFileUpdate(context).checkversion(handler);
                new DlCoreFileUpdate(context).checkVersion(handler);
            }
        }).start();

    }

}
