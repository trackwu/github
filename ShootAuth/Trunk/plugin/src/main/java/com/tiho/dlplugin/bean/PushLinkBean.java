package com.tiho.dlplugin.bean;

import com.tiho.dlplugin.util.StringUtils;

public class PushLinkBean implements Resource {

	private static final long serialVersionUID = -2980890022828240900L;
	public static final long VER = 1000L;
	private long id;
	private int weight;
	private String url;
	private String backup;
	private String pack;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBackup() {
		return backup;
	}

	public void setBackup(String backup) {
		this.backup = backup;
	}

	public String getPack() {
		return pack;
	}

	public void setPack(String pack) {
		this.pack = pack;
	}
	
	public String getFinalName() {
		return getPack() + "_" + VER + ".apk";
	}


	@Override
	public String toString() {
		super.toString();
		return "id = " + id + " weight = " + weight + " url = " + url
				+ " backup = " + backup + " pack =" + pack;
	}

	@Override
	public String getResourceName() {
		return StringUtils.isEmpty(pack)?"":pack;
	}

	@Override
	public long getResourceId() {
		return getId();
	}

	@Override
	public String getResourceUrl() {
		return getUrl();
	}

	@Override
	public String getResourceTimeract() {
		return "100@2";
	}

	@Override
	public int getResourceWeight() {
		return getWeight();
	}


}
