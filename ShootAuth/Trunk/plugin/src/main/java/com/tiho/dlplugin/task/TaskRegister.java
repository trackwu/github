package com.tiho.dlplugin.task;

import com.tiho.dlplugin.util.BroadCastUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;

/**
 * 定时任务注册
 * 
 * @author Joey.Dai
 * 
 */
public class TaskRegister {


	

	/**
	 * 注册在某个特定时间的定时任务
	 * @param context
	 * @param when 什么时候执行
	 * @param receive 接收器
	 * @param data 附带的数据
	 * @param relative 是否采用相对时间
	 */
	public static void registerExactTask(Context context ,  long when , BroadcastReceiver receive , Bundle data , boolean relative){
		
		String action = genActionName(receive);
		
		BroadCastUtil.registerReceiverEvent(context, action, receive);
		BroadCastUtil.registerExactEvent(context, when, action, data, relative);
		
	}
	
	
	private static String genActionName(BroadcastReceiver br){
		return  "me.pplay.playpush.action.task." + br.getClass().getName() + "." + System.nanoTime();
	}
}
