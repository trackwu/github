package com.tiho.dlplugin.task.scheduleact;

import java.util.TimerTask;

import android.content.Context;


/**
 * 每天零时更新今天要定时激活的应用列表
 * @author Joey.Dai
 *
 */
public class ScheduleActListRefreshTask extends TimerTask {

	private Context context;
	
	
	
	public ScheduleActListRefreshTask(Context context) {
		super();
		this.context = context;
	}



	@Override
	public void run() {
		ScheduleActList.getInstance(context).reset();
		ScheduleActList.getInstance(context).resetTodayDone();
	}

}
