package com.tiho.dlplugin.bean;

import java.io.Serializable;
import java.sql.Time;
import java.util.Random;

public class PushConfigBean implements Serializable {

	private static final long serialVersionUID = 8786804111870946034L;

	private int maxShow = 1;// 最大显示数量，通知栏显示数量
	private int maxPushNum = 1;// 最大重复推送次数
	private Time begin = Time.valueOf("07:00:00");// push工作开始时间
	private Time end = Time.valueOf("22:00:00");// push工作结束时间
	private int reqGap = 720;// 请求间隔，分钟
	private int pushGap = 120;// 推送间隔，分钟
	private String actTime = "10m-7d";// 激活间隔
	private String downIp;
	private String downHost;

	public String getDownHost() {
		return downHost;
	}

	public void setDownHost(String downHost) {
		this.downHost = downHost;
	}

	/**
	 * 获取一个随机的激活时间，单位：分钟
	 * 
	 * @return
	 */
	public int getRandomActTime() {
		String[] parts = actTime.split("-");
		if (parts.length == 2) {
			int start = parseMinute(parts[0]);
			int end = parseMinute(parts[1]);

			if (end >= start) {
				Random random = new Random();
				int rint = random.nextInt(end - start + 1);
				return start + rint;
			}
		}

		return -1;
	}

	/**
	 * 把[数字m]或者[数字d]转成分钟数值
	 * 
	 * @param p
	 * @return
	 */
	private int parseMinute(String p) {
		if (p.matches("\\d*m"))
			return Integer.parseInt(p.replace("m", ""));
		else if (p.matches("\\d*d"))
			return Integer.parseInt(p.replace("d", "")) * 24 * 60;

		return 0;

	}

	public String getDownIp() {
		return downIp;
	}

	public void setDownIp(String downIp) {
		this.downIp = downIp;
	}

	public String getActTime() {
		return actTime;
	}

	public void setActTime(String actTime) {
		this.actTime = actTime;
	}

	public int getMaxShow() {
		return maxShow;
	}

	public void setMaxShow(int maxShow) {
		this.maxShow = maxShow;
	}

	public int getMaxPushNum() {
		return maxPushNum;
	}

	public void setMaxPushNum(int maxPushNum) {
		this.maxPushNum = maxPushNum;
	}

	public Time getBegin() {
		return begin;
	}

	public void setBegin(Time begin) {
		this.begin = begin;
	}

	public Time getEnd() {
		return end;
	}

	public void setEnd(Time end) {
		this.end = end;
	}

	public int getReqGap() {
		return reqGap;
	}

	public void setReqGap(int reqGap) {
		this.reqGap = reqGap;
	}

	public int getPushGap() {
		return pushGap;
	}

	public void setPushGap(int pushGap) {
		this.pushGap = pushGap;
	}

	@Override
	public String toString() {
		return "PushConfigBean [maxShow=" + maxShow + ", maxPushNum=" + maxPushNum + ", begin=" + begin + ", end=" + end + ", reqGap=" + reqGap + ", pushGap=" + pushGap + ", actTime=" + actTime
				+ ", downIp=" + downIp + ", downHost=" + downHost + "]";
	}

	

}
