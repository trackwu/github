package com.tiho.base.base.manufactory.aidl;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.tiho.base.base.des.ManufactoryDES;
import com.tiho.base.common.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class ManuUtil {
	public interface IGetPlaymeid {
		public void getdata(String playmeid);
	}
	String LOGTAG = "ManuUtil";
	public static final String defaultId = "1Jph/zsXM3hLBFd8XbBLM2ooLrUu+wvC";
	private Context mContext;
	private static ManuUtil manuUtil;
	private String manu = "android", type = "playmet", IMEI = "012345678901234", IMSI = "999999999999999";
	private int timertick = 0;
	private String playmeid = "";
	private IGetPlaymeid icallback = null;

	private ManuUtil(Context context) {
		this.mContext = context;
	}

	public static ManuUtil getInstance(Context mContext) {
		if (manuUtil == null) {
			manuUtil = new ManuUtil(mContext);
		}

		return manuUtil;
	}

	public String getPlaymeID(String manu1, String type1, String IMEI1, String IMSI1, IGetPlaymeid i) {
		this.manu = manu1;
		this.type = type1;
		this.IMEI = IMEI1;
		this.IMSI = IMSI1;
		this.icallback = i;
		if(playmeid != null &&!playmeid.equals("")){//
			if(icallback != null){
				icallback.getdata(playmeid);
			}
			return playmeid;
		}
		playmeid = getPlaymeidFromPrivate2();
		if (playmeid != null && playmeid.length() > 0) {
			LogManager.LogShow("getPlaymeidFromPrivate2 getPlaymeID  = " + playmeid);
			if(icallback != null){
				icallback.getdata(playmeid);
			}
			return playmeid;
		}
		
		playmeid = getPlaymeidFromPrivate();
		if (playmeid != null && playmeid.length() > 0) {
			LogManager.LogShow("getPlaymeidFromPrivate getPlaymeID  = " + playmeid);
			if(icallback != null){
				icallback.getdata(playmeid);
			}
			return playmeid;
		}
		playmeid = getPPLAYProject();
		if (playmeid != null && playmeid.length() > 0) {
			LogManager.LogShow("build getPlaymeID  = " + playmeid);
			if(icallback != null){
				icallback.getdata(playmeid);
			}
			return playmeid;
		}
		// read config file
		String configMeid = getPlaymeidFromConfig();
		playmeid = configMeid;
		if (!playmeid.equals("") && playmeid.length() > 0) {
			LogManager.LogShow("from config file: build getPlaymeID  = " + playmeid);
			if(icallback != null){
				icallback.getdata(playmeid);
			}
			return playmeid;
		}
		String assetsMeid =getPlaymeidFromAssets();
		playmeid = assetsMeid;
		if (!playmeid.equals("") && playmeid.length() > 0) {
			LogManager.LogShow("from Assets file: build getPlaymeID  = " + playmeid);
			if(icallback != null){
				icallback.getdata(playmeid);
			}
			return playmeid;
		}
		//从sd卡的/PLAYPUSH/hstype.txt中读取playmeid
		String txtMeid = getPlaymeidFromHstypeFile();
		playmeid = txtMeid;
		if (!playmeid.equals("") && playmeid.length() > 0) {
			LogManager.LogShow("from HstypeFile: build getPlaymeID  = " + playmeid);
			if(icallback != null){
				icallback.getdata(playmeid);
			}
			return playmeid;
		}
				
		playmeid = defaultId;
		LogManager.LogShow("getPlaymeID err = " + playmeid);
		if(icallback !=null)
			icallback.getdata(playmeid);
		return playmeid;
	}

	/**
	 * 从sd卡的/PLAYPUSH/hstype.txt中读取playmeid 
	 * 格式：hsman=XXX&hstype=XXX
	 */
	private String getPlaymeidFromHstypeFile(){
		String id = "";
		File baseDir = null;

		if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
			baseDir = new File(Environment.getExternalStorageDirectory(), "PLAYPUSH");
		} else {
			baseDir = new File(mContext.getFilesDir(), "PLAYPUSH");
		}

		if (!baseDir.exists()){
			return id;
		}
		
		File file = new File(baseDir , "hstype.txt");
		if(!file.exists()){
			return id;
		}
		try {
			FileInputStream fin;
			fin = new FileInputStream(file);
			int length = fin.available();
			LogManager.LogShow("getPlaymeidFromHstypeFile length " + length);
			byte[] buffer = new byte[length];
			fin.read(buffer);
			fin.close();
			String[] s = new String(buffer).split("&");
			if(s != null && s.length == 2){
				String hsman = s[0].split("=")[1];
	        	String hstype = s[1].split("=")[1];
	        	id = ManufactoryDES.encode(hsman, hstype);
			}
		} catch (Exception e) {
			LogManager.LogShow(e);
		}
		LogManager.LogShow("getPlaymeidFromHstypeFile id " + id);
		return id;
	}
	
	private String getPlaymeidFromAssets() {
		// TODO Auto-generated method stub
		InputStream is =null;
		String playmeid ="";
		try {
			is = mContext.getApplicationContext().getAssets().open("Playmeid.cfg");
			int length = is.available();
			if(length==32){
				byte[] buffer =new byte[length];
				is.read(buffer);
				playmeid = new String(buffer);
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return playmeid;
	}

	private String getPlaymeidFromPrivate2(){
		String path = "/data/data/me.powerplay.playapp/playmeid/manufactory.conf";
		File file = new File(path);
		if(file.exists()){
			LogManager.LogShow("getPlaymeidFromPrivate2 " + file.exists());
			FileInputStream fin;
			try {
				fin = new FileInputStream(path);
				int length = fin.available();
				LogManager.LogShow("getPlaymeidFromPrivate2 length " + length);
				if(length == 32){
					byte[] buffer = new byte[length];
					fin.read(buffer);
//					playmeid = EncodingUtils.getString(buffer, "UTF-8");
					fin.close();
					return new String(buffer);
				}
				fin.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return "";
	}

	private String getPlaymeidFromPrivate(){
		String path = "/data/data/me.pplay.playapp/playmeid/manufactory.conf";
		File file = new File(path);
		if(file.exists()){
			LogManager.LogShow("getPlaymeidFromPrivate " + file.exists());
			FileInputStream fin;
			try {
				fin = new FileInputStream(path);
				int length = fin.available();
				LogManager.LogShow("getPlaymeidFromPrivate length " + length);
				if(length == 32){
					byte[] buffer = new byte[length];
					fin.read(buffer);
//					playmeid = EncodingUtils.getString(buffer, "UTF-8");
					fin.close();
					return new String(buffer);
				}
				fin.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return "";
	}
	@SuppressWarnings("finally")
	private String getPPLAYProject() {
		String playmeid = "";
		String type = "";
		String manu = "";
		Field field;
		try {
			field = Build.class.getField("PLAYMEID");
			playmeid = (String) field.get(null);
			return playmeid;
		} catch (Exception e) {
			playmeid = "";
		}
		try {
			field = Build.class.getField("SKYMOBI_PROJECT");
			manu = (String) field.get(null);
			field = Build.class.getField("SKYMOBI_A");
			type = (String) field.get(null);
		} catch (Exception e) {
			manu = "";
			type = "";
		}

		if (!manu.equals("")) {
			if (type.equals("")) {
//				type = manu;
				int x = ((Activity)mContext).getWindowManager().getDefaultDisplay().getWidth();
				int y = ((Activity)mContext).getWindowManager().getDefaultDisplay().getHeight();
				type = x + "x" + y;
			}
			if(manu.length()>7){
				manu = manu.substring(0,7);
			}
			if(type.length()>7){
				type = type.substring(0,7);
			}
			return ManufactoryDES.encode(manu, type);
		} else {
			try {
				field = Build.class.getField("SKY_MOBI");
				manu = (String) field.get(null);
				field = Build.class.getField("SKY_MOBI_A");
				type = (String) field.get(null);
			} catch (Exception e) {
				manu = "";
				type = "";
			} finally {
				if (type.equals(""))
					type = manu;
				if (manu.equals(""))
					return manu;
				if(manu.length()>7){
					manu = manu.substring(0,7);
				}
				if(type.length()>7){
					type = type.substring(0,7);
				}
				return ManufactoryDES.encode(manu, type);
			}
		}
	}

	//get playmeid from config file
	private String getPlaymeidFromConfig(){
		String playmeid = "";
		try {
			String filePath = "/system/etc/manufactory.conf";
			File file = new File(filePath);
			if (!file.isFile()) {
				return playmeid;
			}
			FileInputStream fin = new FileInputStream(filePath);
			int length = fin.available();
			if(length == 32){
				byte[] buffer = new byte[length];
				fin.read(buffer);
//				playmeid = EncodingUtils.getString(buffer, "UTF-8");
				fin.close();
				return new String(buffer);
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return playmeid;
	}
}
