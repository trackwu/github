package com.tiho.dlplugin.log.bean;

import java.io.Serializable;

public class BaseOperateItem implements Serializable {

	private static final long serialVersionUID = -3231813631890654652L;

	/**
	 * 弹出到通知栏
	 */
	public static final int OP_TYPE_POP_UP = 0;

	/**
	 * 点击下载，但是没网络
	 */
	public static final int OP_TYPE_DOWNLOAD_NO_NETWORK = 1;

	/**
	 * 点击广告
	 */
	public static final int OP_TYPE_CLICK = 2;

	/**
	 * 应用已安装，消息不推送
	 */
	public static final int OP_TYPE_INSTALLED = 3;
	/**
	 * 激活操作
	 */
	public static final int OP_TYPE_ACTIVATED = 5;

	private int type;
	private int hasNetwork;
	private String operTime;
	private int operResult;
	private String package_name;
	private int invent_push_type;
	private String errmsg;
	

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(String package_name) {
		this.package_name = package_name;
	}

	public int getInvent_push_type() {
		return invent_push_type;
	}

	public void setInvent_push_type(int invent_push_type) {
		this.invent_push_type = invent_push_type;
	}


	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getHasNetwork() {
		return hasNetwork;
	}

	public void setHasNetwork(int hasNetwork) {
		this.hasNetwork = hasNetwork;
	}

	public String getOperTime() {
		return operTime;
	}

	public void setOperTime(String operTime) {
		this.operTime = operTime;
	}

	public int getOperResult() {
		return operResult;
	}

	public void setOperResult(int operResult) {
		this.operResult = operResult;
	}
}
