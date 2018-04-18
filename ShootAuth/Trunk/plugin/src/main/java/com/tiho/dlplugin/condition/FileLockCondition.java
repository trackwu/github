package com.tiho.dlplugin.condition;

import android.content.Context;

/**
 * Push文件是否被锁了
 * 
 * @author Joey.Dai
 * 
 */
public class FileLockCondition implements Condition {

	
	/**
	 * push是否被锁
	 * 
	 * true 未锁
	 * false 被锁
	 */
	@Override
	public boolean onCondition(Context c) {
		return com.tiho.dlplugin.condition.FileLock.getInstance(c).lock();

	}

}
