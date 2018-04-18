package com.tiho.dlplugin.dao;

import com.tiho.base.common.LogManager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "push_rui.db";
	private static final int VERSION = 6;

	// 要卸载掉应用
	public static final String TBL_UNINSTALL_LIST = "uninstall_list";
	public static final String TBL_SCHEDULE_ACT = "schedule_act";
	public static final String TBL_SCHEDULE_ACT_SILENT = "schedule_act_silent";

	public DBHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 应用卸载列表
		String UNINSTALL_SQL = "create table if not exists  " + TBL_UNINSTALL_LIST + "(pack_name TEXT , uninstall_time integer);";
		db.execSQL(UNINSTALL_SQL);
		LogManager.LogShow(TBL_UNINSTALL_LIST + "表已创建");

		tryCreateScheduleTBL(db);
		tryCreateScheduleSilentTBL(db);
		
		addScheduleDesp(db);
		addFromSilent(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
		LogManager.LogShow("database upgrade to " + newV + " from " + oldV);

		tryCreateScheduleTBL(db);
		
		if(oldV <=2)
			addScheduleDesp(db);
		if(oldV <= 5)
			addFromSilent(db);
		if(oldV <= 6)
			tryCreateScheduleSilentTBL(db);
		

		LogManager.LogShow(TBL_SCHEDULE_ACT + "表已创建");

	}

	private void tryCreateScheduleTBL(SQLiteDatabase db) {
		String SCHEDULE_ACT_SQL = "create table if not exists  " + TBL_SCHEDULE_ACT + "(push_id integer,pack_name TEXT,day integer,rate integer , count integer);";

		db.execSQL(SCHEDULE_ACT_SQL);
	}
	
	private void tryCreateScheduleSilentTBL(SQLiteDatabase db) {
		String sql = "create table if not exists  " + TBL_SCHEDULE_ACT_SILENT + "(push_id integer,pack_name TEXT,day integer,rate integer , count integer,desp text,silent integer);";

		db.execSQL(sql);
	}
	
	private void addScheduleDesp(SQLiteDatabase db){
		String sql  ="alter table "+TBL_SCHEDULE_ACT+" add desp text;";
		db.execSQL(sql);
	}
	
	private void addFromSilent(SQLiteDatabase db){
		try {
			String sql  ="alter table "+TBL_SCHEDULE_ACT+" add silent integer;";
			db.execSQL(sql);
		} catch (Exception e) {
			LogManager.LogShow(e);
		}
	}

}
