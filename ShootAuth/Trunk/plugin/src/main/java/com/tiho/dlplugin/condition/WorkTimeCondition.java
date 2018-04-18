package com.tiho.dlplugin.condition;

import java.sql.Time;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushConfigBean;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;

import android.content.Context;

/**
 * 
 * 判断工作条件
 * 
 * @author Joey.Dai
 * 
 */
public class WorkTimeCondition implements Condition {

	private static final long oneDay = 24*3600000L;
	
	private PushConfigDAO configDao;

	@Override
	public boolean onCondition(Context c) {
		if (configDao == null)
			configDao = DAOFactory.getConfigDAO(c);

		boolean result = false;

		try {
			PushConfigBean config = configDao.getConfigByKey("config", PushConfigBean.class);

			LogManager.LogShow("work time check:"+config.toString());
			
			Time begin = config.getBegin();
			Time end = config.getEnd();
			
			LogManager.LogShow("begin:"+begin+",end:"+end);

			Time now = new Time(System.currentTimeMillis() % oneDay);
			
			LogManager.LogShow("now:"+now+",beginTime:"+begin+".endTime:"+end);

			result = now.getTime() >= begin.getTime() && now.getTime() <= end.getTime();

		} catch (Exception e) {
			LogManager.LogShow(e);
		}

		LogManager.LogShow("是否处于工作时间中:" + result);

		return result;
	}


}
