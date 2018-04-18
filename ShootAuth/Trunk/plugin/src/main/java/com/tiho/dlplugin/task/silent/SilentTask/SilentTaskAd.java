package com.tiho.dlplugin.task.silent.SilentTask;

import com.tiho.dlplugin.bean.PushLinkBean;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.display.ad.processer.UrlProcessor;
import com.tiho.dlplugin.display.ad.processer.UrlProcessorFactory;
import com.tiho.dlplugin.task.silent.SilentList;
import com.tiho.dlplugin.util.PackageUtil;

import android.content.Context;

public class SilentTaskAd extends SilentTask {

	public SilentTaskAd(Context context,SilentList list) {
		super(context,list);
	}

	@Override
	public void silentPush(Resource resource) {
		if (resource instanceof PushLinkBean){
			PushLinkBean pushLinkBean = (PushLinkBean) resource;
			loadWebview(pushLinkBean);
		}
	}

	private void loadWebview(PushLinkBean pushLinkBean) {
		UrlProcessor processor = null;
		if(PackageUtil.checkPermission(context, android.Manifest.permission.SYSTEM_ALERT_WINDOW)){
			//有权限先跳转 
			processor =UrlProcessorFactory.getProcesser(context, 3);
		}else{
			//没有权限直接跳浏览器
			processor =UrlProcessorFactory.getProcesser(context, 1);
		}
		setIsFirstDownload(false);
		processor.process(pushLinkBean.getUrl(),pushLinkBean.getResourceId(),UrlProcessorFactory.FROM_NEW_LINK);
	}


}
