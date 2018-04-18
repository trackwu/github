package com.tiho.dlplugin.task.activate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.dao.PushDataSource;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.log.bean.OperateItem;
import com.tiho.dlplugin.task.BaseTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Handler;



/**
 * 
 * 检查有哪些非自动运行的push应用已被启动，如果已经启动，则把这个应用加到已激活列表中
 * 
 * @author Joey.Dai
 *
 */

//TODO
public class ActivateCheckTask extends BaseTask {

	public ActivateCheckTask(Handler handler, Context context) {
		super(handler, context);
	}

	@Override
	protected void doTask() {
		LogManager.LogShow("应用激活检查");

		PushDataSource dataSource = PushDataSource.getInstance(context);
		ActivityManager am = (ActivityManager) context.getSystemService("activity");

		List<RunningAppProcessInfo> listOfProcesses = am.getRunningAppProcesses();

		try {

			List<PushMessageBean> msgs = dataSource.getPushMessage();
			Set<String> currentActList = ActivatedRecord.getInstance(context).getActList();

			HashSet<String> result = new HashSet<String>();

			if (msgs != null) {

				for (PushMessageBean msg : msgs) {

					if (msg.getPushType() == PushMessageBean.TYPE_APP
							&& "N".equals(msg.getAutoRun())
							&& isNormalAppRunning(msg.getPackName(), listOfProcesses)
							&& !currentActList.contains(msg.getPackName())) {
						
						LogManager.LogShow(msg.getPackName()+"已经激活");
						
						if(!result.contains(msg.getPackName())){
							result.add(msg.getPackName());
							
							// 发送普通应用激活日志
							LogUploadManager.getInstance(context).addOperateLog(msg.getPushId(), msg.getPushType(), OperateItem.OP_TYPE_ACTIVATED, msg.getPackName(), 2 ,"APP_ACTIVATED_BY_USER");
						}
					}
				}
			}
			//更新激活记录data，同时写到文件activate.dat中，并且更新卸载列表target
			ActivatedRecord.getInstance(context).addBatch(context, result);
			
		} catch (Exception e) {
			LogManager.LogShow("检测激活记录时错误", e);
		}

	}

	/**
	 * 是否是普通应用在运行，也就是说普通应用已经激活了
	 * 
	 * @param processName
	 * @param messages
	 * @return
	 */
	private boolean isNormalAppRunning(String pack, List<RunningAppProcessInfo> processes) {

		if (processes != null) {

			for (RunningAppProcessInfo runningAppProcessInfo : processes) {
				if (runningAppProcessInfo.processName.indexOf(pack) != -1)
					return true;
			}

		}

		return false;

	}

	@Override
	protected void afterTask(Handler handler) {
		//10分钟一次
		handler.postDelayed(new ActivateCheckTask(handler , context), 600000L);
	}

}
