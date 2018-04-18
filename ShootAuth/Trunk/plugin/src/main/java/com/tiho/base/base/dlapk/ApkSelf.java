package com.tiho.base.base.dlapk;

public class ApkSelf {
	private String id;
	private int versionCode;
	private String md5;
	private String url;
	private String vername;
	private String name;
	private String mChangeLog;
	private int mApkSize;
	private String iconspath;
	private String icon;
	
	public String getId() {
		
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVername() {
		return vername;
	}

	public void setVername(String vername) {
		this.vername = vername;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getChangeLog() {
		return mChangeLog;
	}

	public void setChangeLog(String value) {
		this.mChangeLog = value;
	}

	public int getApkSize() {
		return mApkSize;
	}

	public void setApkSize(int size) {
		this.mApkSize = size;
	}

	public String getIconPath() {
		return iconspath;
	}

	public void setIconPath(String path) {
		this.iconspath = path;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public ApkSelf(){}
	
	public ApkSelf(int versionCode, String url, String md5){
		this.versionCode	= versionCode;
		this.url	= url;
		this.md5	= md5;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = " url = " + url + " md5 = " + md5 + " id = " + id + " versionCode = " + versionCode ;//+ " total = " + total + " current = " + current + " md5 = " + md5 + " savename = " + savename + " tmpname = " + tmpname;
		return str;
		// return super.toString();
	}
}
