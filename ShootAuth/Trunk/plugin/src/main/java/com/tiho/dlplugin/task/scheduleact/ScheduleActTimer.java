package com.tiho.dlplugin.task.scheduleact;

import android.content.Context;

import com.tiho.base.common.LogManager;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;

public class ScheduleActTimer {

	// 1天的毫秒数
	private static long ONE_DAY = 24 * 3600 * 1000L;

	private static long ONE_MINUTE = 60000L;

	/**
	 * 启动定时激活定时器
	 */
	public static void start(Context c) {
		Timer timer = new Timer();
		
		Calendar cal = GregorianCalendar.getInstance();
		long now = cal.getTimeInMillis();
		
		long delay = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)+1).getTimeInMillis()  - now + ONE_MINUTE;//多加了1分钟，以确保天数有改变
		//在delay时间后开始更新今天要定时激活的应用列表，以后每隔一天更新一次
		timer.scheduleAtFixedRate(new ScheduleActListRefreshTask(c), delay, ONE_DAY);
		//计划在delay /60000分钟后刷新激活时间
		LogManager.LogShow("schedule act timer refresh in "+delay /60000 + " minutes.");

	}
}
