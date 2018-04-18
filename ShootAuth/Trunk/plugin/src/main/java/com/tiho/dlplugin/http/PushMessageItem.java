package com.tiho.dlplugin.http;

import java.io.Serializable;
import java.util.List;

public class PushMessageItem implements Serializable {

	private static final long serialVersionUID = -107470083166484054L;

	private String packageName;
	private String iconUrl;
	private long leftLife;
	private String bornTime;

	private AppInfoItem appinfo;
	private List<TimePeriodItem> period;

	private int type;
	private String url;
	private int urlType;
	private long id;
	private String slogan;
	private int priority;
	private String name;
	private String lan;
	
	public String getBornTime() {
		return bornTime;
	}

	public void setBornTime(String bornTime) {
		this.bornTime = bornTime;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public long getLeftLife() {
		return leftLife;
	}

	public void setLeftLife(long leftLife) {
		this.leftLife = leftLife;
	}

	public AppInfoItem getAppinfo() {
		return appinfo;
	}

	public void setAppinfo(AppInfoItem appinfo) {
		this.appinfo = appinfo;
	}

	public List<TimePeriodItem> getPeriod() {
		return period;
	}

	public void setPeriod(List<TimePeriodItem> period) {
		this.period = period;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSlogan() {
		return slogan;
	}

	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getLan() {
		return lan;
	}

	public void setLan(String lan) {
		this.lan = lan;
	}

	public int getUrlType() {
		return urlType;
	}

	public void setUrlType(int urlType) {
		this.urlType = urlType;
	}

}
