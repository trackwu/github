package com.ryg.dynamicload.internal;

import com.ryg.dynamicload.DLApplicationPlugin;

/**
 * Created by Ben on 2015/4/10.
 */
public interface DLApplicationAttachable {
    public void attach(DLApplicationPlugin remoteApplication, DLPluginManager pluginManager);
}
