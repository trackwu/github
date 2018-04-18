package com.tiho.dlplugin.dao;


public interface PushConfigDAO {

	public static final int PUSH_CONFIG_GOT = 1;
	
	
	
	public static final String TYPE_CONFIG = "config";
	
	public static final String TYPE_WITCH = "switch";
	
	public static final String TYPE_SILENT = "silentcfg";

	public <T> T getConfigByKey(String key, Class<T> c);

	public void updateConfig(String key, Object serial) throws Exception;

	public void saveConfig(String key, Object serial) throws Exception;

	
	public boolean overOneDay(String key)throws Exception;
}
