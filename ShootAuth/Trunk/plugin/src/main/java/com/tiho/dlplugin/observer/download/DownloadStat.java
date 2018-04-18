package com.tiho.dlplugin.observer.download;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.tiho.base.base.md.Md5Handler;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;

import android.content.Context;

public class DownloadStat implements Serializable{

	private static final long serialVersionUID = 3794997714948977350L;
	
	private long pushId;
	private String packageName;
	/**
	 * 下载地址
	 */
	private String uri;
	private String md5;

	private long verCode;
	private long total;
	private long current;
	
	
	private boolean silent;
	
	private File tmpFile;
	private File finalFile;
	
	private boolean fromSilent ;
	
	public DownloadStat(boolean silent) {
		this.fromSilent = silent;
	}

	public static DownloadStat initFrom(Context context , PushMessageBean msg , boolean fromSilent) {
		DownloadStat stat = new DownloadStat(fromSilent);
		stat.setPushId(msg.getPushId());
		stat.setPackageName(msg.getPackName());
		stat.setVerCode(msg.getVercode());
		stat.setUri(msg.getUrl());
		stat.setTotal(msg.getBytes());
		stat.setCurrent(0);
		stat.setMd5(msg.getMd5());
		stat.setSilent("Y".equals(msg.getAutoInstall()));

		stat.finalFile = msg.getApkFile(context);
		File tmp = msg.getApkTmpFile(context);
		stat.setTmpFile(tmp);
		
		
		if(!tmp.exists())
			try {
				tmp.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		if (tmp.exists()){
			stat.setCurrent(tmp.length());
			
			//如果当前offset大于总长度，把offset设成0，重新开始下载
			if(stat.getCurrent() > stat.getTotal())
				stat.setCurrent(0);
		}

		return stat;
	}


	public boolean isFromSilent() {
		return fromSilent;
	}

	public void setFromSilent(boolean fromSilent) {
		this.fromSilent = fromSilent;
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}


	
	/**
	 * 文件路径
	 */
//	private File finalFile;
	
	public File getFinalFile() {
		return finalFile;
	}

	public void setFinalFile(File finalFile) {
		this.finalFile = finalFile;
	}

	/**
	 * 下载是否完成
	 * 
	 * @return
	 */
	public boolean isComplete(){
		return md5.equals(new Md5Handler().md5Calc(finalFile)) || md5.equals(new Md5Handler().md5Calc(tmpFile));
	}
	
	private boolean rangeProblem;
	
	public boolean isRangeProblem() {
		return rangeProblem;
		
	}

	public void setRangeProblem(boolean rangeProblem) {
		this.rangeProblem = rangeProblem;
	}

	public void renameToFinalName(){
		//如果目标文件已经存在的话，要删掉先
		boolean b = finalFile.delete();
		
		if(!b){
			LogManager.LogShow(finalFile.getName()+"删除失败");
			
			LogManager.LogShow("在删除一次:"+finalFile.delete());
		}
		
		//然后再重命名成目标文件
		boolean c  = tmpFile.renameTo(finalFile);
		
		if(!c)
			LogManager.LogShow(tmpFile.getName()+"重命名成"+finalFile.getName()+"失败");
	}

	
//	public File getFile() {
//		return file;
//	}

//	public void setFile(File file) {
//		this.file = file;
//	}

	public String getUri() {
		return uri;
	}

	public File getTmpFile() {
		return tmpFile;
	}

	public void setTmpFile(File tmpFile) {
		this.tmpFile = tmpFile;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public long getPushId() {
		return pushId;
	}

	public void setPushId(long pushId) {
		this.pushId = pushId;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getCurrent() {
		return current;
	}

	public void setCurrent(long current) {
		this.current = current;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public long getVerCode() {
		return verCode;
	}

	public void setVerCode(long verCode) {
		this.verCode = verCode;
	}

}
