package com.tiho.dlplugin.util;

import com.tiho.base.common.LogManager;

public class ErrorHandler {

	public static String getErrMsg(String pref, Exception e) {

		String msg = e.getMessage();

		if (StringUtils.isEmpty(msg) || "null".equalsIgnoreCase(msg)) {
			msg = LogManager.LogShow(e);
		}

		String s = pref + ":" + msg;

		if (s.length() > 200) {
			return s.substring(0, 200);
		}
		return s;

	}
}
