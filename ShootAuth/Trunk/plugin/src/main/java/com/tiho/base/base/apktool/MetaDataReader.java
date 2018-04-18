package com.tiho.base.base.apktool;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by huan on 2016/1/6.
 */
public class MetaDataReader implements DataReader {

    private ApplicationInfo mApplicationInfo;

    MetaDataReader(PackageManager packageManager, String path) {
        mApplicationInfo = packageManager.getPackageArchiveInfo(path, PackageManager.GET_META_DATA).applicationInfo;
    }

    @Override
    public String getData(String key) {
        if(mApplicationInfo!=null&&mApplicationInfo.metaData!=null)
            return mApplicationInfo.metaData.getString(key);
        else{
            return null;
        }
    }
}
