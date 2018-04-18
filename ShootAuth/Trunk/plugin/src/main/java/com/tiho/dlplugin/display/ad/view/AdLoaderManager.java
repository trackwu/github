package com.tiho.dlplugin.display.ad.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout.LayoutParams;

public class AdLoaderManager {

    private static WindowManager mWindowManager;

    private static View view;

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    public static void load(final Context context, final String url, final boolean visible, final long id, final int from) {
        //xsc add start 2018-03-22
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // 保险起见，先把原来的内容移除
                closeWindow(context);

                AdDownloadLayout layout = new AdDownloadLayout(context, url, id, from);

                if (!visible) {
                    // 如果不可见，设置成Gone，不占用空间
                    layout.setVisibility(View.GONE);
                }

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.gravity = Gravity.CENTER;
                lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                lp.format = PixelFormat.RGBA_8888;
                lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                lp.width = LayoutParams.WRAP_CONTENT;
                lp.height = LayoutParams.WRAP_CONTENT;

                WindowManager win = getWindowManager(context);

                win.addView(layout, lp);

                view = layout;
            }
        });
        //xsc add end 2018-03-22

    }

    public static void closeWindow(Context c) {
        //xsc add start 2018-03-22
        if (view != null && view.isAttachedToWindow()) {
            getWindowManager(c).removeView(view);
            view = null;
        }
    }
}
