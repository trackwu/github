package com.tiho.dlplugin.display.ad.processer;

import com.tiho.dlplugin.display.ad.view.AdLoaderManager;

import android.content.Context;


public abstract class UrlProcessorByWebView implements UrlProcessor {

	private Context context;
	
	public UrlProcessorByWebView(Context context) {
		super();
		this.context = context;
	}


	@Override
	public void process(String url,long id,int from) {
		AdLoaderManager.load(context, url , isVisible(),id,from);
	}
	
	
	/**
	 * web界面是否可见
	 * @return
	 */
	public abstract boolean isVisible();

}
