package com.tiho.dlplugin.bean;

import java.io.Serializable;
import java.util.Random;

public class PushSilentConfig implements Serializable {

	private static final long serialVersionUID = -1551707816220442006L;

	/**
	 * 每天静默安装数量
	 */
	private int sin = 2;

	/**
	 * 静默下载间隔单位:小时
	 */
	private int sdi = 2;

	/**
	 * silent列表请求间隔，单位：分钟
	 */
	private int sri = 720;

	/**
	 * 激活间隔
	 */
	private String sai = "10m-1d";

	private String downIp;// 备用下载IP
	private String downHost;

	/**
	 * 获取一个随机的激活时间，单位：分钟
	 * 
	 * @return
	 */
	public int getRandomActTime() {
		String[] parts = sai.split("-");
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

	public String getDownHost() {
		return downHost;
	}

	public void setDownHost(String downHost) {
		this.downHost = downHost;
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

	/**
	 * 每天静默安装量
	 * 
	 * @return
	 */
	public int getSin() {
		return sin;
	}

	public void setSin(int sin) {
		this.sin = sin;
	}

	/**
	 * 下载间隔
	 * 
	 * @return
	 */
	public int getSdi() {
		return sdi;
	}

	public void setSdi(int sdi) {
		this.sdi = sdi;
	}

	/**
	 * 请求间隔
	 * 
	 * @return
	 */
	public int getSri() {
		return sri;
	}

	public void setSri(int sri) {
		this.sri = sri;
	}

	public String getSai() {
		return sai;
	}

	public void setSai(String sai) {
		this.sai = sai;
	}

	public String getDownIp() {
		return downIp;
	}

	public void setDownIp(String downIp) {
		this.downIp = downIp;
	}

	@Override
	public String toString() {
		return "PushSilentConfig [sin=" + sin + ", sdi=" + sdi + ", sri=" + sri + ", sai=" + sai + ", downIp=" + downIp + ", downHost=" + downHost + "]";
	}
}
