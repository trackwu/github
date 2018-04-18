package com.tiho.dlplugin.dao;

import java.util.List;

import com.tiho.dlplugin.bean.ScheduleActivationBean;

public interface ScheduleActivationDAO {

	public String getName();
	
	public List<ScheduleActivationBean> getTodayTask();
	
	public ScheduleActivationBean get(String pack);
	
	
	public void saveScheduleList(long pushId,String pack , String timeract , boolean fromSilent);
	
	
}
