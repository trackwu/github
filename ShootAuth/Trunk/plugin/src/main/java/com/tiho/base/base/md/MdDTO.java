package com.tiho.base.base.md;

import java.util.ArrayList;
import java.util.List;


public class MdDTO {
	private List<MdItem> params=new ArrayList<MdItem>();
	private String time;
	
	

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public List<MdItem> getParams() {
		return params;
	}

	public void setParams(List<MdItem> params) {
		this.params = params;
	}
	
}
