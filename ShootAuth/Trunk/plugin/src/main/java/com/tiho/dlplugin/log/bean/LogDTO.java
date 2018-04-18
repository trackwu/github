package com.tiho.dlplugin.log.bean;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class LogDTO<T> implements Serializable {

	private static final long serialVersionUID = 1114540882856702520L;
	private int count;
	private String host_pack;
	
	private List<T> useroper;
	
	
	public void addItem(T t){
		if(useroper == null)
			useroper = new LinkedList<T>();
		
		count++;
		useroper.add(t);
	}
	
	
	public void addItemAll(List<T> t){
		if(useroper == null)
			useroper = new LinkedList<T>();
		
		useroper.addAll(t);
		this.count += t.size();
	}
	
	public List<T> getUseroper() {
		return useroper;
	}

	public void setUseroper(List<T> useroper) {
		this.useroper = useroper;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getHost_pack() {
		return host_pack;
	}

	public void setHost_pack(String host_pack) {
		this.host_pack = host_pack;
	}

}
