package com.tiho.dlplugin.task.silent.SilentTask;

import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.observer.download.FileDownloader;
import com.tiho.dlplugin.task.silent.SilentList;

import android.content.Context;

public abstract class SilentTask {

	protected Context context;
	protected FileDownloader fileDownloader;
	private static boolean isFirstDownload = true;
	protected SilentList list;
	
	public SilentTask(Context context,SilentList list) {
		this.context =context;
		this.list =list; 
		fileDownloader = new FileDownloader();
	}
	
	public abstract void silentPush(Resource resource);
	
	public static boolean getIsFirstDownload(){
		return isFirstDownload;
	}
	public static void setIsFirstDownload(boolean ifd){
		SilentTask.isFirstDownload =ifd;
	}
}

