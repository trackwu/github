package com.ryg.dynamicload;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.ryg.dynamicload.internal.DLPluginPackage;

/**
 * Created by Ben on 2015/4/13.
 */
public interface DLContentProviderPlugin {
    public boolean onCreate();

    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2);

    public String getType(Uri uri);

    public Uri insert(Uri uri, ContentValues contentValues);

    public int delete(Uri uri, String s, String[] strings);

    public int update(Uri uri, ContentValues contentValues, String s, String[] strings);

    public int bulkInsert(Uri uri, ContentValues[] values);

    public void attach(ContentProvider proxyContentProvider, DLPluginPackage pluginPackage);
}
