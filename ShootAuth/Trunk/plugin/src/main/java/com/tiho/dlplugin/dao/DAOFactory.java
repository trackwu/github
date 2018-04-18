package com.tiho.dlplugin.dao;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import com.tiho.dlplugin.dao.impl.PushConfigFileDAOImpl;
import com.tiho.dlplugin.dao.impl.PushMessageFileDAOImpl;
import com.tiho.dlplugin.dao.impl.PushUninstallDAOImpl;
import com.tiho.dlplugin.dao.impl.ScheduleActivationDAOImpl;
import com.tiho.dlplugin.dao.impl.ScheduleActivationSilentDAOImpl;
import com.tiho.dlplugin.dao.impl.SilentPushMessageFileDAOImpl;
import com.tiho.dlplugin.task.silent.dao.IndepentSilnetDAO;
import com.tiho.dlplugin.task.silent.dao.IndepentSilnetDAOImpl;

import android.content.Context;

public class DAOFactory {

	// simple dao container for cache
	private static HashMap<String, Object> map = new HashMap<String, Object>();

	public static SilentPushMessageDAO getSilentPushDAO(Context context) {
		return _new("silent", SilentPushMessageFileDAOImpl.class, context);
	}

	public static PushMessageDAO getPushMessageDAO(Context context) {
		return _new("message", PushMessageFileDAOImpl.class, context);
	}

	public static PushUninstallDAO getUninstallDAO(Context context) {
		return _new("uninstall", PushUninstallDAOImpl.class, context);
	}

	public static PushConfigDAO getConfigDAO(Context context) {
		return _new("conifg", PushConfigFileDAOImpl.class, context);
	}

	/**
	 * 之前的定时激活
	 * @param context
	 * @return
	 */
	public static ScheduleActivationDAO getScheduleActRegularDAO(Context context) {
		return _new("schedule_act", ScheduleActivationDAOImpl.class, context);
	}
	
	
	/**
	 * 静默激活
	 * @param context
	 * @return
	 */
	public static ScheduleActivationDAO getScheduleActSilentDAO(Context context) {
		return _new("schedule_act_silent", ScheduleActivationSilentDAOImpl.class, context);
	}
	
	public static IndepentSilnetDAO getIndepentSilnetDAO(Context context) {
		return _new("indepent_silent", IndepentSilnetDAOImpl.class, context);
	}

	private static <T> T _new(String key, Class<T> c, Object... args) {
		T dao = (T) map.get(key);
		if (dao == null) {
			Constructor<T> constt;
			try {
				constt = c.getConstructor(Context.class);
				dao = constt.newInstance(args);
				map.put(key, dao);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		return dao;
	}

}
