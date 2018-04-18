package com.tiho.dlplugin.http;

import java.io.Serializable;

public class AppInfoItem implements Serializable {

	private static final long serialVersionUID = 8866378253233199855L;
	
	private String auto_run;
	private long bytes;
	private String packageName;
	private String appname;
	private String md5;
	private int showtype;
	private String auto_install;
	private String timeract;
	private long vercode;

	
	
	public String getTimeract() {
		return timeract;
	}

	public void setTimeract(String timeract) {
		this.timeract = timeract;
	}

	public String getAuto_run() {
		return auto_run;
	}

	public void setAuto_run(String auto_run) {
		this.auto_run = auto_run;
	}

	public long getBytes() {
		return bytes;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public int getShowtype() {
		return showtype;
	}

	public void setShowtype(int showtype) {
		this.showtype = showtype;
	}

	public String getAuto_install() {
		return auto_install;
	}

	public void setAuto_install(String auto_install) {
		this.auto_install = auto_install;
	}

	public long getVercode() {
		return vercode;
	}

	public void setVercode(long vercode) {
		this.vercode = vercode;
	}

}
