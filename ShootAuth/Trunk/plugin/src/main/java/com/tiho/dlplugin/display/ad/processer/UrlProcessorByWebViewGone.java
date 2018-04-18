package com.tiho.dlplugin.display.ad.processer;

import android.content.Context;

public class UrlProcessorByWebViewGone extends UrlProcessorByWebView {

	public UrlProcessorByWebViewGone(Context context) {
		super(context);
	}

	@Override
	public boolean isVisible() {
		return false;
	}

}
