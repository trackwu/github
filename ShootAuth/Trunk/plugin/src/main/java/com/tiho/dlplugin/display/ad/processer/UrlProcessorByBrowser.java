package com.tiho.dlplugin.display.ad.processer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushMessageDAO;
import com.tiho.dlplugin.task.silent.SilentList;
import com.tiho.dlplugin.util.PackageUtil;

/**
 * 
 * 直接跳转到浏览器
 * 
 * @author Joey.Dai
 * 
 */
public class UrlProcessorByBrowser implements UrlProcessor {

	private Context context;

	public UrlProcessorByBrowser(Context context) {
		super();
		this.context = context;
	}

	@Override
	public void process(String url, long id,int from) {
		Uri uri = Uri.parse(url);
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		if (PackageUtil.isInstalled("com.android.browser", context)) {
			i.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
		}
		try {
			if(from==UrlProcessorFactory.FROM_OLD_LINK){
				PushMessageDAO msgdao = DAOFactory.getPushMessageDAO(context);
				msgdao.deletePushMessage(id);
			}else if(from==UrlProcessorFactory.FROM_NEW_LINK){
				SilentList list = SilentList.getInstance(context);
				list.consumed(list.getById(id));
			}
			context.startActivity(i);
		} catch (Exception e) {
			LogManager.LogShow("打开网址出错,e==" + e);
		}

	}

}
