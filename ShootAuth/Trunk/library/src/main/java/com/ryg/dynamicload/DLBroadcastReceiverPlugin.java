package com.ryg.dynamicload;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Ben on 2015/4/16.
 */
public interface DLBroadcastReceiverPlugin {

    public void setActions(String[] actions);

    public String[] getActions();

    public void onReceive(Context context, Intent intent);
}
