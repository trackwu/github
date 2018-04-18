package com.tiho.dlplugin.util;

import java.io.File;
import java.io.IOException;

import com.tiho.base.common.LogManager;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class FileCache {
	public static final int TYPE_HOME_DOWN 				= 1;
	public static final int TYPE_ICON 					= 2;
	public static final int TYPE_PUSHDATA				= 3;
	public static final int TYPE_CACHE_IMAGE			= 4;

	public static final String FILE_PATH_HOME 			= "_PPLAY/";
	public static final String FILE_PATH_DOWN 			= "download/";
	public static final String FILE_PATH_CACHE 			= "cache/";
	public static final String FILE_PATH_ICON 			= "icon/";
	public static final String FILE_PATH_PUSHDATA		= "pushData/";
	public static final String FILE_PATH_CACHE_IMAGE 	= FILE_PATH_CACHE + "images/";
	private boolean mInternal = false;
	public String getFileCache(Context context) {
		String cacheDir = null;
		if(false == mInternal && (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
			cacheDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FILE_PATH_HOME;
			File file = new File(cacheDir);
			if(!file.exists())
				file.mkdirs();
		} else {
			cacheDir = context.getFilesDir().getPath() + File.separator;
			File dataFile = new File(cacheDir);
			if(!dataFile.exists())
				dataFile.mkdirs();
		}
		return cacheDir;
	}
	
    public String createFileDirInternal(Context context,int type, boolean internal){
        mInternal = internal;
        return createFileDir(context, type);
    }
       
	public String createFileDir(Context context,int type){  
		String cacheDirPath = getFileCache(context);
		File filedir = null;
		String cmd = "chmod 777  ";
		switch (type) {
		case TYPE_HOME_DOWN:
			cacheDirPath += FILE_PATH_DOWN;
			cmd += cacheDirPath; //755 644
			try {
			 Runtime.getRuntime().exec(cmd);
			} catch (Exception e) {

			 e.printStackTrace();
			}  
			filedir = new File(cacheDirPath);
			break;
		case TYPE_ICON:
			cacheDirPath += FILE_PATH_ICON;
			cmd += cacheDirPath; //755 644
			try {
			 Runtime.getRuntime().exec(cmd);
			} catch (Exception e) {

			 e.printStackTrace();
			}  
			filedir = new File(cacheDirPath);
			break;
		case TYPE_PUSHDATA:
			cacheDirPath += FILE_PATH_PUSHDATA;
			LogManager.LogShow("cacheDirPath: " + cacheDirPath);
			cmd += cacheDirPath; //755 644
			try {
			 Runtime.getRuntime().exec(cmd);
			} catch (Exception e) {
			 e.printStackTrace();
			}
			filedir = new File(cacheDirPath);
			break;
		case TYPE_CACHE_IMAGE:
			cacheDirPath += FILE_PATH_CACHE_IMAGE;
			cmd += cacheDirPath; //755 644
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (Exception e) {
				e.printStackTrace();
			}  
			filedir = new File(cacheDirPath);
			if(!filedir.getParentFile().exists()){
				File paratFile = new File(filedir.getParent());
				paratFile.mkdir();
			}
			break;
		default:
			break;
		}
		if (filedir != null && !filedir.exists()) {
			filedir.mkdir();
		}
		return cacheDirPath;
	}

	public File getFile(Context context,int type, String fileName) {
		return creatFile(fileName, createFileDir(context, type));
	}

	
	private File creatFile(String fileName, String filedirPath) {
		File file = new File(filedirPath + fileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return file;
	}
	
	public void clear(File cacheDir){
	  if(!cacheDir.exists())return;
      File[] files = cacheDir.listFiles();
      if(files==null)
          return;
      for(File f:files)
          f.delete();
	}
	
	
	//判断空间是否充足
	
	public boolean spaceEnough(Context context){
//		if(spaceSize(context)/1024/1024 < 15){
		if(spaceSize(context) <= 0){
			return false;
		}
		return true;
	}
	
	
	public long spaceSize(Context context){
		long avail = -1;
		File sdcard_file = new File(Environment.getExternalStorageDirectory().getPath());
		if(!sdcard_file.exists() || !sdcard_file.canWrite()){
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			MemoryInfo mi = new MemoryInfo();
			am.getMemoryInfo(mi);
			//mi.availMem; 当前系统的可用内存 
//			String avail = Formatter.formatFileSize(getBaseContext(), mi.availMem);// 将获取的内存大小规格化
			avail = mi.availMem;
			LogManager.LogShow( "Total: " + mi.availMem/1024/1024);
		}else{
			StatFs stat = new StatFs(sdcard_file.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			long availableBlocks = stat.getAvailableBlocks();

			long total = (blockSize * totalBlocks)/1024/1024;
			avail = blockSize * availableBlocks;
			LogManager.LogShow( "SdCard Total: " + total + " Mb   Available: " + (blockSize * availableBlocks)/1024/1024 + " Mb");
		}
		return avail - 15*1024*1024;
		
	}
	
}
