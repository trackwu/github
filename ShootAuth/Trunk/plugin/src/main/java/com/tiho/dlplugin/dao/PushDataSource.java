package com.tiho.dlplugin.dao;

import android.content.Context;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.bean.PushUninstallBean;
import com.tiho.dlplugin.observer.download.AutoInstallObserver;
import com.tiho.dlplugin.util.Pair;

import java.util.List;
import java.util.Observable;

/**
 * 
 * push消息的数据源,
 * 
 * 
 * @author Joey.Dai
 * 
 */
public class PushDataSource extends Observable {

	private PushMessageDAO pushDao;
	private SilentPushMessageDAO silentDao;
	private PushUninstallDAO uninstallDao;

	private static PushDataSource instance;

	public static final int PUSH_ACTION_SAVE = 1;
	public static final int PUSH_ACTION_DELETE = 2;

	public static final int PUSH_UNINSTALL_ACTION_SAVE = 3;
	public static final int PUSH_UNINSTALL_ACTION_DELETE = 4;
	
	public static final int SILENT_PUSH_SAVE = 5; 
	public static final int SILENT_PUSH_DELETE = 6;

	private PushDataSource(Context c) {
		super();

		pushDao = DAOFactory.getPushMessageDAO(c);
		uninstallDao = DAOFactory.getUninstallDAO(c);
		silentDao = DAOFactory.getSilentPushDAO(c);
	}

	public synchronized static PushDataSource getInstance(Context c) {
		if (instance == null) {
			instance = new PushDataSource(c);

			// 添加静默安装Observer
			instance.addObserver(new AutoInstallObserver(c));
		}

		return instance;
	}

	/**
	 * 获取push消息列表
	 * 
	 * @return
	 */
	public List<PushMessageBean> getPushMessage() throws Exception {
		return pushDao.getPushMessage();
	}
	
	public List<PushMessageBean> getSilentMessage() throws Exception {
		return silentDao.getPushMessage();
	}

	/**
	 * 获取最新的一条消息
	 * 
	 * @return
	 */
	public PushMessageBean getLatestPushMessage() throws Exception {
		return pushDao.getLatestPushMessage();
	}

	/**
	 * 
	 * 保存push消息
	 * 
	 * @param msgs
	 */
	public void savePushMessage(List<PushMessageBean> msgs) throws Exception {
		
		if(msgs != null && !msgs.isEmpty()){
			pushDao.savePushMessage(msgs);//PushMessageFileDAOImpl
			LogManager.LogShow("Push消息保存成功");
			
			setChanged();
			notifyObservers(Pair.of(PUSH_ACTION_SAVE, msgs));
			
			LogManager.LogShow("通知观察者");
			
		}else{
			LogManager.LogShow("没有新消息需要保存");
		}
	}
	
	
	public void saveSilentPushMessage(List<PushMessageBean> msgs) throws Exception {
		if(msgs != null && !msgs.isEmpty()){
			silentDao.savePushMessage(msgs);
			LogManager.LogShow("静默Push消息保存成功");
			
			setChanged();
			notifyObservers(Pair.of(SILENT_PUSH_SAVE , msgs));
			
			LogManager.LogShow("通知观察者");
			
		}else{
			LogManager.LogShow("没有新消息需要保存");
		}
	}

	public boolean haveMessages() throws Exception{
		List<PushMessageBean> normalMsg = pushDao.getPushMessage();
		List<PushMessageBean>  silentMsg = silentDao.getPushMessage();
		
		return (normalMsg != null && !normalMsg.isEmpty()) || (silentMsg != null && !silentMsg.isEmpty());
	}
	
	
	/**
	 * 
	 * 删除push
	 * 
	 * @param msg
	 */
	public void deletePushMessage(PushMessageBean msg) throws Exception {
		pushDao.deletePushMessage(msg.getPushId());
		LogManager.LogShow(msg.getPushId()+"消息已经删除");
		
		setChanged();
		notifyObservers(Pair.of(PUSH_ACTION_DELETE, msg));
	}

	public void saveUninstall(List<PushUninstallBean> list) {
		uninstallDao.saveUninstallList(list);//保存到数据库的uninstall_list这张表中
		LogManager.LogShow("卸载列表保存成功");

		setChanged();
		notifyObservers(Pair.of(PUSH_UNINSTALL_ACTION_SAVE, list));
	}

	public void flushCache() throws Exception{
		pushDao.flushCache();
	}
}
