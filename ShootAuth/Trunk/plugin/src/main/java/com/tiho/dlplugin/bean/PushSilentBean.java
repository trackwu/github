package com.tiho.dlplugin.bean;

import java.io.File;

import com.tiho.dlplugin.util.PushDirectoryUtil;

import android.content.Context;

public class PushSilentBean implements Resource ,Comparable<PushSilentBean> {

	private static final long serialVersionUID = 7798345239469481333L;
	
	private String path;
	private String md5;
	private long size;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
	private int weight;
	private String timeract;
	private String pack;
	private long ver;
	private long id;
	
	
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
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getTimeract() {
		return timeract == null ? null : timeract.trim();
	}
	public void setTimeract(String timeract) {
		this.timeract = timeract;
	}
	public String getPack() {
		return pack;
	}
	public void setPack(String pack) {
		this.pack = pack;
	}
	public long getVer() {
		return ver;
	}
	public void setVer(long ver) {
		this.ver = ver;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof PushSilentBean)
			return md5.equals(((PushSilentBean)o).getMd5());
		
		return super.equals(o);
	}
	
	
	@Override
	public int hashCode() {
		return md5.hashCode();
	}
	/**
	 * apk下载完之后的文件
	 * 
	 * @param context
	 * @return
	 */
	public File getApkFile(Context context){
		File dir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.SILENT_DOWNLOAD_DIR);
		return new File(dir, getPack() + "_" + getVer() + ".apk");
	}
	
	
	/**
	 * apk下载时的临时文件
	 * 
	 * @param c
	 * @return
	 */
	public File getApkTmpFile(Context c){
		File dir = PushDirectoryUtil.getDir(c, PushDirectoryUtil.SILENT_DOWNLOAD_DIR);
		
		return new File(dir, getPath().hashCode()+".tmp");
	}
	@Override
	public int compareTo(PushSilentBean another) {
		if(getWeight() > another.getWeight())
			return -1;
		else if(getWeight() < another.getWeight())
			return 1;
		
		else if(getSize() < another.getSize())
			return -1;
		else if(getSize() > another.getSize())
			return 1;
		
		return 0;
	}
	
	@Override
	public String toString() {
		super.toString();
		return "id ="+id+" path="+path+" md5="+md5+" size="+size+" weighit"+weight+" timeract"+timeract+" pack"+pack+" ver"+ver;
	}
	@Override
	public String getResourceName() {
		return getPack();
	}
	@Override
	public long getResourceId() {
		return getId();
	}
	@Override
	public String getResourceUrl() {
		return getPath();
	}
	@Override
	public String getResourceTimeract() {
		return getTimeract();
	}
	@Override
	public int getResourceWeight() {
		return getWeight();
	}
	
	
}
