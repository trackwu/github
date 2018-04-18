package com.tiho.dlplugin.dao.impl;

import com.tiho.dlplugin.dao.SilentPushMessageDAO;

import android.content.Context;

public class SilentPushMessageFileDAOImpl extends PushMessageFileDAOImpl implements SilentPushMessageDAO {

	public SilentPushMessageFileDAOImpl(Context context) {
		super(context);
	}

	
	@Override
	protected String getFileName() {
		
		return "push_silent.dat";
	}


}
