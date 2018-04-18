package com.ryg.dynamicload.internal;

import com.ryg.dynamicload.DLContentProviderPlugin;

/**
 * Created by Ben on 2015/4/13.
 */
public interface DLContentProviderAttachable {

    public void attach(DLContentProviderPlugin remoteContentProvider, DLPluginManager pluginManager);
}
