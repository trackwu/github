package com.tiho.dlplugin.dao.impl;

import android.content.Context;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.ObjectUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;

import java.io.File;
import java.util.HashMap;

public class PushConfigFileDAOImpl implements PushConfigDAO {

	private Context context;
	
	//缓存
	private HashMap<String, Object> cache = new HashMap<String, Object>();
	
	
	public PushConfigFileDAOImpl(Context context) {
		super();
		this.context = context;
	}
	

	@Override
	public <T> T getConfigByKey(String key, Class<T> c){
		
		if(!cache.containsKey(key)){
			
			String name = "push_"+key+".dat" ;
			
			File f = PushDirectoryUtil.getFileInPushBaseDir(context, PushDirectoryUtil.BASE_DIR, name);
			
			if(f.exists()){//如果该文件存在，就读取出来生成对象，存储到cache中
				byte[] data = FileUtil.getFileRawData(f);
				T t = (T) ObjectUtil.toObject(data);
				cache.put(key, t);
			}else{//该文件不存在，先用c的默认构造器生成一个对象并存储到cache中
				try {
					cache.put(key, c.newInstance());
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		T t =  (T)cache.get(key);
		
		LogManager.LogShow("file config:"+t);
		
		return t;
	}

	@Override
	public void updateConfig(String key, Object serial) throws Exception {
		
		String name = "push_"+key+".dat" ;
		
		byte[] data = ObjectUtil.toByte(serial);
		
		File f = PushDirectoryUtil.getFileInPushBaseDir(context, PushDirectoryUtil.BASE_DIR, name);
		
		FileUtil.writeToFile(f, data, false);
		
		cache.put(key, serial);
	}

	@Override
	public void saveConfig(String key, Object serial) throws Exception {
		updateConfig(key, serial);
	}

	@Override
	public boolean overOneDay(String key) throws Exception {
		
		File f = PushDirectoryUtil.getFileInPushBaseDir(context, PushDirectoryUtil.BASE_DIR, "push_"+key+".dat");
		
		return f.exists() ? (System.currentTimeMillis() - f.lastModified()) > 24L*3600000L: true; 
	}

}
