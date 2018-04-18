package com.tiho.dlplugin.task.silent;

import java.text.MessageFormat;

import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.dao.impl.PushMessageWebRetrieve;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.util.Urls;

import android.content.Context;

public class SilentMessageWebRetrieve extends PushMessageWebRetrieve {

	public SilentMessageWebRetrieve(Context c) {
		super(c);
	}
	
	
	protected String getUrl() {

		String ver = CommonInfo.getInstance(context).getPluginVer();

		return MessageFormat.format(Urls.getInstance().getSilentListUrl(), ver ,  PushStat.getInstance(context).getUpTime());
	}

	
}
