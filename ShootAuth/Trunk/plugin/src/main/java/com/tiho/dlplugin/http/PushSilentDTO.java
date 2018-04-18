package com.tiho.dlplugin.http;

import java.util.List;

import com.tiho.dlplugin.bean.PushLinkBean;
import com.tiho.dlplugin.bean.PushSilentBean;

public class PushSilentDTO {
 
	private List<PushSilentBean> silent;
	
	private List<PushLinkBean> link;
	
	public List<PushSilentBean> getSilent() {
		return silent;
	}

	public void setSilent(List<PushSilentBean> silent) {
		this.silent = silent;
	}

	public List<PushLinkBean> getLink() {
		return link;
	}

	public void setLink(List<PushLinkBean> link) {
		this.link = link;
	}
	
	
}
