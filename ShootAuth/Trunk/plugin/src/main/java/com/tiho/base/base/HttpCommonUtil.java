package com.tiho.base.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLPluginPackage;
import com.tiho.base.base.des.ManufactoryDES;
import com.tiho.base.base.manufactory.aidl.ManuUtil;
import com.tiho.base.base.manufactory.aidl.PlaymeIdHelper;
import com.tiho.base.common.CfgIni;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.Global;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.util.NetworkUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class HttpCommonUtil {
    private static final String HSTYPE_DIR = "DataShootPP";
    private static HttpCommonUtil headerUtil;
    private static Context mContext;
    private String hsman = ""; // 厂商
    private String imsi = "999001234567890";
    private String mcc = ""; // 国家 imsi前三位（如果没有则：999）
    private String plmn = ""; // imsi 后三位
    private int level; //安卓sdk版本
    private String screen = ""; // 屏幕尺寸
    private String imei = "";
    private String hstype = ""; // 手机型号
    private String hostVer = ""; //宿主apk版本号
    private String cellid = "";
    private String we = "";
    private String wifimac = "";
    private String serial = "";
    private String playmeid = ManuUtil.defaultId;
    private String mIp = "";
    private String lan = ""; //系统当前使用的语言
    private String hsmanOwn = "";
    private String hstypeOwn = "";
    private String hostPack = "";
    private String network = "";
    private String lc;
    private String pluginpkg = "";
    private int pluginver = 0;
    private String androidId = "";
    private String ownMan ="";
    private String ownType ="";
    private UUID deviceUuid = new UUID(0, 0);

    private HttpCommonUtil() {
    }

    public synchronized static HttpCommonUtil getInstance() {
        if (headerUtil == null) {
            headerUtil = new HttpCommonUtil();
        }
        return headerUtil;
    }

    public void init(Context context) {
        LogManager.LogShow("httpcommonkey context = " + context);
        mContext = context;
        init();
    }

    public String commonkey() {
        String key = "hsman=" + hsman + "&mcc=" + mcc + "&level=" + level
                + "&screen=" + screen + "&lan="
                + Locale.getDefault().getLanguage() + "&playmeid=" + playmeid;
        LogManager.LogShow("httpcommonkey:" + key, LogManager.INFO);
        LogManager.LogShow("httpcommonkey base64:"
                + new String(Base64.encode(key.getBytes())));
        return new String(Base64.encode(key.getBytes()));
    }

    public String xPlayAgent() {

        String value = "imsi=" + imsi + "&imei=" + imei + "&hsman=" + hsman
                + "&hstype=" + hstype + "&android=" + hostVer + "&cellid="
                + cellid + "&we=" + we + "&lan="
                + Locale.getDefault().getLanguage() + "&wifimac=" + wifimac
                + "&screen=" + screen + "&level=" + level + "&serial=" + serial
                + "&network="
                + ConnectivityManagerUtil.getNetworkType(mContext)
                + "&playmeid=" + playmeid + "&androidId=" + androidId
                +"&ownMan="+ownMan+"&ownType="+ownType;
        LogManager.LogShow("xPlayAgent:" + value, LogManager.INFO);
        LogManager.LogShow("xPlayAgent base64:"
                + new String(Base64.encode(value.getBytes())));
        return new String(Base64.encode(value.getBytes()));
    }

    public String getPlaymeid() {
        return playmeid;
    }

    public String getImsi() {
        if (TextUtils.isEmpty(imsi)) {
            try {
                TelephonyManager tm = (TelephonyManager) mContext
                        .getSystemService(Context.TELEPHONY_SERVICE);
                imsi = tm.getSubscriberId();
            } catch (Exception e) {
                LogManager.LogShow(e);
            }
        }
        if(imsi==null){
            return "";
        }else{
            return imsi;
        }
    }

    public String getImei() {
        if (TextUtils.isEmpty(imei)) {
            try {
                TelephonyManager tm = (TelephonyManager) mContext
                        .getSystemService(Context.TELEPHONY_SERVICE);
                imei = tm.getDeviceId();
            } catch (Exception e) {
                LogManager.LogShow(e);
            }
        }
        if(imei==null){
            return "";
        }else{
            return imei;
        }
    }

    public int getAndroidVersion(){
        return  Build.VERSION.SDK_INT;
    }
    public String getOwnHsManType() {
        return "hsman=" + hsmanOwn + "&hstype=" + hstypeOwn + "&f=pr";
    }

    public String getOwnHsMan() {
        return hsmanOwn;
    }

    public String getOwnHsType() {
        return hstypeOwn;
    }

    public String getUrlParams() {
        String s = "imsi=" + imsi + "&imei=" + imei + "&hostpkg=" + hostPack + "&hostver=" + hostVer + "&pluginpkg=" + pluginpkg + "&pluginver=" + pluginver;

        String params = "?hsman=" + hsmanOwn + "&hstype=" + hstypeOwn + "&b=" + URLEncoder.encode(Base64.encode(s.getBytes()));
        LogManager.LogShow("getUrlParams params = " + params);
        return params;
    }

    private void getPluginPackageInfo() {
        LogManager.LogShow("getPluginPackageInfo " + mContext.getPackageManager());
        DLPluginPackage dlp = DLPluginManager.getInstance(mContext).getPackage(Global.sPackageName);
        PackageInfo packageInfo = dlp.packageInfo;
        pluginpkg = packageInfo.packageName;
        pluginver = packageInfo.versionCode;

        LogManager.LogShow("getPluginPackageInfo packageName = " + pluginpkg + ", versionCode = " + pluginver);
    }
    private static String getNormalText(String text){
        if(text==null)
            return "";
        String result;
        result = text.replaceAll("[^0-9a-zA-Z]", "");
        result = result.toLowerCase();
        if (result.length() > 41)
            result = result.substring(0, 40);
        return result;
    }



    private void init() {
        try {
            network = ConnectivityManagerUtil.getNetworkType(mContext);
            TelephonyManager tm = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            WifiManager wifiM = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
            PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            hsman = Build.MANUFACTURER;
            if (hsman == null)
                hsman = "";
            // imsi = tm.getSubscriberId();

            plmn = tm.getSimOperator();
            lc = tm.getSimCountryIso();
            tm.getSimCountryIso();
            if (plmn == null || plmn.equals("")) {
                plmn = "999999";
            }
            // mnc = plmn.substring(3);

            level = Build.VERSION.SDK_INT;

            lan = Locale.getDefault().getLanguage();
            imei = tm.getDeviceId();
            imsi = CfgIni.getInstance().getValue("common", "imsi", tm.getSubscriberId());
            LogManager.LogShow("imsi==" + imsi);
            if (imei == null)
                imei = "";
            if (TextUtils.isEmpty(imsi)) {
                imsi = "";
                mcc = "999";
            } else {
                mcc = imsi.substring(0, imsi.length() >= 3 ? 3 : imsi.length());
            }
            hstype = Build.MODEL; // 手机型号
            hostVer = String.valueOf(pinfo.versionCode);
            hostPack = pinfo.packageName;
            // vername = pinfo.versionName;
            // vercode = pinfo.versionCode;
            hsman = CfgIni.getInstance().getValue("common", "hsman", hsman);
            hstype = CfgIni.getInstance().getValue("common", "hstype", hstype);
            initPlaymeid();
            wifimac = wifiM.getConnectionInfo().getMacAddress();
            if (wifimac == null)
                wifimac = "";
            DhcpInfo dhcpInfo = wifiM.getDhcpInfo();

            serial = tm.getSimSerialNumber();
            if (level > 8)
                serial = getSerial();
            if (serial == null)
                serial = "";
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            screen = dm.widthPixels + "x" + dm.heightPixels;


            androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (!TextUtils.isEmpty(imei)) {
                deviceUuid = new UUID(androidId.hashCode(), ((long) imei.hashCode() << 32) | imei.hashCode());
            }

            getPluginPackageInfo();
            mIp = Formatter.formatIpAddress(dhcpInfo.ipAddress);  //int2ip(wifiM.getConnectionInfo().getIpAddress());
            if (TextUtils.isEmpty(mIp)) {
                mIp = "0.0.0.0";
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSerial() {
        String serialnum = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            serialnum = (String) (get.invoke(c, "ro.serialno", "unknown"));
        } catch (Exception ignored) {
        }
        return serialnum;
    }

    private void initPlaymeid() {
        try {
            String tmp = CfgIni.getInstance().getValue("common", "playmeid", null);
            playmeid = tmp == null ? PlaymeIdHelper.getInstance(mContext).getPlaymeIdCiphertext() : tmp;
            getHsOwn();
            if (playmeid != null && !playmeid.equals(ManuUtil.defaultId)) {
                writeHstypeToFile(playmeid);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private void getHsOwn() {
        String[] s = ManufactoryDES.decode(playmeid);
        LogManager.LogShow("getHsOwn s = " + s);
        if (s.length == 3) {
            hsmanOwn = s[1];
            hstypeOwn = s[2];
            ownMan = hsmanOwn+"_"+getNormalText(Build.MANUFACTURER);
            ownType = hstypeOwn+"_"+getNormalText(Build.MODEL);
        }
        LogManager.LogShow("getOwnMan ownMan = " + ownMan + ", ownType = " + ownType);
        LogManager.LogShow("getHsOwn hsmanOwn = " + hsmanOwn + ", hstypeOwn = " + hstypeOwn);
    }

    public Map<String, String> getCommonInfo() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("hsman_system", hsman);
        map.put("imsi", imsi);
        map.put("mcc", mcc);
        map.put("mnc", plmn);
        map.put("android_version", level + "");
        map.put("screen", screen);
        map.put("imei", imei);
        map.put("hstype_system", hstype);
        map.put("host_ver", hostVer);
        map.put("wifimac", wifimac);
        map.put("client_ip", mIp);
        map.put("lan", lan);
        map.put("hsman", hsmanOwn);
        map.put("hstype", hstypeOwn);
        map.put("host_pack", hostPack);
        map.put("network", network);
        map.put("client_version", CommonInfo.getInstance(mContext).getPluginVer());
        map.put("plugin_name", CommonInfo.getInstance(mContext).getPluginName());
        map.put("network_enabled", (NetworkUtil.isNetworkOk(mContext) ? "Y" : "N"));
        map.put("androidId", androidId);
        map.put("deviceUuid", deviceUuid.toString());
        for (String k : map.keySet()) {
            if (TextUtils.isEmpty(map.get(k))) {
                LogManager.LogShow("getCommonInfo " + k + " is empty");
                map.put(k, " ");
            }
        }
        LogManager.LogShow("getCommonInfo map = " + map);
        return map;
    }

    private void deletePlaypushFile() {
        try {
            File dir = null;
            String playpushDir = "PLAYPUSH";
            if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
                dir = new File(Environment.getExternalStorageDirectory(), playpushDir);
            } else {
                dir = new File(mContext.getFilesDir(), playpushDir);
            }

            File file = new File(dir, "hstype.txt");
            if (file.exists()) {
                LogManager.LogShow("deletePlaypushFile hstype.txt exists");
                file.delete();
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
    }

    private void writeHstypeToFile(String playmeid) {
        deletePlaypushFile();
        File dir = null;

//        if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
//            dir = new File(Environment.getExternalStorageDirectory(), HSTYPE_DIR);
//        } else {
            dir = new File(mContext.getFilesDir(), HSTYPE_DIR);
//        }

        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, "hstype.txt");
        if (file.exists()) {
            LogManager.LogShow("writeHstypeToFile hstype.txt exists");
            return;
        }

        try {
            String[] s = ManufactoryDES.decode(playmeid);
            System.out.println("s = " + s);
            if (s.length == 3) {
                String hsman = s[1];
                String hstype = s[2];
                LogManager.LogShow("hsman = " + hsman + ", hstype = " + hstype);
                String in = "hsman=" + hsman + "&hstype=" + hstype;

                if (!dir.exists()) {
                    dir.mkdir();
                }
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(in.getBytes());
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
    }



    public static interface CommonUtiCb {
        public void result(int result, Object obj);
    }


}
