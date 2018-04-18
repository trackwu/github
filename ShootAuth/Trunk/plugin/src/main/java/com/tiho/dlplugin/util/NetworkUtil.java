package com.tiho.dlplugin.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

public class NetworkUtil {

	
	public static boolean CLOSE_AFTER_USE = false;
	
	/**
	 * 判断是否有网络
	 * @param c
	 * @return
	 */
	public static boolean isNetworkOk(Context c){
		
		ConnectivityManager connectManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo info = connectManager.getActiveNetworkInfo();

		return info != null && info.isAvailable() && info.isConnected();
		
	}
	
	
	public static boolean isWifiOn(Context context){
		ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State state = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		
		if(state == State.CONNECTING || state  == State.CONNECTED)
			return true;
		return false;
	}
	
	/**
	 * 注册网络恢复时候的回调
	 * @param c
	 * @param something 恢复时候要做的事
	 */
	public static void registerNetworkRecoverEvent(final Context c  , BroadcastReceiver br){
		BroadCastUtil.registerReceiverEvent( c , ConnectivityManager.CONNECTIVITY_ACTION , br);
	}
}
