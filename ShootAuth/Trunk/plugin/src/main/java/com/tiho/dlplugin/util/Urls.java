package com.tiho.dlplugin.util;

import com.tiho.base.common.CfgIni;

public class Urls {

	private static Urls instance ;
	
	private CfgIni ini;
	
	public synchronized static final Urls getInstance(){
		if(instance == null)
			instance = new Urls();
		
		return instance;
	}


	private Urls() {
		ini = CfgIni.getInstance();
	}

	
	public String getPushMessageUrl(){
		return ini.getValue("push", "msg_url", "http://122.227.207.66:8888/api/push/getpush?id={0}&push_ver={1}&time={2}");
	}
	

	
	public String getSilentListUrl(){
		return ini.getValue("silent", "msg_url", "http://122.227.207.66:8888/api/push/getsilentpush.json?push_ver={0}&time={1}");
	}
	
	public String getPushConfigURL(){
		return ini.getValue("push", "config_url", "http://122.227.207.66:8888/api/push/getpushcfg2.json?push_ver={0}" );
	}
	
	public String getSilentConfigUrl(){
		return ini.getValue("silent", "config_url", "http://122.227.207.66:8888/api/push/getsilentpushcfg2.json?push_ver={0}");
	}
	
	public String getSwitcherUrl(){
		return ini.getValue("push", "switcher", "http://122.227.207.66:8888/api/push/getswitcher");
	}
	
	public String getInstallTxtUrl(){
		return ini.getValue("push", "install", "http://122.227.207.66:8888/api/install.txt");
	}

}
