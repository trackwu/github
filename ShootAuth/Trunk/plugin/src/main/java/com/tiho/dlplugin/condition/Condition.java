package com.tiho.dlplugin.condition;

import android.content.Context;

public interface Condition {

	
	/**
	 * 是否满足条件
	 * @param c
	 * @return true 是
	 * false  否
	 */
	public boolean onCondition(final Context c);
}
