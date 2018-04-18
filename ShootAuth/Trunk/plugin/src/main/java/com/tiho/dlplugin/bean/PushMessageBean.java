
package com.tiho.dlplugin.bean;

import java.io.File;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.tiho.dlplugin.util.PushDirectoryUtil;

import android.content.Context;

public class PushMessageBean implements Serializable {

	private static final long serialVersionUID = -5165368186291098932L;

	public static final int TYPE_AD = 1;
	public static final int TYPE_APP = 2;

	private long pushId;
	private int pushType;
	private int weight;
	private Timestamp expireTime;
	
	private String lan;

	private String title;
	private String slogan;

	private String icon;
	private String url;
	private int urlType;

	// for app---------------------
	private String appname;
	private String md5;
	private long bytes;
	private long vercode;
	private String packName;
	private int showtype;
	private String autoRun;
	private String autoInstall;
	private String timeract;

	// end of app info-------------

	// 最优时间，pair表示起始时间和结束时间
	private List<com.tiho.dlplugin.util.Pair<Time, Time>> bestTimes = new ArrayList<com.tiho.dlplugin.util.Pair<Time, Time>>();
	
	//self property***************************
	private boolean done = false;
	//****************************************
	
	public Long getPushId() {
		return pushId;
	}

	public boolean isDone() {
		return done;
	}

	public String getTimeract() {
		return timeract;
	}

	public void setTimeract(String timeract) {
		this.timeract = timeract;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public void setPushId(Long pushId) {
		this.pushId = pushId;
	}

	public int getUrlType() {
		return urlType;
	}

	public void setUrlType(int urlType) {
		this.urlType = urlType;
	}

	public Integer getPushType() {
		return pushType;
	}

	public void setPushType(Integer pushType) {
		this.pushType = pushType;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}


	public Timestamp getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Timestamp expireTime) {
		this.expireTime = expireTime;
	}

	public String getAutoRun() {
		return autoRun;
	}

	public void setAutoRun(String autoRun) {
		this.autoRun = autoRun;
	}

	public String getAutoInstall() {
		return autoInstall;
	}

	public void setAutoInstall(String autoInstall) {
		this.autoInstall = autoInstall;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPackName() {
		return packName;
	}

	public void setPackName(String packName) {
		this.packName = packName;
	}

	public String getSlogan() {
		return slogan;
	}

	public int getShowtype() {
		return showtype;
	}

	public void setShowtype(int showtype) {
		this.showtype = showtype;
	}

	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}

	public List<com.tiho.dlplugin.util.Pair<Time, Time>> getBestTimes() {
		return bestTimes;
	}

	public void setBestTimes(List<com.tiho.dlplugin.util.Pair<Time, Time>> bestTimes) {
		this.bestTimes = bestTimes;
	}

	public String getLan() {
		return lan;
	}

	public void setLan(String lan) {
		this.lan = lan;
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


	public long getBytes() {
		return bytes;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	public long getVercode() {
		return vercode;
	}

	public void setVercode(long vercode) {
		this.vercode = vercode;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public File getIconFile(Context c){
		return PushDirectoryUtil.getFileInPushBaseDir(c, PushDirectoryUtil.ICON_DIR, String.valueOf(getIcon().hashCode()));
	}
	
	
	/**
	 * apk下载完之后的文件
	 * 
	 * @param context
	 * @return
	 */
	public File getApkFile(Context context){
		File dir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.DOWNLOAD_DIR);
		return new File(dir, getPackName() + "_" + getVercode() + ".apk");
	}
	
	
	/**
	 * apk下载时的临时文件
	 * 
	 * @param c
	 * @return
	 */
	public File getApkTmpFile(Context c){
		File dir = PushDirectoryUtil.getDir(c, PushDirectoryUtil.DOWNLOAD_DIR);
		
		return new File(dir, getUrl().hashCode()+".tmp");
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o != null && o instanceof PushMessageBean){
			PushMessageBean msg = (PushMessageBean)o ;
			
			return this.getPushId().longValue() == msg.getPushId().longValue() ;
			
		}
		
		return super.equals(o);
	}

	@Override
	public String toString() {
		return "PushMessageBean [pushId=" + pushId + ", pushType=" + pushType + ", weight=" + weight + ", expireTime=" + expireTime + ", lan=" + lan + ", title=" + title + ", slogan=" + slogan
				+ ", icon=" + icon + ", urlType=" + urlType+ ", url=" + url + ", appname=" + appname + ", md5=" + md5 + ", bytes=" + bytes + ", vercode=" + vercode + ", packName=" + packName + ", showtype=" + showtype
				+ ", autoRun=" + autoRun + ", autoInstall=" + autoInstall + ", bestTimes=" + bestTimes + "]";
	}
	
	
}
