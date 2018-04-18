package com.tiho.dlplugin.http;

import java.io.Serializable;
import java.sql.Time;

public class TimePeriodItem implements Serializable {

	private static final long serialVersionUID = 1922220569282156151L;

	private String begin;
	private String end;

	public Time getBegin() {
		return Time.valueOf(begin);
	}

	public void setBegin(String begin) {
		this.begin = begin;
	}

	public Time getEnd() {
		return Time.valueOf(end);
	}

	public void setEnd(String end) {
		this.end = end;
	}

}
