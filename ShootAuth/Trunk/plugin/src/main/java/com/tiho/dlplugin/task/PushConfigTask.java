package com.tiho.dlplugin.task;

import android.content.Context;
import android.os.Handler;

import com.tiho.base.base.http.HttpHelper;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushConfigBean;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.util.StringUtils;
import com.tiho.dlplugin.util.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.MessageFormat;



/**
 * 定时更新config信息
 * @author Joey.Dai
 *
 */
public class PushConfigTask extends BaseTask{

	public PushConfigTask(Handler handler, Context context) {
		super(handler, context);
	}

	@Override
	protected void doTask() {
		String ver = CommonInfo.getInstance(context).getPluginVer();

		String url  =  MessageFormat.format(Urls.getInstance().getPushConfigURL(), ver);
		
		String response = HttpHelper.getInstance(context).simpleGet(url);

		if (!StringUtils.isEmpty(response)) {
			try {
				
				LogManager.LogShow("返回config:"+response);
				PushConfigBean config = toPushConfigBean(response);
				DAOFactory.getConfigDAO(context).saveConfig("config", config);
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	private PushConfigBean toPushConfigBean(String json) {

		PushConfigBean config = new PushConfigBean();

		JSONObject jo = null;
		try {
			jo = new JSONObject(json);
			config.setMaxShow(jo.getInt("maxShow"));
			config.setMaxPushNum(jo.getInt("maxPushNum"));
			config.setReqGap(jo.getInt("reqGap"));
			config.setPushGap(jo.getInt("pushGap"));
			config.setDownIp(jo.getString("downIp"));
			config.setDownHost(jo.getString("downHost"));

			JSONObject timeJo = jo.getJSONObject("pushTime");

			config.setBegin(Time.valueOf(timeJo.getString("begin")));
			config.setEnd(Time.valueOf(timeJo.getString("end")));
			config.setActTime(jo.getString("actTime"));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return config;
	}
	
	@Override
	protected void afterTask(Handler handler) {
		handler.postDelayed(new PushConfigTask(handler, context), 24*3600000L);

	}

}
