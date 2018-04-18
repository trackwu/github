package com.tiho.dlplugin.log.bean;

import java.io.Serializable;

public class BaseDownloadItem implements Serializable{

	private static final long serialVersionUID = -5154575100995755526L;
	
	private int from;
	private String package_name;
	private String startime;
	private String endtime;
	private String operTime;
	private long offset;
	private long totalsize;
	private int download_flag;
	private String errmsg;
	private int log_flag;
	private String auto_download;


	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(String package_name) {
		this.package_name = package_name;
	}

	public String getStartime() {
		return startime;
	}

	public void setStartime(String startime) {
		this.startime = startime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getOperTime() {
		return operTime;
	}

	public void setOperTime(String operTime) {
		this.operTime = operTime;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getTotalsize() {
		return totalsize;
	}

	public void setTotalsize(long totalsize) {
		this.totalsize = totalsize;
	}

	public int getDownload_flag() {
		return download_flag;
	}

	public void setDownload_flag(int download_flag) {
		this.download_flag = download_flag;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public int getLog_flag() {
		return log_flag;
	}

	public void setLog_flag(int log_flag) {
		this.log_flag = log_flag;
	}

	public String getAuto_download() {
		return auto_download;
	}

	public void setAuto_download(String auto_download) {
		this.auto_download = auto_download;
	}

}
