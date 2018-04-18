package com.ryg.dynamicload;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import com.ryg.utils.LOG;

import com.ryg.dynamicload.internal.DLIntent;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLPluginPackage;
import com.ryg.utils.DLUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Ben on 2015/4/20.
 * TODO用接口方式实现
 */
public class DLPluginLogic {
    private final static String TAG = "DLPluginLogic";
    private static final String PLUGIN_PACKAGE_NAME = "com.shoot.dlplugin";
    private static final String PLUGIN_SAVE_NAME = "dlShootPlugin.dat";
    private static final String PLUGIN_APK_NAME = "com.shoot.dlplugin.apk";
    private static final String PLUGIN_DIR = "shootDl";
    private static final String CONFIG_PATH_NAME = "configShootPath";
    private static final String PATH_KEY = "dlPluginPath";
    private static final String ASSETS_NAME_KEY = "plugAssetsName";
    private static final int BUFFER_SIZE = 0x1000;
    private static String pluginAssetsName = "";
    public static boolean isPluginLoaded(Context context) {
        String pluginPackageName = PLUGIN_PACKAGE_NAME;
        DLPluginManager pluginManager = DLPluginManager.getInstance(context);
        LOG.d(TAG, "isPluginLoaded PackageName = " + pluginPackageName);
        if (pluginManager.getPackage(pluginPackageName) != null && pluginManager.getPackage(pluginPackageName).isLoaded) {
            return true;
        }
        LOG.d(TAG, "插件未加载过 pluginApkName = " + pluginPackageName);
        return false;
    }

    public static DLPluginPackage loadPluginApk(Context context) throws Exception {
        return startPlugin(context, initPlugin(context));
    }

    public static void loadPluginApkService(Context context) throws Exception {
        startPluginService(context, initPlugin(context));
    }

    private static DLPluginPackage startPlugin(Context context, PluginItem item) throws Exception {
        DLPluginManager pluginManager = DLPluginManager.getInstance(context);
        DLPluginPackage dlPluginPackage = pluginManager.getPackage(item.getPackageInfo().packageName);
        if (!dlPluginPackage.isLoaded) {
            if (!TextUtils.isEmpty(item.getPackageInfo().applicationInfo.className)) {
                pluginManager.startPluginApplication(DLProxyApplication.instance, item.getPackageInfo().packageName, item.getPackageInfo().applicationInfo.className);
            }
            if (!TextUtils.isEmpty(item.getProviderName())) {
                pluginManager.startPluginContentProvider(DLProxyContentProvider.instance, item.getPackageInfo().packageName, item.getProviderName());
            }
            ActivityInfo[] activityInfos = item.getPackageInfo().receivers;
            if (activityInfos != null) {
                for (ActivityInfo activityInfo : activityInfos) {
                    Bundle bundle = activityInfo.metaData;
                    if (bundle == null) {
                        continue;
                    }
                    String action = bundle.getString("action");
                    String[] actions = action.split("#");
                    pluginManager.startPluginBroadcastReceiver(DLProxyBroadcastReceiver.getInstance(), context, item.getPackageInfo().packageName, activityInfo.name, actions);
                }
            }
        }
        return dlPluginPackage;
    }

    private static void startPluginService(Context context, PluginItem item) throws Exception {
        DLPluginManager pluginManager = DLPluginManager.getInstance(context);
        //如果存在Service则调用起Service
        if (!TextUtils.isEmpty(item.getLauncherServiceName())) {
            DLIntent intent = new DLIntent(item.getPackageInfo().packageName, item.getLauncherServiceName());
            //startService
            DLPluginPackage pluginPackage=pluginManager.getPackage(item.getPackageInfo().packageName);
            if(pluginPackage!=null){
                pluginPackage.isLoaded=true;
            }
            pluginManager.startPluginService(context, intent);
        }
    }

    private static PluginItem initPlugin(Context context) throws Exception {
        // /data/data/包名/files/shootDl/com.shoot.dlplugin.apk
        File apkPluginFile = getApkPluginFile(context);
        PluginItem item = new PluginItem();
        item.setPluginPath(apkPluginFile.getAbsolutePath());
        item.setPackageInfo(DLUtils.getPackageInfo(context, item.getPluginPath()));
        if (item.getPackageInfo().activities != null && item.getPackageInfo().activities.length > 0) {
            item.setLauncherActivityName(item.getPackageInfo().activities[0].name);
        }
        if (item.getPackageInfo().services != null && item.getPackageInfo().services.length > 0) {
            item.setLauncherServiceName(item.getPackageInfo().services[0].name);
        }
        if (item.getPackageInfo().providers != null && item.getPackageInfo().providers.length > 0) {
            item.setProviderName(item.getPackageInfo().providers[0].name);
        }
        DLPluginManager.getInstance(context).loadApk(item.getPluginPath());
        cleanOldApk(apkPluginFile);
        return item;
    }

    private static File getApkPluginFile(Context context) throws IOException {
        // /data/data/包名/files/shootDl
        File pluginFileDir = new File(context.getFilesDir(), PLUGIN_DIR);
        // /data/data/包名/files/shootDl/com.shoot.dlplugin.apk
        File apkFile = new File(pluginFileDir, PLUGIN_APK_NAME);
        // /data/data/包名/files/shootDl/dlShootPlugin.dat
        File saveFile = new File(pluginFileDir, PLUGIN_SAVE_NAME);
        // dlShootP
        pluginAssetsName =getPlugAssetsName(context);
        if(TextUtils.isEmpty(pluginAssetsName)){
            throw new  IOException("pluginAssetsName is empty!");
        }
        mkdirChecked(pluginFileDir);
        cleanOldApk(apkFile);
        if (saveFile.exists()) {
            updateFromAssets(context, apkFile, saveFile);
        } else {
            //将assets下的dlShootP读取为/data/data/包名/files/shootDl/dlShootPlugin.dat
            extractFromAssets(context, saveFile, false);
            //将assets下的dlShootP解码并读取为/data/data/包名/files/shootDl/com.shoot.dlplugin.apk
            extractFromAssets(context, apkFile, true);
        }
        savePlugPath(context, saveFile.getPath());
        return apkFile;
    }

    private static String getPlugAssetsName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_PATH_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ASSETS_NAME_KEY, null);
    }

    private static void savePlugPath(Context context, String savePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_PATH_NAME, Context.MODE_PRIVATE);
        String path = sharedPreferences.getString(PATH_KEY, null);
        if (!TextUtils.equals(savePath, path)) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(PATH_KEY, savePath);
            edit.apply();
        }
    }

    private static void updateFromAssets(Context context, File apkFile, File saveFile) throws IOException {
        int tmpLocalVersion, tmpAssetsVersion;
        File tmpLocalApkFile = File.createTempFile(apkFile.getName(), null, apkFile.getParentFile());
        File tmpAssetsApkFile = File.createTempFile(apkFile.getName(), null, apkFile.getParentFile());
        decodeFile(saveFile, tmpLocalApkFile);
        extractFromAssets(context, tmpAssetsApkFile, true);
        tmpLocalVersion = DLUtils.getPackageVersion(context, tmpLocalApkFile.getAbsolutePath());
        tmpAssetsVersion = DLUtils.getPackageVersion(context, tmpAssetsApkFile.getAbsolutePath());
        if (tmpAssetsVersion > tmpLocalVersion) {
            if (!tmpAssetsApkFile.renameTo(apkFile)) {
                throw new IOException("Failed to rename " + tmpAssetsApkFile.getPath());
            }
            if (!tmpLocalApkFile.delete()) {
                LOG.w(TAG, "Failed to rename " + tmpAssetsApkFile.getPath());
            }
        } else {
            if (!tmpLocalApkFile.renameTo(apkFile)) {
                throw new IOException("Failed to rename " + tmpLocalApkFile.getPath());
            }
            if (!tmpAssetsApkFile.delete()) {
                LOG.w(TAG, "Failed to rename " + tmpAssetsApkFile.getPath());
            }
        }
    }

    private static void cleanOldApk(File apkFile) throws IOException {
        if (apkFile.exists()) {
            if (!apkFile.delete()) {
                throw new IOException("Failed to delete file " + apkFile.getPath());
            }
        }
    }

    private static void extractFromAssets(Context context, File saveFile, boolean isDecode) throws IOException {
        AssetManager assetManager = context.getAssets();
        File tmpFile = File.createTempFile(saveFile.getName(), null, saveFile.getParentFile());
        InputStream in = new BufferedInputStream(assetManager.open(pluginAssetsName),BUFFER_SIZE);
        OutputStream out = new FileOutputStream(tmpFile);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = in.read(buffer);
            while (length != -1) {
                if (isDecode) {
                    buffer = decode(buffer, length);
                }
                out.write(buffer, 0, length);
                length = in.read(buffer);
            }
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!tmpFile.renameTo(saveFile)) {
            throw new IOException("Failed to rename file " + saveFile.getPath());
        }

    }

    private static void decodeFile(File srcFile, File outFile) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(srcFile),BUFFER_SIZE);
        FileOutputStream out = new FileOutputStream(outFile);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = in.read(buffer);
            while (length != -1) {
                buffer = decode(buffer, length);
                out.write(buffer, 0, length);
                length = in.read(buffer);
            }
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建文件夹
     *
     * @param dir 需要创建的目录
     * @throws IOException 创建文件夹异常
     */
    private static void mkdirChecked(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException(dir.getAbsolutePath() + "is not dir");
            }
        } else {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create dir " + dir.getAbsolutePath());
            }
        }
    }

    //对字符串进行简单加密和解密
    private static byte[] decode(byte[] input, int len) {
        byte[] seed = {(byte) 0xa4, 0x02, 0x06, 0x05, 0x01, 0x09, 0x2e, 0x4d, 0x5c, (byte) 0xe1, 0x7c, 0x55, (byte) 0x8a, (byte) 0xcc, (byte) 0xac};//加密种子
        int i = 0, j = 0;
        for (i = 0; i < len; i++) {
            input[i] = (byte) (input[i] ^ seed[j]);
            j = j < 14 ? j + 1 : 0;
        }
        return input;
    }

}
