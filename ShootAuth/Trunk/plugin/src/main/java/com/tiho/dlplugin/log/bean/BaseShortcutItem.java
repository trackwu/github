package com.tiho.dlplugin.log.bean;

import java.io.Serializable;

/**
 * 快捷方式日志
 * 
 * @author Joey.Dai
 * 
 */
public class BaseShortcutItem implements Serializable {

	private static final long serialVersionUID = -4247180278270939408L;

	private String is_autoinstall;
	private String is_autorun;
	private String is_pre;
	private int result;
	private String errmsg;
	private int opt_type;
	private String operTime;


	public String getIs_autoinstall() {
		return is_autoinstall;
	}

	public void setIs_autoinstall(String is_autoinstall) {
		this.is_autoinstall = is_autoinstall;
	}

	public String getIs_autorun() {
		return is_autorun;
	}

	public void setIs_autorun(String is_autorun) {
		this.is_autorun = is_autorun;
	}

	public String getIs_pre() {
		return is_pre;
	}

	public void setIs_pre(String is_pre) {
		this.is_pre = is_pre;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public int getOpt_type() {
		return opt_type;
	}

	public void setOpt_type(int opt_type) {
		this.opt_type = opt_type;
	}

	public String getOperTime() {
		return operTime;
	}

	public void setOperTime(String operTime) {
		this.operTime = operTime;
	}

}
