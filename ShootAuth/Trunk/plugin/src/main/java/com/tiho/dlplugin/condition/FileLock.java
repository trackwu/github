package com.tiho.dlplugin.condition;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.util.PushDirectoryUtil;

import android.content.Context;

public class FileLock {
	
	private static final String PUSH_LOCK_FILE = "FileLock.lck";

	private Context context;
	
	private static FileLock instance  ; 
	
	public static synchronized FileLock getInstance(Context c){
		if(instance == null)
			instance = new FileLock(c);
		
		
		return instance ; 
	}
	
	private FileLock(Context context) {
		super();
		this.context = context;
	}

	
	private java.nio.channels.FileLock lock ;

	public boolean lock(){
		File dir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.PUBLIC_DIR);
		File file = new File(dir, PUSH_LOCK_FILE);

		boolean result = false;
		try {
			if (!file.exists())
				file.createNewFile();

			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			FileChannel channel = raf.getChannel();
			lock = channel.tryLock();

			result = lock != null;

		} catch (Exception e) {
			LogManager.LogShow("检查push文件锁异常");
			LogManager.LogShow(e);
		}

		LogManager.LogShow("文件锁是否获取成功:" + result);
		
		return result ;
	}
	
	
	public void unlock(){
		
		if(lock != null)
			try {
				lock.release();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
}
