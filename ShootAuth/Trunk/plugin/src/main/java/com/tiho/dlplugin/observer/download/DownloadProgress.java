package com.tiho.dlplugin.observer.download;

public interface DownloadProgress {

	public void downloaded(long total , long now);
	
}
