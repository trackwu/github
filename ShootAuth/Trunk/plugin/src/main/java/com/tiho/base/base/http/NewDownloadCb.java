package com.tiho.base.base.http;

import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.observer.download.DownloadStat;


/**
 * v48新下载，下载结果回调接口
 *
 */
public interface NewDownloadCb {

	/**
	 * 下载结果
	 * @param stat DownloadStat
	 * @param msg PushSilentBean
	 */
	public void callback(DownloadStat stat, PushSilentBean psb, PushMessageBean pmb);
	
}
