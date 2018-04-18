package com.ryg.dynamicload;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.ryg.dynamicload.internal.DLPluginPackage;
import com.ryg.utils.LOG;

/**
 * Created by Ben on 2015/4/14.
 */
public class DLBasePluginContentProvider extends ContentProvider implements DLContentProviderPlugin {

    public static final String TAG = "DLBasePluginApplication";
    private ContentProvider mProxyContentProvider;
    private DLPluginPackage mPluginPackage;
    protected ContentProvider that = this;

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){
        return 0;
    }
    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public void attach(ContentProvider proxyContentProvider, DLPluginPackage pluginPackage) {
        LOG.d(TAG, TAG + " attach");
        mProxyContentProvider = proxyContentProvider;
        mPluginPackage = pluginPackage;
        that = mProxyContentProvider;
    }
}
