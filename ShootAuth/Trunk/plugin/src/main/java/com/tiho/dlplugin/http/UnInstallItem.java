package com.tiho.dlplugin.http;

import java.io.Serializable;

public class UnInstallItem implements Serializable{

	private static final long serialVersionUID = -8255912778820709412L;
	
	private String pack ; 
	private int time;
	
	public String getPack() {
		return pack;
	}
	public void setPack(String pack) {
		this.pack = pack;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	} 
	
}
