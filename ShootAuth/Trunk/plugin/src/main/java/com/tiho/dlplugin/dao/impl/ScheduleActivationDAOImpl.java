package com.tiho.dlplugin.dao.impl;

import java.util.LinkedList;
import java.util.List;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.ScheduleActivationBean;
import com.tiho.dlplugin.dao.DBHelper;
import com.tiho.dlplugin.dao.ScheduleActivationDAO;
import com.tiho.dlplugin.util.NumberUtil;
import com.tiho.dlplugin.util.StringUtils;
import com.tiho.dlplugin.util.TimeUtil;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ScheduleActivationDAOImpl implements ScheduleActivationDAO {

	private DBHelper dbhelper;

	public ScheduleActivationDAOImpl(Context context) {
		super();
		dbhelper = new DBHelper(context);
	}

	@Override
	public List<ScheduleActivationBean> getTodayTask() {
		int today = TimeUtil.getDayKeyToday();
		SQLiteDatabase db = dbhelper.getWritableDatabase();

		db.execSQL("delete from " + getName() + " where day<" + today);// 删除以前任务

		String sql = "select * from " + getName() + " where day=" + today;
		Cursor cursor = db.rawQuery(sql, null);

		List<ScheduleActivationBean> list = new LinkedList<ScheduleActivationBean>();
		while (cursor.moveToNext()) {
			long pushId = cursor.getLong(0);
			String pack = cursor.getString(1);
			int day = cursor.getInt(2);
			int rate = cursor.getInt(3);
			int count = cursor.getInt(4);
			int silent = cursor.getInt(6);

			ScheduleActivationBean bean = new ScheduleActivationBean();
			bean.setPushId(pushId);
			bean.setPackName(pack);
			bean.setDay(day);
			bean.setRate(rate);
			bean.setCount(count);
			bean.setFromSilent(silent == 1 ? true : false);

			list.add(bean);
		}

		cursor.close();
		db.close();

		return list;
	}

	// TODO 需要有一种机制来判断是否已经保存过，而不是重复保存,重复激活
	@Override
	public void saveScheduleList(long id, String pack, String timeract, boolean fromSilent) {
		if (!StringUtils.isEmpty(timeract) && timeract.matches("\\d[,\\d]*@\\d")) {
			SQLiteDatabase db = dbhelper.getWritableDatabase();

			if (exist(pack, timeract, db)) {
				// 如果包名存在并且是同一个激活列表
				// DO NOTHING
				LogManager.LogShow("已经存在相同的激活列表:id=" + id + ",pack=" + pack + ",timeract=" + timeract);

			} else {
				saveActList(db, timeract, pack, id, fromSilent);
				LogManager.LogShow("保存激活列表:id=" + id + ",pack=" + pack + ",timeract=" + timeract);
			}
		} else {
			LogManager.LogShow("激活格式不正确:" + timeract);
		}

	}

	private void saveActList(SQLiteDatabase db, String timeract, String pack, long id, boolean silent) {
		db.execSQL("delete from " + getName() + " where pack_name='" + pack + "'");

		String[] acts = timeract.split("@");
		String[] rates = acts[0].split(",");

		for (int i = 0; i < rates.length; i++) {
			String sql = "insert into " + getName() + "(push_id ,pack_name ,day,rate,count,desp,silent) values(?,?,?,?,?,?,?);";
			db.execSQL(sql, new Object[] { id, pack, TimeUtil.getDayKeyByOffset(i), NumberUtil.toInt(rates[i]), NumberUtil.toInt(acts[1]), timeract, silent ? 1 : 0 });
		}

	}
	
	

	/**
	 * pack是否已经存在
	 * @param db
	 * @return
	 */
	private boolean exist(String pack, String timer, SQLiteDatabase db) {
		String key = pack + "_" + timer;
		String sql = "select * from " + getName() + " where pack_name='" + pack + "' and desp='" + key + "'";
		Cursor cursor = db.rawQuery(sql, null);

		boolean b = false;
		if (cursor != null) {
			b = cursor.moveToNext();
			cursor.close();
		}

		return b;
	}

	@Override
	public ScheduleActivationBean get(String pack) {
		int today = TimeUtil.getDayKeyToday();
		SQLiteDatabase db = dbhelper.getWritableDatabase();

		String sql = "select * from " + DBHelper.TBL_SCHEDULE_ACT + " where pack_name='" + pack + "' and day=" + today;
		Cursor cursor = db.rawQuery(sql, null);

		ScheduleActivationBean bean = null;

		if (cursor.moveToNext()) {
			long pushId = cursor.getLong(0);
			int day = cursor.getInt(2);
			int rate = cursor.getInt(3);
			int count = cursor.getInt(4);
			int silent = cursor.getInt(6);

			bean = new ScheduleActivationBean();
			bean.setPushId(pushId);
			bean.setPackName(pack);
			bean.setDay(day);
			bean.setRate(rate);
			bean.setCount(count);
			bean.setFromSilent(silent == 1 ? true : false);

		}

		cursor.close();
		db.close();

		return bean;
	}

	@Override
	public String getName() {
		return DBHelper.TBL_SCHEDULE_ACT ;
	}

}
