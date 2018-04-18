package com.tiho.dlplugin.util;

import java.lang.reflect.Method;

import com.tiho.base.common.LogManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;

public class GPRSManager {

	private static final String CLASS_NAME = "[GPRSManager]";
	private ConnectivityManager mConnectivityManager;
	private TelephonyManager mTelephonyManager;
	private boolean mIsUseTelephonyManager;
	private static GPRSManager gameBaseGPRSManager = null;

	/**
	 * 当打开网络时sOpenGprs为true，则在退出后将网络关闭；如果没有打开网络则退出后不关闭 暂时取消 2014-5-8
	 */
	// private static boolean sOpenGprs = false;
	public synchronized static GPRSManager getInstance(Context context) {
		if (gameBaseGPRSManager == null) {
			LogManager.LogShow(CLASS_NAME + "GameBaseGPRSManager getInstance");
			gameBaseGPRSManager = new GPRSManager(context);
		}
		return gameBaseGPRSManager;
	}

	private GPRSManager(Context context) {
		mIsUseTelephonyManager = false;
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	/**
	 * 设置GPRS状态 Leo.Wu 2012-8-29 return boolean
	 */
	private boolean setMobileDataEnabled(boolean bEnable) {
		LogManager.LogShow(CLASS_NAME + "enter setMobileDataEnabled bEnable =" + bEnable);

		boolean result = false;

		if (bEnable) {
			// sOpenGprs = true;
			mIsUseTelephonyManager = false;
			result = setMobileDataByConnectivityManager("setMobileDataEnabled", bEnable);
			/**
			 * setMobileDataByITelephony对某些机型失败的同时，有负作用，先去除
			 */
			// if (!result) {
			// result = setMobileDataByITelephony(bEnable);
			// if (result) {
			// mIsUseTelephonyManager = true;
			// }
			// }
		} else {
			// if(!sOpenGprs){
			// LogManager.LogShow("setMobileDataEnabled sOpenGprs is false, don't colse gprs");
			// return true;
			// }
			if (mIsUseTelephonyManager) {
				result = setMobileDataByITelephony(bEnable);
			} else {
				result = setMobileDataByConnectivityManager("setMobileDataEnabled", bEnable);
			}
		}

		LogManager.LogShow("setMobileDataEnabled isConn = " + result + ", bEnable = " + bEnable);
		return result;
	}
	
	
	/**
	 * 打开移动网络
	 * @return
	 */
	public boolean openMobileNetwork(){
		return setMobileDataEnabled(true);
	}
	
	
	/**
	 * 关闭移动网络
	 * @return
	 */
	public boolean closeMobileNetwork(){
		return setMobileDataEnabled(false);
	}

	/**
	 * 由ConnectivityManager开启或者关闭GPRS Leo.Wu 2012-8-29 return boolean
	 */
	private boolean setMobileDataByConnectivityManager(String methodName, boolean isEnable) {
		LogManager.LogShow(CLASS_NAME + "enter setMobileDataByConnectivityManager isEnable=" + isEnable);
		boolean ret = false;
		Class<?> cmClass = mConnectivityManager.getClass();
		Class<?>[] argClasses = new Class[1];
		argClasses[0] = boolean.class;

		try {
			Method method = cmClass.getMethod(methodName, argClasses);
			method.invoke(mConnectivityManager, isEnable);
			ret = true;
		} catch (Exception e) {
			LogManager.LogShow(e);
			LogManager.LogShow(CLASS_NAME + "setMobileDataByConnectivityManager Exception:" + e);
			ret = false;
		}
		LogManager.LogShow("setMobileDataByConnectivityManager ret = " + ret);
		return ret;
	}

	/**
	 * 仅适用于2.3版本以下（即SDK小于等于8） 有负作用，不推荐使用
	 * 需要权限：android.permission.ACCESS_NETWORK_STATE
	 * ，android.permission.MODIFY_PHONE_STATE
	 * 
	 * @param context
	 * @param enable
	 * @return
	 */
	private boolean setMobileDataByITelephony(boolean enable) {
		boolean result = false;
		Class<?> telephonyManagerClass = null;
		Object ITelephonyStub = null;
		Class<?> ITelephonyClass = null;

		Method setDataConnectivityMethod = null;
		Method setApnTypeMethod = null;

		int sdk = Build.VERSION.SDK_INT;

		LogManager.LogShow(CLASS_NAME + "enter setMobileDataByITelephony enable:" + enable);
		if (Build.VERSION.SDK_INT > sdk) {
			LogManager.LogShow(CLASS_NAME + "sdk:" + sdk);
			return false;
		}

		try {
			telephonyManagerClass = Class.forName(mTelephonyManager.getClass().getName());
			Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
			getITelephonyMethod.setAccessible(true);
			ITelephonyStub = getITelephonyMethod.invoke(mTelephonyManager);
			ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

			if (enable) {
				setApnTypeMethod = ITelephonyClass.getDeclaredMethod("enableApnType", String.class);
				setDataConnectivityMethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");
			} else {
				setApnTypeMethod = ITelephonyClass.getDeclaredMethod("disableApnType", String.class);
				setDataConnectivityMethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
			}

			if (setApnTypeMethod != null && setDataConnectivityMethod != null) {
				setApnTypeMethod.setAccessible(true);
				setApnTypeMethod.invoke(ITelephonyStub, "default");

				setDataConnectivityMethod.setAccessible(true);
				setDataConnectivityMethod.invoke(ITelephonyStub);

				result = true;
			}
		} catch (Exception ex) {
			LogManager.LogShow(CLASS_NAME + "mobiledataconnect:", ex);
			result = false;
		}

		return result;
	}

}
