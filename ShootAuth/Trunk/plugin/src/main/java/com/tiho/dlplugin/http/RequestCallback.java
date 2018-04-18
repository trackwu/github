package com.tiho.dlplugin.http;

public abstract class RequestCallback implements Runnable {

	private Object data;
	
	
	public Object getData() {
		return data;
	}


	public void setData(Object data) {
		this.data = data;
	}


}
