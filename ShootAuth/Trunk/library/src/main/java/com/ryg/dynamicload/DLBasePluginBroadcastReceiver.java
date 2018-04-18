package com.ryg.dynamicload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Ben on 2015/4/17.
 */
public class DLBasePluginBroadcastReceiver extends BroadcastReceiver implements DLBroadcastReceiverPlugin {

    private String[] actions;

    @Override
    public void setActions(String[] actions) {
        {
            this.actions = actions;
        }
    }

    @Override
    public String[] getActions() {
        return actions;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }

}
