package com.tiho.dlplugin.condition;

import android.content.Context;

public class ConditionManager {

	private static final Condition fileLockCondition ;
	private static final Condition workTimeCondition;
	
	
	static{
		fileLockCondition = new FileLockCondition();
		workTimeCondition = new WorkTimeCondition();
	}
	
	
	
	/**
	 * 检查push是否被锁
	 * @param c
	 * @return true 被锁 false 未锁
	 */
	public static final boolean isPushOnLock(Context c){
		return !fileLockCondition.onCondition(c);
	}
	
	/**
	 * 当前是否在push的工作时间范围内
	 * @param c
	 * @return
	 */
	public static final boolean inWorkTime(Context c){
		return workTimeCondition.onCondition(c);
	}
	
	
	/**
	 * 是否满足push的推送条件
	 * @param c
	 * @return
	 */
	public static final boolean isPushOnCondition(Context c){
		
		return	   	   fileLockCondition.onCondition(c)
				&& workTimeCondition.onCondition(c) ;
	}
}
