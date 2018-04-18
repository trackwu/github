package com.tiho.base.base.http;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;


/**
 * 
 * http client 工厂
 */
public class ClientFactory {

	private static PushClientFactory pcf;

	public synchronized static DefaultHttpClient getClient(Context context) {
		if (pcf == null)
			pcf = new PushClientFactory(context);

		return pcf.genClient();
	}
	
	
	private static BaseClientFactory bcf;
	
	public synchronized static DefaultHttpClient getCustomUAClient(Context context , String ua) {
		if (bcf == null)
			bcf = new CustomUAClientFactory(context, ua);

		return bcf.genClient();
		
	}
	
	
}
