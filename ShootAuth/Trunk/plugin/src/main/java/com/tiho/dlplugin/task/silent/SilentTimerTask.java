package com.tiho.dlplugin.task.silent;

import android.content.Context;
import android.os.Handler;

import com.tiho.base.base.http.json.JsonUtil;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushSilentConfig;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.export.PushServiceImp;
import com.tiho.dlplugin.http.PushSilentDTO;
import com.tiho.dlplugin.task.BaseTask;
import com.tiho.dlplugin.util.NetworkUtil;
import com.tiho.dlplugin.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时获取静默消息
 * 
 * @author Joey.Dai
 * 
 */
public class SilentTimerTask extends BaseTask {

	private PushConfigDAO configDao;
	private SilentMessageWebRetrieve webMessageDao;

	public static boolean firstRecoverOfNetwork = true;

	private boolean causeByNetwork;
	
	private SilentManager silentManager;
	
	public SilentTimerTask(Handler handler, Context context, boolean networkCause) {
		super(handler, context);

		configDao = DAOFactory.getConfigDAO(context);
		webMessageDao = new SilentMessageWebRetrieve(context);
		silentManager = SilentManager.getInstance(context);
		
		causeByNetwork = networkCause;

	}

	@Override
	protected void doTask() {
		try {
			LogManager.LogShow("准备发送请求获取Silent消息");

			if (NetworkUtil.isNetworkOk(context)) {
				//如果是网络切换触发push获取，需要满足当前没有消息这个条件
				//如果是定时任务触发push获取，需要满足一定的请求间隔
				//2个条件只要满足一个发送请求
				
				if ((causeByNetwork && SilentList.getInstance(context).isEmpty()) || (!causeByNetwork && pushRequestIntervalEnough())) {
					requestSilent();
					PushStat.getInstance(context).resetSilentReqTime();
				}

			} else {
				LogManager.LogShow("当前没有网络连接，等待网络连接");
			}
		} catch (Exception e) {
			LogManager.LogShow("获取消息失败", e);
		}
	}

	private boolean pushRequestIntervalEnough() {
		PushStat stat = PushStat.getInstance(context);
		long interval = requestIntervalMilliseconds();

		long timePast = System.currentTimeMillis() - stat.getSilentReqTime();

		return timePast >= (interval / 2);
	}

	/**
	 * 请求push消息，并保存
	 * 
	 * @throws Exception
	 */
	private void requestSilent() throws Exception {
		// 网络获取push
		String response = webMessageDao.getPushMessage(true);
		LogManager.LogShow("Silent消息返回内容:" + response);

		if (!StringUtils.isEmpty(response)) {
			try {
				PushSilentDTO dto = JsonUtil.fromJson(response, PushSilentDTO.class);
				List<Resource> list = new ArrayList<Resource>();
				if(dto!=null){
					if(dto.getSilent()!=null){
						list.addAll(dto.getSilent());
					}
					if(dto.getLink()!=null){
						list.addAll(dto.getLink());
					}
					silentManager.addPushSilentList(list);
				}
			} catch (Exception e) {
				LogManager.LogShow("请求push消息失败", e);
			}
		}

	}
	

	private long requestIntervalMilliseconds() {
		long interval = 720 * 60000L;

		try {
			PushSilentConfig config = configDao.getConfigByKey("silentcfg", PushSilentConfig.class);
			long cfgSri = config.getSri() * 60000L;
			long min = PushServiceImp.sSilentSInterval == 0 ? 7200000L : PushServiceImp.sSilentSInterval; 
			interval = cfgSri < min ? min : cfgSri;
		} catch (Exception e) {
			LogManager.LogShow("SilentTimerTask requestIntervalMilliseconds ", e);
		}

		return interval;

	}

	@Override
	protected void afterTask(Handler handler) {

		if (!causeByNetwork) {
			long millis = requestIntervalMilliseconds();

			LogManager.LogShow("下一次请求静默push消息在 " + millis / 60000 + " 分钟之后.");

			handler.postDelayed(new SilentTimerTask(handler, context, false), millis);

		}

	}

}
