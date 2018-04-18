package com.tiho.dlplugin.bean;

import java.io.Serializable;

public interface Resource extends Serializable {
	
	public String getResourceName();//返回pack
	
	public long getResourceId();
	
	public String getResourceUrl();//返回url或者path
	
	public String getResourceTimeract();
	
	public int getResourceWeight();
	
}
