package com.tiho.dlplugin.log.bean;

import java.io.Serializable;

public class WakeupBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int type;

	public WakeupBean(int type) {
		super();
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
}
