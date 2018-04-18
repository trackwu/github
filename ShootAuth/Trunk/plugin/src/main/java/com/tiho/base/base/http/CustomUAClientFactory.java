package com.tiho.base.base.http;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;

public class CustomUAClientFactory extends BaseClientFactory {

	private String userAgent;
	
	public CustomUAClientFactory(Context context) {
		this(context, null);
	}

	public CustomUAClientFactory(Context context, String userAgent) {
		super(context);
		this.userAgent = userAgent;
	}

	@Override
	protected List<Header> additionalHeader() {
		LinkedList<Header> headers = new LinkedList<Header>();
		headers.add(new BasicHeader("User-Agent", userAgent));
		
		return headers;
	}
	
	

}
