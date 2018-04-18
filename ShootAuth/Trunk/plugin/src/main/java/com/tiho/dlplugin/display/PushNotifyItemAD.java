package com.tiho.dlplugin.display;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.display.ad.processer.UrlProcessor;
import com.tiho.dlplugin.display.ad.processer.UrlProcessorFactory;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.log.bean.OperateItem;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * 广告push
 * 
 * @author Joey.Dai
 * 
 */
public class PushNotifyItemAD extends BasePushNotifyItem {


	public PushNotifyItemAD(Context c, PushNotifyManager notifyManager, PushMessageBean msg) {
		super(c, notifyManager, msg);
	}

	/**
	 * 广告消息点击
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);// 不能删除

		if (isClickAction(intent)) {

			LogManager.LogShow("广告消息点击:" + intent.getAction());

			LogUploadManager.getInstance(getContext()).addOperateLog(getMessage().getPushId(), PushMessageBean.TYPE_AD, OperateItem.OP_TYPE_CLICK, null, -1,"AD_CLICK");

			PushMessageBean msg = getMessage();
			
			LogManager.LogShow("PushNotifyItemAD UrlType:" + msg.getUrlType());
			
			int type = hasPermission(context) ? msg.getUrlType() : 1 ;
			
			UrlProcessor processer = UrlProcessorFactory.getProcesser(context, type);

			if (processer != null) {
				processer.process(msg.getUrl(),getId(),UrlProcessorFactory.FROM_OLD_LINK);
				try {
					notifyManager.removeItem(getId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean hasPermission(Context context){
		return context.checkCallingOrSelfPermission(android.Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED;
	}
}
