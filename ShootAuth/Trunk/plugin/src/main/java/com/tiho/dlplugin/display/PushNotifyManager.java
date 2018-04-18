package com.tiho.dlplugin.display;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushConfigBean;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.log.bean.OperateItem;

import android.content.Context;
import android.os.Handler;

/**
 * 
 * Push通知栏管理器
 * 
 * @author Joey.Dai
 * 
 */
public class PushNotifyManager {

	private List<PushNotifyItem> items;//正在显示中的条目

	private Handler handler = new Handler();

	private static PushNotifyManager instance;

	private Map<Long, Integer> pushNum;//每日推送次数

	private Context context;
	
	private int dayOfMonth ; //每月的日期

	public synchronized static final PushNotifyManager getInstance(Context context) {
		if (instance == null) {
			instance = new PushNotifyManager(context);
		}
		return instance;
	}

	private PushNotifyManager(Context context) {
		super();
		
		this.context = context;
		
		items = new LinkedList<PushNotifyItem>();

		pushNum = new HashMap<Long, Integer>();
		
		dayOfMonth = getDayOfMonth() ;
	}

	
	private int getDayOfMonth(){
		GregorianCalendar gc = new GregorianCalendar() ; 
		return  gc.get(GregorianCalendar.DATE);
	}
	
	/**
	 * 是否在显示中
	 * 
	 * @param id
	 *            pushId
	 * @return
	 */
	public boolean isDisplaying(long id) {
		for (PushNotifyItem item : items) {
			if (item.getId() == id)
				return true;
		}

		return false;
	}

	/**
	 * 某条消息是否超过每日的推送上限
	 * 
	 * @param id
	 *            pushId
	 * @return true 超过 false 没超过
	 * @throws Exception
	 */
	public boolean overLimit(long id) throws Exception {

		PushConfigBean config = DAOFactory.getConfigDAO(context).getConfigByKey("config", PushConfigBean.class);

		Integer num = pushNum.get(id);
		boolean result =  num == null ? false : num == config.getMaxPushNum();
		
		LogManager.LogShow(id+"是否查过每日上限:"+result);
		
		return result;
	}

	
	/**
	 * 是否已经达到了通知栏的最大显示条数
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean isFull() throws Exception {
		PushConfigBean config = DAOFactory.getConfigDAO(context).getConfigByKey("config", PushConfigBean.class);

		LogManager.LogShow("通知栏最大消息树：" + config.getMaxShow() + ",当前数目:" + items.size());

		return config.getMaxShow() == items.size();
	}
	
	

	public void doNotify(PushMessageBean msg) throws Exception {
		if(dayOfMonth != getDayOfMonth()){
			pushNum.clear();
			dayOfMonth = getDayOfMonth() ;
		}
		
		
		if (isFull()) {
			LogManager.LogShow("通知栏达到最到显示数目，把第一个去掉");

			PushNotifyItem item = items.remove(0);
			item.remove();

			LogManager.LogShow(item.getMessage().getPushId() + "已经从通知栏中去掉");
		}

		PushNotifyItem notifyItem = null;

		if (msg.getPushType() == PushMessageBean.TYPE_AD) {
			notifyItem = new PushNotifyItemAD(context, this, msg);
		} else if (msg.getPushType() == PushMessageBean.TYPE_APP) {
			notifyItem = new PushNotifyItemApp(context, this, msg);
		}

		if (notifyItem != null) {
			
			LogUploadManager.getInstance(context).addOperateLog(msg.getPushId(), msg.getPushType(), OperateItem.OP_TYPE_POP_UP, msg.getPackName(), -1,"PUSH_POP_UP");

			items.add(notifyItem);
			increasePushNum(msg.getPushId());
			
			notifyItem.doNotify();
		}
	}

	
	/**
	 * 给每日推送次数加 1
	 * @param pushId
	 */
	private void increasePushNum(long pushId){
		Integer num = pushNum.get(pushId);
		
		if(num == null)
			pushNum.put(pushId, 1);
		else
			pushNum.put(pushId, num.intValue() + 1);
		
	}
	
	public Handler getHandler() {
		return handler;
	}

	public void removeItem(long pushId) {
		Iterator<PushNotifyItem> it = items.iterator();
		while (it.hasNext()) {
			PushNotifyItem item = it.next();
			if (item.getId() == pushId)
				it.remove();
		}
	}
}
