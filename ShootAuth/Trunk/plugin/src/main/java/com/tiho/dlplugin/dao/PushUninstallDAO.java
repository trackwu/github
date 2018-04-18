package com.tiho.dlplugin.dao;

import java.util.List;

import com.tiho.dlplugin.bean.PushUninstallBean;

public interface PushUninstallDAO {

	public List<PushUninstallBean> getUninstallAppList();
	
	
	public void saveUninstallList(List<PushUninstallBean> list);
	
	
	public void deleteUninstallApp(String pack);
	
	
}
