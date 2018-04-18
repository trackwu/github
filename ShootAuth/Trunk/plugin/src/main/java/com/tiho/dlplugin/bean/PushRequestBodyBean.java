package com.tiho.dlplugin.bean;

public class PushRequestBodyBean {

	private String host_pack;
	private int auto_install;
	private long free_ram;
	private long free_sd;

	public String getHost_pack() {
		return host_pack;
	}

	public void setHost_pack(String host_pack) {
		this.host_pack = host_pack;
	}

	public int getAuto_install() {
		return auto_install;
	}

	public void setAuto_install(int auto_install) {
		this.auto_install = auto_install;
	}

	public long getFree_ram() {
		return free_ram;
	}

	public void setFree_ram(long free_ram) {
		this.free_ram = free_ram;
	}

	public long getFree_sd() {
		return free_sd;
	}

	public void setFree_sd(long free_sd) {
		this.free_sd = free_sd;
	}

}
