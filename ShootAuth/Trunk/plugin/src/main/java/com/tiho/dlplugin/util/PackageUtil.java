package com.tiho.dlplugin.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;

import com.tiho.base.base.md.Md5Handler;
import com.tiho.base.common.LogManager;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class PackageUtil {

    /**
     * 是否已经安装应用
     *
     * @param pack 包名
     * @return
     */
    public static boolean isInstalled(String pack, Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(pack, 0) != null;
        } catch (Exception e) {
            LogManager.LogShow("pack = " + pack + "找不到");
        }
        return false;
    }

    /**
     * 安装是否最新版本
     *
     * @param md5  最新版本的md5
     * @param pack 包名
     * @return 相同返回true 不同返回false
     */
    public static boolean isLatestVersion(String md5, String pack, Context context) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(pack, 0);
            if (packageInfo != null&&packageInfo.applicationInfo!=null) {
                String sourcePath = packageInfo.applicationInfo.sourceDir;
                File file = new File(sourcePath);
                if (!TextUtils.isEmpty(sourcePath)&&file.exists()) {
                    String packMd5 = new Md5Handler().md5Calc(file);
                    if(TextUtils.isEmpty(md5)){
                        result=true;
                    }else if(!TextUtils.isEmpty(packMd5)&&TextUtils.equals(packMd5.toLowerCase(),md5.toLowerCase())){
                        result=true;
                    }
                }
            }
        } catch (Exception e) {
            LogManager.LogShow("pack = " + pack + "找不到");
        }
        return result;
    }

    public static Bitmap loadUninstallApkIcon(Context context, String archiveFilePath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);

            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = archiveFilePath;
            appInfo.publicSourceDir = archiveFilePath;

            BitmapDrawable bd = (BitmapDrawable) appInfo.loadIcon(pm);
            return bd.getBitmap();

        } catch (Exception e) {
            LogManager.LogShow(e);
            return BitmapUtil.getBitmap("icon_default");
        }

    }

    /**
     * 包是否可以解析
     *
     * @param context
     * @param path
     * @return true 可以解析 ， false 不可解析
     */
    public static boolean parsable(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);

        return info != null;
    }

    /**
     * 解析包名
     *
     * @param context
     * @param path
     * @return
     */
    public static String parsePackageName(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            String packageName = appInfo.packageName; // 得到包名
            return packageName;
        }
        return "";
    }

    /**
     * 是否声明了某个权限
     *
     * @param c
     * @param p
     * @return
     */
    public static boolean permissionDeclared(Context c, String p) {
        PackageInfo pi;
        try {
            pi = c.getPackageManager().getPackageInfo(c.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = pi.requestedPermissions;

            for (String permissionInfo : ps) {
                if (permissionInfo.equals(p))
                    return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 检查宿主是否有某个权限
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean checkPermission(Context context, String permission) {
        return context.checkCallingOrSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED ? true : false;
    }

    /**
     * 解析apk应用名
     *
     * @param context
     * @param path
     * @return
     */
    public static String parseAppName(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            info.applicationInfo.sourceDir = path;
            info.applicationInfo.publicSourceDir = path;

            return pm.getApplicationLabel(info.applicationInfo).toString();
        }
        return "";
    }

    /**
     * 调用系统安装界面
     *
     * @param path
     * @param acti
     * @param requestCode
     */
    public static void gotoInstall(String path, Activity acti, int requestCode) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        acti.startActivityForResult(i, requestCode);
    }

    public static void gotoInstall(Context context, String path) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * 创建快捷方式
     *
     * @param context
     * @param intent  快捷方式点击后的intent
     * @param appname 快捷方式名字
     * @param icon    图标路径，绝对路径
     */
    public static Intent createShortcutOnDesktop(Context context, Intent intent, String name, Bitmap icon) {

        Intent shortcutIntent = new Intent();
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutIntent.putExtra("duplicate", false);

        context.sendBroadcast(shortcutIntent);

        return shortcutIntent;
    }

    /**
     * 创建快捷方式
     *
     * @param context
     * @param shortcutName 快捷方式名字
     * @param createIntent 创建此快捷方式时的intent
     */
    public static void deleteShortCut(Context context, String shortcutName, Intent createIntent) {
        Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        // 快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, createIntent);
        context.sendBroadcast(shortcut);
    }

    /**
     * 判断快捷方式是否存在
     *
     * @param context
     * @param iconTitle 快捷方式名称
     * @return
     */
    public static boolean hasShortcut(Context context, String iconTitle) {
        boolean isInstallShortcut = false;
        final String AUTHORITY = getAuthorityFromPermission(context, "com.android.launcher.permission.READ_SETTINGS");

        if (AUTHORITY == null)
            return false;

        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        Cursor c = context.getContentResolver().query(CONTENT_URI, new String[]{"title"}, "title=?", new String[]{iconTitle}, null);
        if (c != null && c.getCount() > 0) {
            isInstallShortcut = true;
        }
        return isInstallShortcut;
    }

    private static String getAuthorityFromPermission(Context context, String permission) {
        if (permission == null) {
            return null;
        }
        List<PackageInfo> packageInfoList = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
        if (packageInfoList == null) {
            return null;
        }
        for (PackageInfo packageInfo : packageInfoList) {
            ProviderInfo[] providerInfos = packageInfo.providers;
            if (providerInfos != null) {
                for (ProviderInfo providerInfo : providerInfos) {
                    if (permission.equals(providerInfo.readPermission) || permission.equals(providerInfo.writePermission)) {
                        return providerInfo.authority;
                    }
                }
            }
        }
        return null;
    }

    public static void openApp(Context context, String packageName) {
        Intent i = intentForOpenApp(context, packageName);
        if (i == null) {
            i = intentForOpenAppFromAction(context, packageName);
        }
        if (i != null)
            context.startActivity(i);
    }

    private static Intent intentForOpenAppFromAction(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(packageName, 1);
            Intent resolveIntent = new Intent(packageName + ".activate");
            resolveIntent.addCategory(Intent.CATEGORY_DEFAULT);
            resolveIntent.setPackage(pi.packageName);
            List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, PackageManager.MATCH_DEFAULT_ONLY);
            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                Intent intent = new Intent(packageName + ".activate");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return intent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Intent intentForOpenApp(Context context, String packageName) {
        //打开指定的App com.sprovider.self.MainActivity
        if (packageName != null && packageName.equals("com.sprovider.self")) {
            return openSpeicalApp(context);
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                String packName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                ComponentName cn = new ComponentName(packName, className);

                intent.setComponent(cn);

                return intent;
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }

        return null;
    }

    public static boolean isHostPlayApp(Context context) {
        String name = context.getApplicationContext().getPackageName();
        return "me.pplay.playapp".equals(name) || "me.powerplay.playapp".equals(name);
    }

    //打开指定的App com.sprovider.self.MainActivity
    private static Intent openSpeicalApp(Context context) {
        LogManager.LogShow("openSpeicalApp start");
        try {
            String packageName = "com.sprovider.self";
            PackageManager pm = context.getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);

            Iterator<ResolveInfo> it = apps.iterator();
            while (it.hasNext()) {
                ResolveInfo ri = it.next();
                if (ri != null && ri.activityInfo.name.equals("com.sprovider.self.MainActivity")) {
                    String packName = ri.activityInfo.packageName;
                    String className = ri.activityInfo.name;

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    ComponentName cn = new ComponentName(packName, className);

                    intent.setComponent(cn);
                    LogManager.LogShow("openSpeicalApp ok");
                    return intent;
                }
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
        LogManager.LogShow("openSpeicalApp over");
        return null;
    }

    public static boolean isSystemApp(Context context, String pkg) {
        boolean ret = false;

        try {
            PackageInfo mPackageInfo = context.getPackageManager().getPackageInfo(pkg, 0);
            if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                //第三方应用  
                ret = false;
            } else {
                //系统应用  
                ret = true;
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
        return ret;
    }

    /**
     * 获取apk的版本号
     *
     * @param context
     * @param path
     * @return
     */
    public static int parsePackageVersionCode(Context context, String path) {
        try {
            LogManager.LogShow("parsePackageVersionCode path = " + path);
            PackageManager pm = context.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
            if (pkgInfo != null) {
                int ver = pkgInfo.versionCode;
                LogManager.LogShow("parsePackageVersionCode ver = " + ver);
                return ver;
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
        return 0;
    }

}
