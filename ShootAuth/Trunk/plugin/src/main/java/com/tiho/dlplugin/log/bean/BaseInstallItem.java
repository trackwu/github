package com.tiho.dlplugin.log.bean;

import java.io.Serializable;

public class BaseInstallItem implements Serializable{

	private static final long serialVersionUID = -130009949617582617L;
	
	private String operTime;
	private int install_flag;
	private String errmsg;
	private String package_name;
	private int auto_install;
	private String auto_download;


	public String getOperTime() {
		return operTime;
	}

	public void setOperTime(String operTime) {
		this.operTime = operTime;
	}

	public int getInstall_flag() {
		return install_flag;
	}

	public void setInstall_flag(int install_flag) {
		this.install_flag = install_flag;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(String package_name) {
		this.package_name = package_name;
	}

	public int getAuto_install() {
		return auto_install;
	}

	public void setAuto_install(int auto_install) {
		this.auto_install = auto_install;
	}

	public String getAuto_download() {
		return auto_download;
	}

	public void setAuto_download(String auto_download) {
		this.auto_download = auto_download;
	}

}
