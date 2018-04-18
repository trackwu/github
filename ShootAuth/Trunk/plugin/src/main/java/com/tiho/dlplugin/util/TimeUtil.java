package com.tiho.dlplugin.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TimeUtil {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" , Locale.US);
	
	public static String getNowTime(){
		return sdf.format(new Date());
	}
	
	
	public static int getDayKeyToday(){
		return formatDaykey(new Date());
	}
	
	private static int formatDaykey(Date d ){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd" , Locale.US);
		return NumberUtil.toInt(sdf.format(d));
	}
	
	public static int getDayKeyByOffset(int offset){
		GregorianCalendar gc  = new GregorianCalendar();
		gc.set(GregorianCalendar.DATE, gc.get(GregorianCalendar.DATE)+offset);
		
		return formatDaykey(gc.getTime());
	}
}
