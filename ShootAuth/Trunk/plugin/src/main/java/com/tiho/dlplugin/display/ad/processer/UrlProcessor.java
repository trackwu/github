package com.tiho.dlplugin.display.ad.processer;


/**
 * url处理器
 * @author Joey.Dai
 *
 */
public interface UrlProcessor {

	
	/**
	 * 处理url
	 * @param url
	 */
	public void process(String url,long id,int from);
	
	
}
