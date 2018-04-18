package com.tiho.base.base.md;

public class MdItem {
	private String packname="";
	private int file_version=0;
	private String url="";
	private long filesize = 0l;
	private String md5 ="";
	
	public String getPackname() {
		return packname;
	}
	public void setPackname(String packname) {
		this.packname = packname;
	}
	public int getFile_version() {
		return file_version;
	}
	public void setFile_version(int file_version) {
		this.file_version = file_version;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getFilesize() {
		return filesize;
	}
	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
}
