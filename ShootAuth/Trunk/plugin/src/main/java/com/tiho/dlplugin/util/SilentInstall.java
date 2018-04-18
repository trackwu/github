package com.tiho.dlplugin.util;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.install.PackageUtilsEx;

import android.content.Context;

public class SilentInstall {
	/*
	 * 用于记录当前的版本是否支持静默安装，省去每次判断时，都要进行一次check， 提高执行效率！huge at 13-4-16
	 */
	private static int IsSupportSilentInstall = 2;
	// 有第三方应用关闭所引起的安装失败的回调参数
	public final static int UNKNOWSOURCES = -2;
	public static boolean ischange = false;

	public static int SilentInstallApk(Context context, String apkPath){
		LogManager.LogShow("SilentInstallApk start context = " + context + ", apkPath = " + apkPath);
		int ret = PackageUtilsEx.install(context, apkPath);
		LogManager.LogShow("SilentInstallApk ret = " + ret);
		return ret;
	}
	
	public static int backgroundUnInstall(Context context, String pkgName){
		LogManager.LogShow("backgroundUnInstall start context = " + context + ", pkgName = " + pkgName);
		int ret = PackageUtilsEx.uninstall(context, pkgName);
		LogManager.LogShow("backgroundUnInstall ret = " + ret);
		return ret;
	}
	
	
	
//	public static void SilentInstallApk(Context context, String pkgName, String apkPath, IPackageInstallObserver ipo) {
//		LogManager.LogShow("SilentInstallApk: start::" + pkgName + "  apkPath:" + apkPath);
//		if (0 == IsSupportBGInstall(context)) {
//			backgroundInstall(context, pkgName, apkPath, ipo);
//		} else {
//			try {
//				File file = new File(apkPath);
//				Intent intent = new Intent();
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.setAction(android.content.Intent.ACTION_VIEW);
//				intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//				context.startActivity(intent);
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//				LogManager.LogShow("popupInstall: Exception : " + e.getMessage());
//			}
//		}
//		LogManager.LogShow("SilentInstallApk: end");
//	}

	/* 静默安装apk函数 huge at13-4-15 */
//	public static void backgroundInstall(Context context, String pkgName, String apkPath, IPackageInstallObserver ipo) {
//		int installFlags = 0;
//		PackageManager pm = context.getPackageManager();
//		try {
//			PackageInfo pi = pm.getPackageInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
//			if (pi != null) {
//				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
//			}
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//		try {
//			int result = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
//			if (result == 0) {
//				if (isChangeUnknownSources(context)) {
//					Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
//					ischange = true;
//				}
//			}
//			File file = new File(apkPath);
//			pm.installPackage(Uri.fromFile(file), new InstallCallback(context, ipo), installFlags, pkgName);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/* 静默卸载apk函数 huge at13-4-15 */
//	public static void backgroundUnInstall(Context context, String pkgName, IPackageDeleteObserver ipo) {
//		PackageManager pm = context.getPackageManager();
//		try {
//			pm.deletePackage(pkgName, ipo, 0);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	
	public static boolean bgInstallSupport(Context c){
		return 0 == IsSupportBGInstall(c);
	}
	/* 当前的版本是否支持静默安装功能 huge at13-4-15 */
	public static int IsSupportBGInstall(Context context) {
		if (IsSupportSilentInstall == 2) {
//			IsSupportSilentInstall = context.checkCallingOrSelfPermission(android.Manifest.permission.INSTALL_PACKAGES);
			IsSupportSilentInstall = PackageUtilsEx.isSystemApplication(context) ? 0 : 2;
			LogManager.LogShow("ret=====IsSupportBGInstall=======" + IsSupportSilentInstall);
		}
		return IsSupportSilentInstall;
	}

	/**
	 * 是否有打开第三方未知来源的权限
	 * 
	 * @param context
	 * @return ture 有权限 false没权限
	 */
//	public static boolean isChangeUnknownSources(Context context) {
//		int permission_WRITE_SETTINGS = context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SETTINGS);
//		int permission_WRITE_SECURE_SETTINGS = context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS);
//		if (permission_WRITE_SETTINGS == android.content.pm.PackageManager.PERMISSION_GRANTED && permission_WRITE_SECURE_SETTINGS == android.content.pm.PackageManager.PERMISSION_GRANTED) {
//			return true;
//		} else
//			return false;
//	}

//	private static class InstallCallback extends IPackageInstallObserver.Stub {
//		private IPackageInstallObserver ipo;
//		private Context context;
//
//		public InstallCallback(Context context, IPackageInstallObserver ipo) {
//			this.ipo = ipo;
//			this.context = context;
//		}
//
//		@Override
//		public void packageInstalled(String arg0, int arg1) throws RemoteException {
//			if (arg1 == 0) {
//				try {
//					int result = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
//					if (result == 1 && ischange) {
//						if (isChangeUnknownSources(context)) {
//							Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
//							LogManager.LogShow("INSTALL_NON_MARKET_APPS" + Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1));
//						}
//
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			if (arg1 == -1 && !isChangeUnknownSources(context)) {
//				ipo.packageInstalled(arg0, UNKNOWSOURCES);
//			} else {
//				ipo.packageInstalled(arg0, arg1);
//			}
//		}
//
//	}

}
