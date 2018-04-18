package com.tiho.base.base.manufactory.aidl;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.tiho.base.base.apktool.ApkTool;
import com.tiho.base.base.apktool.DataReader;
import com.tiho.base.base.des.ManufactoryDES;
import com.tiho.base.common.CfgIni;
import com.tiho.base.common.LogManager;

import java.io.File;


/**
 * Created by Ben on 2016/1/27.
 */
public class PlaymeIdHelper {
    private static final String HSMAN = "hsman";
    private static final String HSTYPE = "hstype";

    private static PlaymeIdHelper instance;
    private Context mContext;

    private PlaymeIdHelper(Context context) {
        mContext = context;
    }

    public static PlaymeIdHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PlaymeIdHelper(context);
        }
        return instance;
    }

    public String getPlaymeIdPlaintext() {
        DataReader dataReader = ApkTool.getReader(mContext.getPackageManager(), mContext.getPackageCodePath());
        String hsman = dataReader.getData(HSMAN);
        String hstype = dataReader.getData(HSTYPE);

        if (!TextUtils.isEmpty(hsman) && !TextUtils.isEmpty(hstype)) {
            return new StringBuffer(hsman).append("-").append(hstype).toString();
        }

        dataReader = ApkTool.getReader(new File(mContext.getPackageCodePath()));
        hsman = dataReader.getData(HSMAN);
        hstype = dataReader.getData(HSTYPE);
        if (!TextUtils.isEmpty(hsman) && !TextUtils.isEmpty(hstype)) {
            return new StringBuffer(hsman).append("-").append(hstype).toString();
        }

        String playmeId = ManuUtil.getInstance(mContext).getPlaymeID(CfgIni.getInstance().getValue("common", "hsman", Build.MANUFACTURER),
                CfgIni.getInstance().getValue("common", "hstype", Build.MODEL),
                "",
                "", null);
        playmeId = CfgIni.getInstance().getValue("common", "playmeid", playmeId);
        if (TextUtils.isEmpty(playmeId)) {
            playmeId = ManuUtil.defaultId;
        }
        String[] result = ManufactoryDES.decode(playmeId);
        if (result.length == 3) {
            hsman = result[1];
            hstype = result[2];
            return new StringBuffer(hsman).append("-").append(hstype).toString();
        } else {
            return new StringBuffer("android").append("-").append("playmet").toString();
        }
    }

    public String getPlaymeIdCiphertext() {
        try {
            //xsc add start 2018-03-23
//            String manu = Build.MANUFACTURER;
//            String model = Build.MODEL;
//            if (!manu.equals("unknown") && !model.equals("unknown")) {
//                return ManufactoryDES.encode(manu, model);
//            }
            //xsc add end 2018-03-23

            //从manifest文件中读取“hsman”和“hstype”并加密后返回
            DataReader dataReader = ApkTool.getReader(mContext.getPackageManager(), mContext.getPackageCodePath());
            String hsman = dataReader.getData(HSMAN);
            String hstype = dataReader.getData(HSTYPE);

            if (!TextUtils.isEmpty(hsman) && !TextUtils.isEmpty(hstype)) {
                return ManufactoryDES.encode(hsman, hstype);
            }
            // getPackageCodePath()---> /system/priv-app/EdlPush/EdlPush.apk
            LogManager.LogShow("getPackageCodePath = " + mContext.getPackageCodePath());
            dataReader = ApkTool.getReader(new File(mContext.getPackageCodePath()));
            hsman = dataReader.getData(HSMAN);
            hstype = dataReader.getData(HSTYPE);
            if (!TextUtils.isEmpty(hsman) && !TextUtils.isEmpty(hstype)) {
                LogManager.LogShow("通过文件读取数据并返回");
                return ManufactoryDES.encode(hsman, hstype);
            }
        }catch (Exception e){
            LogManager.LogShow("getPlaymeIdCiphertext e=" + e);
        }
        // manu1=alps, type1=Forme M5, IMEI1="", IMSI1="", i=null,返回"1Jph/zsXM3hLBFd8XbBLM2ooLrUu+wvC"
        String playmeId = ManuUtil.getInstance(mContext).getPlaymeID(CfgIni.getInstance().getValue("common", "hsman", Build.MANUFACTURER),
                CfgIni.getInstance().getValue("common", "hstype", Build.MODEL),
                "",
                "", null);
        playmeId = CfgIni.getInstance().getValue("common", "playmeid", playmeId);
        if (TextUtils.isEmpty(playmeId)) {
            playmeId = ManuUtil.defaultId;
        }

        return playmeId;
    }
}
