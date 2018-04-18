package com.tiho.base.base.http;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import com.tiho.dlplugin.common.CommonInfo;

import android.content.Context;

public class PushClientFactory extends BaseClientFactory {

	public PushClientFactory(Context context) {
		super(context);
	}

	@Override
	protected List<Header> additionalHeader() {
		LinkedList<Header> headers = new LinkedList<Header>();
		headers.add(new BasicHeader("User-Agent", "playPush"));
		
		headers.add(new BasicHeader("X-play-agent", CommonInfo.getInstance(getContext()).getXplayAgent()));
		
		return headers;
	}

}
