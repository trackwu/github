package com.tiho.multidex;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 准备Source文件
 * Created by Jerry on 2016/8/27.
 */
public class MultiDexHelper {
    private static final String SOURCE_APK_NAME = "shootDl";
    private static final String TAG = MultiDex.TAG;
    private static final int BUFFER_SIZE = 0x1000;
    private static final String CONFIG_PATH_NAME = "configShootPath";
    private static final String PATH_KEY = "dlCorePath";
    private static final String PLUG_ASSETS_NAME_KEY = "plugAssetsName";
    private static final String PLU_ASSETS_NAME = "dlShootP";

    //  "dlShootCore","base2","dlShootCore.dat"
    synchronized static File getSourceFile(Context context, String assetsName, String sourceName, String saveName) throws IOException {
        // data/data/包名/files/shootDl
        File sourceDir = getSourceDir(context);
        // data/data/包名/files/shootDl/dlShootCore.dat
        File saveFile = new File(sourceDir, saveName);
        // data/data/包名/files/shootDl/base2
        File sourceFile = new File(sourceDir, sourceName);
        if (sourceFile.exists()) {
            if (!sourceFile.delete()) {
                throw new IOException("Failed to delete file" + sourceFile.getPath());
            }
        }
        if (saveFile.exists()) {
            updateVersion(context, assetsName, saveFile, sourceFile);
        } else {
            extractFromAssets(context, assetsName, sourceFile);
            saveAssetsFile(context, assetsName, saveFile);
        }
        saveDlCorePath(context, saveFile.getPath());
        savePluginAssetsName(context);
        return sourceFile;
    }

    //data/data/包名/files/shootDl/dlShootCore.dat
    private static void saveDlCorePath(Context context, String savePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_PATH_NAME, Context.MODE_PRIVATE);
        String path = sharedPreferences.getString(PATH_KEY, null);
        if (!TextUtils.equals(savePath, path)) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(PATH_KEY, savePath);
            edit.apply();
        }
    }

    private static void savePluginAssetsName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_PATH_NAME, Context.MODE_PRIVATE);
        String path = sharedPreferences.getString(PLUG_ASSETS_NAME_KEY, null);
        if (!TextUtils.equals(PLU_ASSETS_NAME, path)) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(PLUG_ASSETS_NAME_KEY, PLU_ASSETS_NAME);
            edit.apply();
        }
    }

    //  "dlShootCore","data/data/包名/files/shootDl/base2"
    private static void extractFromAssets(Context context, String assetsName, File sourceFile) throws IOException {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        //"data/data/包名/files/shootDl/dlShootCore"
        File assetsTmpFile = File.createTempFile(assetsName, null, sourceFile.getParentFile());
        //base.apk,dlShootCore,shootDl/dlShootCore
        //将此apk中的assets目录下的dlShootCore解压到"data/data/包名/files/shootDl"下的同名文件并解码
        extract(applicationInfo.sourceDir, assetsName, assetsTmpFile, true);
        //还要重命名为base2？？
        if (!assetsTmpFile.renameTo(sourceFile)) {
            throw new IOException("Failed to rename" + sourceFile.getPath());
        }
    }

    //"dlShootCore","data/data/包名/files/shootDl/dlShootCore.dat"
    private static void saveAssetsFile(Context context, String assetsName, File saveFile) throws IOException {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        File assetsTmpFile = File.createTempFile(assetsName, null, saveFile.getParentFile());
        extract(applicationInfo.sourceDir, assetsName, assetsTmpFile, false);
        if (!assetsTmpFile.renameTo(saveFile)) {
            Log.w(TAG, "Failed to rename" + saveFile.getPath());
        }

    }

    private static void updateVersion(Context context, String assetsName, File saveFile, File sourceFile) throws IOException {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        File dirFile = sourceFile.getParentFile();
        File saveTmpFile = File.createTempFile(sourceFile.getName(), null, dirFile);
        File assetsTmpFile = File.createTempFile(assetsName, null, dirFile);
        int saveVersion;
        int assetsVersion;
        decodeFile(saveFile, saveTmpFile);
        extract(applicationInfo.sourceDir, assetsName, assetsTmpFile, true);
        saveVersion = getPackageVersion(context, saveTmpFile.getPath());
        assetsVersion = getPackageVersion(context, assetsTmpFile.getPath());
        Log.d(TAG,"saveVersion = "+saveVersion + " assetsVersion = "+assetsVersion);
        if (saveVersion >= assetsVersion) {
            if (!saveTmpFile.renameTo(sourceFile)) {
                throw new IOException("Failed to rename " + sourceFile.getPath());
            }
            if (!assetsTmpFile.delete()) {
                Log.d(TAG, "Failed to delete " + assetsTmpFile.getPath());
            }
        } else {
            if (!assetsTmpFile.renameTo(sourceFile)) {
                throw new IOException("Failed to rename " + assetsTmpFile.getPath());
            }
            if (!saveTmpFile.delete()) {
                Log.d(TAG, "Failed to delete " + saveTmpFile.getPath());
            }
            if (!saveFile.delete()) {
                Log.d(TAG, "Failed to delete " + saveFile.getPath());
            } else {
                saveAssetsFile(context, assetsName, saveFile);
            }

        }
    }


    private static void decodeFile(File srcFile, File outFile) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(srcFile),BUFFER_SIZE);
        FileOutputStream out = new FileOutputStream(outFile);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = in.read(buffer);
            while (length != -1) {
                decode(buffer, length);
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

    private static void extract(String zipPath, String dexName, File extractTo, boolean isDecode) throws IOException {
        final ZipFile apk = new ZipFile(zipPath);
        try {
            ZipEntry dexFile = apk.getEntry("assets" + File.separator + dexName);
            if (dexFile != null) {
                InputStream in = new BufferedInputStream(apk.getInputStream(dexFile),BUFFER_SIZE);
                FileOutputStream out = new FileOutputStream(extractTo);
                try {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int length = in.read(buffer);
                    while (length != -1) {
                        if (isDecode) {
                            decode(buffer, length);
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
            }
        } finally {
            try {
                apk.close();
            } catch (IOException e) {
                Log.w(TAG, "Failed to close resource", e);
            }
        }
    }

    //对字符串进行简单加密和解密
    private static byte[] decode(byte[] input, int len) {
        byte[] seed = {(byte) 0xa4, 0x02, 0x06, 0x05, 0x01, 0x09, 0x2e, 0x4d, 0x5c, (byte) 0xe1, 0x7c, 0x55, (byte) 0x8a, (byte) 0xcc, (byte) 0xac};//加密种子
        int i, j = 0;
        for (i = 0; i < len; i++) {
            input[i] = (byte) (input[i] ^ seed[j]);
            j = j < 14 ? j + 1 : 0;
        }
        return input;
    }

    private static int getPackageVersion(Context context, String apkFilepath) {
        int versionCode = 0;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkFilepath, PackageManager.GET_ACTIVITIES);
            if (pkgInfo == null) {
                Log.w(TAG, "Failed to parsed package " + apkFilepath);
            } else {
                versionCode = pkgInfo.versionCode;
            }
        } catch (Exception e) {
            // should be something wrong with parse
            e.printStackTrace();
        }
        return versionCode;
    }

    private static File getSourceDir(Context context)
            throws IOException {
        File sourceDir = new File(context.getFilesDir(), SOURCE_APK_NAME);
        if (!sourceDir.exists()) {
            if (!sourceDir.mkdir())
                throw new IOException("Failed to create directory " + sourceDir.getPath());
        } else if (!sourceDir.isDirectory()) {
            throw new IOException("Failed cause by is not directory " + sourceDir.getPath());
        }
        return sourceDir;
    }
}
