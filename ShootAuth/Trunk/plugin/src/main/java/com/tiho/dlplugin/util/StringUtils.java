package com.tiho.dlplugin.util;

public class StringUtils {

	/**
	 * 是否是空字符串
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * 是否是空白内容
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	public static String stringAfter(String str, String pattern) {
		int index = str.indexOf(pattern);

		return index == -1 ? null : str.substring(index + pattern.length());
	}

	public static String stringBetween(String src , String begin , String end){
		String after = stringAfter(src, begin);
		if(after != null){
			int index = after.indexOf(end);
			if(index != -1)
				return after.substring(0, index);
		}
		
		return null;
	}
}
