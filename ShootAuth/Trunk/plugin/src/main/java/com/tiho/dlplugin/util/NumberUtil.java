package com.tiho.dlplugin.util;

public class NumberUtil {

	
	
	public static long toLong(String s){
		try {
			
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			
			return 0 ;
		}
	}
	
	
	public static int toInt(String s){
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			
			return 0;
		}
		
	}
	
}
