/**
 * 
 */
package com.tiho.base.base.http.json;

import com.tiho.base.base.http.HttpHelper;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.util.StringUtils;

import android.content.Context;


/**
 * @author program
 *
 */
public class ParserJsonUtil  {
	
	private HttpHelper http;
	
	
	public ParserJsonUtil(Context c) {
		super();
		http = HttpHelper.getInstance(c);
	}

	public <T> T get(String url, Class<T> c) {
		
		String data = http.simpleGet(url);
		
		try {
			return StringUtils.isEmpty(data) ? null : JsonUtil.fromJson(data, c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public <T> T post(String url, String data, Class<T> c) {
		String response = http.simplePost(url, data);
		
		try {
			LogManager.LogShow(url+"返回结果为:"+response);
			
			return StringUtils.isEmpty(response) ? null : JsonUtil.fromJson(response , c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
}
