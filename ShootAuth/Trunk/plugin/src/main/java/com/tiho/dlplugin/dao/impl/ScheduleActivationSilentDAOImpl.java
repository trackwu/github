package com.tiho.dlplugin.dao.impl;

import com.tiho.dlplugin.dao.DBHelper;

import android.content.Context;

public class ScheduleActivationSilentDAOImpl extends ScheduleActivationDAOImpl{


	public ScheduleActivationSilentDAOImpl(Context context) {
		super(context);
	}

	@Override
	public String getName() {
		return DBHelper.TBL_SCHEDULE_ACT_SILENT ;
	}

}
