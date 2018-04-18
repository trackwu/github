package com.tiho.dlplugin.log.bean;

import java.io.Serializable;

public class UnInstallItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1797650972677547959L;
	
	
	private String package_name;
	private String operTime;
	private int uninstall_flag;
	
	private String errmsg;

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(String package_name) {
		this.package_name = package_name;
	}

	public String getOperTime() {
		return operTime;
	}

	public void setOperTime(String operTime) {
		this.operTime = operTime;
	}

	public int getUninstall_flag() {
		return uninstall_flag;
	}

	public void setUninstall_flag(int uninstall_flag) {
		this.uninstall_flag = uninstall_flag;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	

}
