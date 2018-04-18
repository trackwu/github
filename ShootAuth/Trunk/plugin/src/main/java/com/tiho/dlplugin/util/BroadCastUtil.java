package com.tiho.dlplugin.util;

import java.util.LinkedList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;

public class BroadCastUtil {

	
	private static List<BroadcastReceiver> receivers = new LinkedList<BroadcastReceiver>();
	private static List<PendingIntent> pendings = new LinkedList<PendingIntent>();
	
	
	/**
	 * 把所有的广播接收器都取消掉
	 * @param c
	 */
	public static void removeAllReceivers(Context c){
		for (BroadcastReceiver br : receivers) {
			c.unregisterReceiver(br);
		}
	}
	
	public static void removeReceiver(BroadcastReceiver br , Context c){
		receivers.remove(br);
		c.unregisterReceiver(br);
	}
	/**
	 * 取消所有的定时任务
	 * @param c
	 */
	public static void cancelAllAlarmTask(Context c){
		AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
		for (PendingIntent pi : pendings) {
			am.cancel(pi);
		}
	}
	
	/**
	 * 注册广播接收事件
	 * 
	 * @param c
	 * @param action
	 *            要注册的intent action
	 * @param something
	 *            收到广播后要做的事情
	 */
	public static void registerReceiverEvent(Context c, final String action, BroadcastReceiver something) {
		IntentFilter filter = new IntentFilter();
		// 添加网络活动
		filter.addAction(action);

		// 注册该广播事件
		c.registerReceiver(something, filter);
		
		receivers.add(something);
	}

	/**
	 * 
	 * 注册定时任务，每隔一段时间发送一个指定action的intent
	 * 
	 * @param c
	 * @param interval
	 *            间隔，单位：毫秒
	 * @param action
	 *            intent的action
	 */
	public static void registerRepeatEvent(Context c, long interval, String action) {
		AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(action);
		PendingIntent pintent = PendingIntent.getBroadcast(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, pintent);
		
		pendings.add(pintent);
	}

	
	
	/**
	 * 注册一个在某个时间执行的任务
	 * 
	 * @param c
	 * @param when 时间
	 * @param action action
	 * @param data 要附带的数据
	 * @param relative 是否用相对时间 ， 如果是相对时间，when就是相对于设备当前流逝时间的偏移量。如果是否，就是UTC的绝对时间。
	 */
	public static void registerExactEvent(Context c, long when, String action , Bundle data , boolean relative) {
		AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(action);
		if(data != null)
			intent.putExtras(data);
		
		PendingIntent pintent = PendingIntent.getBroadcast(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if(relative)
			am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+when, pintent);
		else
			am.set(AlarmManager.RTC, when, pintent);
		
		pendings.add(pintent);
	}

}
