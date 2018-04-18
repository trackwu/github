package com.tiho.dlplugin.dao.impl;

import java.util.LinkedList;
import java.util.List;

import com.tiho.dlplugin.bean.PushUninstallBean;
import com.tiho.dlplugin.dao.DBHelper;
import com.tiho.dlplugin.dao.PushUninstallDAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PushUninstallDAOImpl implements PushUninstallDAO {

	private DBHelper dbHelper ; 
	
	public PushUninstallDAOImpl(Context c) {
		dbHelper = new DBHelper(c);
	}

	@Override
	public List<PushUninstallBean> getUninstallAppList() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from "+DBHelper.TBL_UNINSTALL_LIST, null);
		
		List<PushUninstallBean> list=  new LinkedList<PushUninstallBean>();
		while(cursor.moveToNext()){
			PushUninstallBean bean = new PushUninstallBean();
			bean.setPackName(cursor.getString(0));
			bean.setUninstallTime(cursor.getInt(1));
			
			list.add(bean);
		}
		cursor.close();
		db.close();
		
		return list;
	}

	@Override
	public void saveUninstallList(List<PushUninstallBean> list) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		for (PushUninstallBean pushUninstallBean : list) {
			Cursor c = db.rawQuery("select * from "+DBHelper.TBL_UNINSTALL_LIST+" where pack_name=?" , new String[]{pushUninstallBean.getPackName()});
			
			if(c == null || !c.moveToNext()){
				db.execSQL("insert into "+DBHelper.TBL_UNINSTALL_LIST+"(pack_name , uninstall_time) values(?,?)" , new Object[]{pushUninstallBean.getPackName() , pushUninstallBean.getUninstallTime()});
			}else{
				c.close();
			}
		}
		
		db.close();
		
	}

	@Override
	public void deleteUninstallApp(String pack) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db.execSQL("delete from "+DBHelper.TBL_UNINSTALL_LIST+" where pack_name=?" , new Object[]{pack});
		
		db.close();
	}

}
