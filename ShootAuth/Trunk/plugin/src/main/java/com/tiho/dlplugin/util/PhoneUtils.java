package com.tiho.dlplugin.util;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Jerry on 2016/6/8.
 */
public class PhoneUtils {


    private static Map<String, String> getCpuInfo() {
        Hashtable<String, String> cupMap = new Hashtable<>();
        String str1 = "/proc/cpuinfo";
        String line;
        String[] strArray;
        FileReader fr = null;
        BufferedReader bf = null;
        try {
            fr = new FileReader(str1);
            bf = new BufferedReader(fr, 8192);
            while ((line = bf.readLine()) != null) {
                strArray = line.split(":");
                if (strArray.length == 2
                        && !TextUtils.isEmpty(strArray[0])
                        && !TextUtils.isEmpty(strArray[1])) {
                    cupMap.put(strArray[0].trim(), strArray[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bf != null) {
                    bf.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cupMap;
    }

    /**
     * 获取cpu型号
     * @return 获取成功返回cpu型号 否则返回空字符串
     */
    public static String getCpuModel() {
        Map<String, String> cupMap = getCpuInfo();
        String cpuModel = cupMap.get("Processor");
        if (!TextUtils.isEmpty(cpuModel)) {
            return cpuModel;
        } else {
            return "";
        }
    }

    /**
     * 获取android系统版本
     */
    public static int getAndroidVersion(){
      return  Build.VERSION.SDK_INT;
    }
    public static String getRadioVersion() {
        String radioVersion = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                radioVersion = Build.getRadioVersion();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(radioVersion)) {
            return radioVersion;
        } else {
            return "";
        }
    }

    public static String getReleaseVersion() {
        String releaseVersion = null;
        try {
            releaseVersion = Build.VERSION.RELEASE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(releaseVersion)) {
            return releaseVersion;
        } else {
            return "";
        }
    }

    public static String getImei(Context context) {
        String imei = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        } else {
            return "";
        }
    }

    public static String getImsi(Context context) {
        String imsi = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imsi = telephonyManager.getSubscriberId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(imsi)) {
            return imsi;
        } else {
            return "";
        }
    }

    public static String getSimSerialNumber(Context context) {
        String simSerialNumber = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            simSerialNumber = telephonyManager.getSimSerialNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(simSerialNumber)) {
            return simSerialNumber;
        } else {
            return "";
        }
    }

    public static String getAndroidId(Context context) {
        String androidId = null;
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(androidId)) {
            return androidId;
        } else {
            return "";
        }
    }

    public static String getSerial() {
        String serial = null;
        try {
            serial = Build.SERIAL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(serial)) {
            return serial;
        } else {
            return "";
        }
    }

    public static String getModel() {
        String model = null;
        try {
            model = Build.MODEL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(model)) {
            return model;
        } else {
            return "";
        }
    }

    public static String getCpuHardWare() {
        String hardWare = null;
        try {
            Map<String, String> cupMap = getCpuInfo();
            hardWare = cupMap.get("Hardware");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(hardWare)) {
            return hardWare;
        } else {
            return "";
        }
    }

    public static String getCpuRevision() {
        String revision = null;
        try {
            Map<String, String> cupMap = getCpuInfo();
            revision = cupMap.get("Revision");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(revision)) {
            return revision;
        } else {
            return "";
        }
    }

    public static String getCpuSerial() {
        String serial = null;
        try {
            Map<String, String> cupMap = getCpuInfo();
            serial = cupMap.get("Serial");
        }catch (Exception e){
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(serial)) {
            return serial;
        } else {
            return "";
        }
    }

    public static String getWifiMacAddress(Context context) {
        String wifiMac = null;
        try {
            WifiManager wifiM = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            wifiMac = wifiM.getConnectionInfo().getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(wifiMac)) {
            return wifiMac;
        } else {
            return "";
        }
    }

    public static String getBluetoothAddress(Context context) {
        String address = null;
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (context.checkCallingPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    boolean bool = bluetoothAdapter.isEnabled();
                    if (bool) {
                        address = bluetoothAdapter.getAddress();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(address)) {
            return address;
        } else {
            return "";
        }
    }

    public static String getCpuFeatures() {
        String features = null;
        try {
            Map<String, String> cupMap = getCpuInfo();
            features = cupMap.get("Features");
            if (TextUtils.isEmpty(features)) {
                features = cupMap.get("Flags");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(features)) {
            return features;
        } else {
            return "";
        }
    }

    public static String getWifiSSID(Context context) {
        String wifiSSID = null;
        try {
            WifiManager wifiM = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            wifiSSID = wifiM.getConnectionInfo().getSSID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(wifiSSID)) {
            return wifiSSID;
        } else {
            return "";
        }
    }

    public static String getSimOperatorName(Context context) {
        String simOperatorName = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            simOperatorName = telephonyManager.getSimOperatorName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(simOperatorName)) {
            return simOperatorName;
        } else {
            return "";
        }
    }

    public static String getWifiBSSID(Context context) {
        String wifiBSSID = null;
        try {
            WifiManager wifiM = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            wifiBSSID = wifiM.getConnectionInfo().getBSSID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(wifiBSSID)) {
            return wifiBSSID;
        } else {
            return "";
        }
    }

    public static String getNetworkExtraInfo(Context context) {
        String networkExtraInfo = null;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            networkExtraInfo = networkInfo.getExtraInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(networkExtraInfo)) {
            return networkExtraInfo;
        } else {
            return "";
        }
    }

    /**
     * 获取硬件识别码
     */
    public static String getFinger() {
        String finger = null;
        try {
            finger = Build.FINGERPRINT;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(finger)) {
            return finger;
        } else {
            return "";
        }
    }

    /**
     * 获取主板
     */
    public static String getBoard() {
        String board = null;
        try {
            board = Build.BOARD;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(board)) {
            return board;
        } else {
            return "";
        }
    }

    /**
     * 获取系统启动程序版本号
     */
    public static String getBootloader() {
        String bootloader = null;
        try {
            bootloader = Build.BOOTLOADER;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(bootloader)) {
            return bootloader;
        } else {
            return "";
        }
    }

    /**
     * 获取系统定制商
     */
    public static String getBrand() {
        String brand = null;
        try {
            brand = Build.BRAND;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(brand)) {
            return brand;
        } else {
            return "";
        }
    }

    /**
     * 获取系统机型
     */
    public static String getDevice() {
        String device = null;
        try {
            device = Build.DEVICE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(device)) {
            return device;
        } else {
            return "";
        }
    }

    /**
     * 获取硬件名称
     */
    public static String getHardware() {
        String hardware = null;
        try {
            hardware = Build.HARDWARE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(hardware)) {
            return hardware;
        } else {
            return "";
        }
    }

    /**
     * 获取手机制造商
     */
    public static String getProduct() {
        String product = null;
        try {
            product = Build.PRODUCT;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(product)) {
            return product;
        } else {
            return "";
        }
    }

    /**
     * 获取硬件制造商
     */
    public static String getManufacturer() {
        String manufacturer = null;
        try {
            manufacturer = Build.MANUFACTURER;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(manufacturer)) {
            return manufacturer;
        } else {
            return "";
        }
    }

    public static String getPhoneNumber(Context context) {
        String phoneNumber = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            phoneNumber = telephonyManager.getLine1Number();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(phoneNumber)) {
            return phoneNumber;
        } else {
            return "";
        }
    }

    public static String getNetworkSubtype(Context context) {
        int subtype = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            subtype = networkInfo.getSubtype();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(subtype);
    }

    public static String getIncremental() {
        String incremental = null;
        try {
            incremental = Build.VERSION.INCREMENTAL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(incremental)) {
            return incremental;
        } else {
            return "";
        }
    }


}
