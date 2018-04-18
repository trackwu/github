package com.ryg.dynamicload;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.ryg.dynamicload.internal.DLContentProviderAttachable;
import com.ryg.dynamicload.internal.DLContentProviderProxyImpl;
import com.ryg.dynamicload.internal.DLPluginManager;

/**
 * Created by Ben on 2015/4/13.
 */
public class DLProxyContentProvider extends ContentProvider implements DLContentProviderAttachable {

    public static DLProxyContentProvider instance;
    private DLContentProviderProxyImpl mImpl = new DLContentProviderProxyImpl(this);
    private DLContentProviderPlugin mRemoteContentProvider;

    @Override
    public boolean onCreate() {
        instance = this;
        return true;
    }

    public void onCreatePlugin(String packageName, String clazz) {
        if (mRemoteContentProvider == null) {
            mImpl.init(packageName, clazz);
        } else {
            mRemoteContentProvider.onCreate();
        }
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return mRemoteContentProvider.query(uri, strings, s, strings2, s2);
    }

    @Override
    public String getType(Uri uri) {
        return mRemoteContentProvider.getType(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return mRemoteContentProvider.insert(uri, contentValues);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        return mRemoteContentProvider.bulkInsert(uri, values);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return mRemoteContentProvider.delete(uri, s, strings);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return mRemoteContentProvider.update(uri, contentValues, s, strings);
    }

    @Override
    public void attach(DLContentProviderPlugin remoteContentProvider, DLPluginManager pluginManager) {
        mRemoteContentProvider = remoteContentProvider;
    }

}
