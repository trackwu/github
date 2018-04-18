package com.tiho.dlplugin.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import com.tiho.base.base.md.Md;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.util.PackageUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Dl插件框架自更新
 * Created by Jerry on 2016/9/9.
 */
public class DlCoreFileUpdate extends DecodeFileUpdate {
    private static final String PACKAGE_NAME = "com.shoot.dlcore";
    private static final String CONFIG_PATH_NAME = "configShootPath";
    private static final String PATH_KEY = "dlCorePath";
    private Context mContext;
    private File saveFile;

    public DlCoreFileUpdate(Context context) {
        mContext = context;
        String path = getPlugPath(context);
        if (!TextUtils.isEmpty(path))
            saveFile = new File(path);
        else
            LogManager.LogShow("checkVersion saveFile is empty ");
    }

    private String getPlugPath(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_PATH_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PATH_KEY, null);
    }

    public void checkVersion(final Handler mHandler) {
        if (saveFile != null) {
            LogManager.LogShow("checkVersion pkgName = " + PACKAGE_NAME);
            String commonkey = CommonInfo.getInstance(mContext).getCommonkey();
            String xplayagent = CommonInfo.getInstance(mContext).getXplayAgent();
            Md md = new Md(mContext);
            ArrayList<Md.MdDownloadData> lists = checkParam(md);
            md.mdDownload(lists, new Md.IDownLoadCB() {
                @Override
                public void callback(int result, Md.MdCbData data, Object obj) {
                    if (result != Md.MDLR_STA_PROGRESS) {
                        LogManager.LogShow("result = " + result);
                        if (result == Md.MDRES_REALDL_OK) {
                            LogManager.LogShow(PACKAGE_NAME + " update complete");
                            try {
                                String downloadPath = (String) obj;
                                File downloadFile = new File(downloadPath);
                                if (saveFile.exists()) {
                                    if (!saveFile.delete()) {
                                        LogManager.LogShow("delete file failed " + saveFile.getPath());
                                    }
                                }
                                File encodeTmpFile = File.createTempFile(saveFile.getName(), null, saveFile.getParentFile());
                                encodeFile(downloadFile, encodeTmpFile);
                                if (!encodeTmpFile.renameTo(saveFile)) {
                                    LogManager.LogShow("rename failed " + saveFile.getPath());
                                } else {
                                    LogManager.LogShow("rename success " + saveFile.getPath());
                                }
                                if (!downloadFile.delete()) {
                                    LogManager.LogShow("Failed to delete " + downloadFile.getPath());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (result == Md.MDLR_STA_NOUPDATA) {
                            LogManager.LogShow(PACKAGE_NAME + " is last version");
                        }
                    }
                }
            }, commonkey, xplayagent);
        }
    }


    private ArrayList<Md.MdDownloadData> checkParam(Md md) {
        ArrayList<Md.MdDownloadData> lists = new ArrayList<>();
        Md.MdDownloadData item = new Md.MdDownloadData();
        md.setRootPath(mContext.getFilesDir().getPath());
        item.appid = PACKAGE_NAME;
        item.appver = getVersionCode(saveFile);
        item.type = 0;
        lists.add(item);
        return lists;
    }

    private int getVersionCode(File saveFile) {
        int versionCode = 0;
        try {
            if (saveFile.exists()) {
                File tmpFile = File.createTempFile(saveFile.getName(), null, saveFile.getParentFile());
                decodeFile(saveFile, tmpFile);
                versionCode = PackageUtil.parsePackageVersionCode(mContext, tmpFile.getPath());
                if (!tmpFile.delete()) {
                    LogManager.LogShow("Failed to delete " + tmpFile.getPath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


}
