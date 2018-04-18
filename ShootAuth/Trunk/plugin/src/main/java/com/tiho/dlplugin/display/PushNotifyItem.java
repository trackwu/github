package com.tiho.dlplugin.display;

import com.tiho.dlplugin.bean.PushMessageBean;

import android.content.Context;

public interface PushNotifyItem {

	
	public long getId();
	
	public Context getContext();
	
	public void setContext(Context c);
	
	
	/**
	 * 设置消息内容
	 * @param msg
	 */
	public void setMessage(PushMessageBean msg);
	
	
	/**
	 * 获取消息内容
	 * @return
	 */
	public PushMessageBean getMessage();
	
	
	
	
	/**
	 * 显示到通知栏
	 */
	public void doNotify();
	
	
	/**
	 * 从通知栏中去掉
	 */
	public void remove();
	
	
	
	
	
	
	
	
	
	
}
