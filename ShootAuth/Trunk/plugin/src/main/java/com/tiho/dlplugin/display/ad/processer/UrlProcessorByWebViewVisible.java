package com.tiho.dlplugin.display.ad.processer;

import android.content.Context;

public class UrlProcessorByWebViewVisible extends UrlProcessorByWebView {

	public UrlProcessorByWebViewVisible(Context context) {
		super(context);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
