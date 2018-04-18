package com.tiho.dlplugin.bean;

public class ScheduleActivationBean {

	private long pushId;
	private String packName;
	private int day;
	private int rate;
	private int count;
	
	private boolean fromSilent ;
	
	
	public boolean isFromSilent() {
		return fromSilent;
	}
	public void setFromSilent(boolean fromSilent) {
		this.fromSilent = fromSilent;
	}
	public long getPushId() {
		return pushId;
	}
	public void setPushId(long pushId) {
		this.pushId = pushId;
	}
	public int getCount() {
		return count;
	}
	
	
	public void setCount(int count) {
		this.count = count;
	}
	public String getPackName() {
		return packName;
	}
	public void setPackName(String packName) {
		this.packName = packName;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	
	
	@Override
	public String toString() {
		return "ScheduleActivationBean [pushId=" + pushId + ", packName=" + packName + ", day=" + day + ", rate="
				+ rate + ", count=" + count + "]";
	}
	
	
	
}
