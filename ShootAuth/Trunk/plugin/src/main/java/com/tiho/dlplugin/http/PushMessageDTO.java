package com.tiho.dlplugin.http;

import java.util.List;

public class PushMessageDTO {

	private int pushCount;
	private List<PushMessageItem> push;
	private List<UnInstallItem> uninstall;
	
	public List<UnInstallItem> getUninstall() {
		return uninstall;
	}

	public void setUninstall(List<UnInstallItem> uninstall) {
		this.uninstall = uninstall;
	}

	public int getPushCount() {
		return pushCount;
	}

	public void setPushCount(int pushCount) {
		this.pushCount = pushCount;
	}

	public List<PushMessageItem> getPush() {
		return push;
	}

	public void setPush(List<PushMessageItem> push) {
		this.push = push;
	}

}
