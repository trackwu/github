package com.ryg.dynamicload.internal;

import com.ryg.dynamicload.DLBroadcastReceiverPlugin;

/**
 * Created by Ben on 2015/4/16.
 */
public interface DLBroadcastReceiverAttachable {
    public void attach(DLBroadcastReceiverPlugin remoteBroadcastReceiver, DLPluginManager pluginManager);
}
