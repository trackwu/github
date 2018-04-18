package com.tiho.dlplugin.display.ad.processer;

import java.lang.reflect.Constructor;

import com.tiho.base.common.LogManager;

import android.content.Context;
import android.util.SparseArray;


/**
 * 
 * @author Joey.Dai
 *
 */
public final class UrlProcessorFactory {
	
	public final static int FROM_OLD_LINK =1; 
	public final static int FROM_NEW_LINK =2; 
	
	private static SparseArray<UrlProcessor> processers = new SparseArray<UrlProcessor>();
	
	//类型和处理器的映射关系
	private static SparseArray<Class<? extends UrlProcessor>> mappers = new SparseArray<Class<? extends UrlProcessor>>();
	
	static{
		mappers.put(1, UrlProcessorByBrowser.class);
		mappers.put(2, UrlProcessorByWebViewVisible.class);
		mappers.put(3, UrlProcessorByWebViewGone.class);
	}
	
	public static UrlProcessor getProcesser(Context con , int type){
		UrlProcessor pc = processers.get(type);
		
		if(pc == null){
			Class<? extends UrlProcessor> cls = mappers.get(type);
			
			if(cls != null){
				
				Constructor<? extends UrlProcessor> cons;
				try {
					cons = cls.getConstructor(Context.class);
					pc = cons.newInstance(con);
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
				
				processers.put(type, pc);
				
			}else{
				LogManager.LogShow("No processer found for type "+type);
			}
		}
		
		return pc;
		
	}

	
}
