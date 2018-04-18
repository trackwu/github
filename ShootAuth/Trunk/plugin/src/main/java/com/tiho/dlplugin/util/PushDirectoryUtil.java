package com.tiho.dlplugin.util;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

public class PushDirectoryUtil {
    public static final String PUBLIC_DIR = "DataShootPP";

	public static final String BASE_DIR = "DataShootAP";

	public static final String ICON_DIR = BASE_DIR + "/icon";

	public static final String DOWNLOAD_DIR = BASE_DIR + "/data";
	
	public static final String SILENT_DOWNLOAD_DIR = BASE_DIR + "/data/stl";
	
	public static final String LOG_DIR = BASE_DIR + "/log";
	
	public static final String DOWNLOAD_INFO_DIR = BASE_DIR + "/data/info";
	public static File getFileInPushBaseDir(Context c , String d , String name){
	
		File dir = getDir(c, d);
		
		return new File(dir , name);
		
	}
	
	

	public static File getDir(Context context, String dir) {

		File baseDir = null;
		if(TextUtils.equals(dir,DOWNLOAD_DIR)||TextUtils.equals(dir,SILENT_DOWNLOAD_DIR)){
			if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
				baseDir = new File(Environment.getExternalStorageDirectory(), dir);
			} else {
				baseDir = new File(context.getFilesDir(), dir);
			}
		}else{
			baseDir = new File(context.getFilesDir(), dir);
		}
		if (!baseDir.exists())
			baseDir.mkdirs();

		return baseDir;
	}
	
	public static String getRootPath(Context context){
	    String rootPath = "";
	    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
	        rootPath = Environment.getExternalStorageDirectory().toString();
        } else {
            rootPath = context.getFilesDir().getPath();
        }
	    return rootPath;
	}

}
