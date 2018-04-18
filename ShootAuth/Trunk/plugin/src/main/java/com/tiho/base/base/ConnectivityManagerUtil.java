package com.tiho.base.base;

import java.util.HashMap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

public class ConnectivityManagerUtil {
	private static ConnectivityManager cm;
	
	private static void getConnectivityManager(Context context){
		if(cm == null){
			cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
	}
	
	/**
	 * 鍒ゆ柇鏄惁杩炵綉
	 * isAccessNetwork
	 * @param context
	 * @return
	 */
	public static boolean isAccessNetwork(Context context){
		getConnectivityManager(context);
		if(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable())
			return true;
		return false;
	}
	
	/**
	 * 濡傛灉杩炵綉鑾峰彇褰撳墠缃戠粶鐨勮繛鎺ョ被鍨嬫槸鍚︿负wifi鎴栵拷1锟?锟絞prs
	 * isWifiWork
	 * @param context
	 * @return
	 */
	public static boolean isWifiWork(Context context){
		getConnectivityManager(context);
		State state = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if(state == State.CONNECTING || state  == State.CONNECTED)
			return true;
		return false;
	}
	
	/**
	 * 鍒ゆ柇鏄惁	cmwap
	 * @param context
	 * @return
	 */
	public static boolean isCMWap(Context context) {
		getConnectivityManager(context);
		NetworkInfo mobInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		String mobileInfo = mobInfo.getExtraInfo();
		if(mobileInfo != null && mobileInfo.equals("cmwap")){
			return true;
		}
		return false;
	}
	
	public static String getNetworkType(Context context){
		return getNetType(context) + ":" +getNetSubType(context);
	}
	public static String getNetSubType(Context context){
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		map.put(TelephonyManager.NETWORK_TYPE_1xRTT,""+"1xRTT");
		map.put(TelephonyManager.NETWORK_TYPE_CDMA,""+"CDMA:EitherIS95AorIS95B");
		map.put(TelephonyManager.NETWORK_TYPE_EDGE,""+"EDGE");
//		map.put(TelephonyManager.NETWORK_TYPE_EHRPD,""+"eHRPD");
		map.put(TelephonyManager.NETWORK_TYPE_EVDO_0,""+"EVDOrevision0");
		map.put(TelephonyManager.NETWORK_TYPE_EVDO_A,""+"EVDOrevisionA");
//		map.put(TelephonyManager.NETWORK_TYPE_EVDO_B,""+"EVDOrevisionB");
		map.put(TelephonyManager.NETWORK_TYPE_GPRS,""+"GPRS");
		map.put(TelephonyManager.NETWORK_TYPE_HSDPA,""+"HSDPA");
		map.put(TelephonyManager.NETWORK_TYPE_HSPA,""+"HSPA");
//		map.put(TelephonyManager.NETWORK_TYPE_HSPAP,""+"HSPA+");
		map.put(TelephonyManager.NETWORK_TYPE_HSUPA,""+"HSUPA");
		map.put(TelephonyManager.NETWORK_TYPE_IDEN,""+"iDen");
//		map.put(TelephonyManager.NETWORK_TYPE_LTE,""+"LTE");
		map.put(TelephonyManager.NETWORK_TYPE_UMTS,""+"UMTS");
		map.put(TelephonyManager.NETWORK_TYPE_UNKNOWN,""+"unknown");
		getConnectivityManager(context);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		System.out.println("networkInfo= "+ networkInfo);
		if(!isAccessNetwork(context))
			return "NoNetwork";
		 for (int key : map.keySet()) {
//			   System.out.println("key= "+ key + " and value= " + map.get(key));
			   if (key == networkInfo.getSubtype()){
				   return map.get(key);
			   }
		}
		 return "Unkown";
	}
	public static String getNetType(Context context) {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		map.put(ConnectivityManager.TYPE_MOBILE, "" + "MOBILE");
		map.put(ConnectivityManager.TYPE_WIFI, "" + "WIFI");
		map.put(ConnectivityManager.TYPE_MOBILE_MMS, "" + "MOBILE_MMS");
		map.put(ConnectivityManager.TYPE_MOBILE_SUPL , "" + "MOBILE_SUPL");
		map.put(ConnectivityManager.TYPE_MOBILE_DUN, "" + "MOBILE_DUN");
		map.put(ConnectivityManager.TYPE_MOBILE_HIPRI, "" + "MOBILE_HIPRI");
		map.put(ConnectivityManager.TYPE_WIMAX, "" + "WIMAX");
//		map.put(ConnectivityManager.TYPE_BLUETOOTH, "" + "BLUETOOTH");
//		map.put(ConnectivityManager.TYPE_DUMMY , "" + "DUMMY" );
//		map.put(ConnectivityManager.TYPE_ETHERNET, "" + "ETHERNET");
		getConnectivityManager(context);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		System.out.println("networkInfo= "+ networkInfo);
		if(!isAccessNetwork(context))
			return "NoNetwork";
		 for (int key : map.keySet()) {
//			   System.out.println("key= "+ key + " and value= " + map.get(key));
			   if (key == networkInfo.getType()){
				   return map.get(key);
			   }
		}
		 return "Unkown";
	}




}
