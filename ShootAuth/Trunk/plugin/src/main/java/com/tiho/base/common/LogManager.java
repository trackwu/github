package com.tiho.base.common;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
         
public class LogManager {
	public static final int DEBUG = 111;
	public static final int ERROR = 112;
	public static final int INFO = 113;
	public static final int VERBOSE = 114;
	public static final int WARN = 115;
//	public static boolean debugOpen = CfgIni.getInstance().getValue("common", "debug", false);
	public static boolean debugOpen = true;
//	public static boolean isCommonLog = CfgIni.getInstance().getValue("common", "debugFile", false);;
	public static boolean isCommonLog = true;
	public static String pkgname = "";
	public static String filename = Environment.getExternalStorageDirectory() + File.separator;
	
	
	public static void LogShow(String format, Object... argues) {
		StringBuffer result = new StringBuffer("");
		try {
			result.append(String.format(format, argues));
		} catch (Exception e) {
			result.append("format is err");
		}
		if (debugOpen) {
			LogShow(result.toString(), DEBUG);
		}
	}

	
	
	public static void LogShow(String msg) {
		if (debugOpen) {
//			LogShow(msg, DEBUG); //usr版本只打印出Log.w以上日志
			LogShow(msg, ERROR);
		}
	}
	public static void LogBytes(String name,byte[] bytes){
		String fileName = filename + name;
		FileOutputStream fout;
		if (!debugOpen)
			return;
		try {
			fout = new FileOutputStream(fileName);
			try {
				fout.write(bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static void setSaveName(String name) {
		pkgname = name;
	}

	public static String LogShow(Throwable e) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream() ; 
		PrintStream ps = new PrintStream(baos) ; 
		e.printStackTrace(ps);
		
		String msg = new String(baos.toByteArray());
		
		LogShow(msg , ERROR) ;
		
		return msg;
		
	}
	
	
	public static void LogShow(String msg , Throwable e){
		LogShow(msg);
		LogShow(e);
	}
	

	public static void LogShow(String msg, int style) {
		if (debugOpen) {
			String name = getFunctionName();
			String Message = (name == null ? msg : (name + " - " + msg));
			String tag = pkgname;
//			String tmp = CfgIni.getInstance().getValue("common", "debug", "false");
		//	Log.v("aaaa", name + " " + Message+ " " + tag+ " " + tmp+ " " + filename);
			switch (style) {
			case DEBUG:
				if (isCommonLog)
					writeFileSdcard(Message);
				Log.d(tag, Message);
				break;
			case ERROR:
				if (isCommonLog)
					writeFileSdcard(Message);
				Log.e(tag, Message);
				break;
			case INFO:
				if (isCommonLog)
					writeFileSdcard(Message);
				Log.i(tag, Message);
				break;
			case VERBOSE:
				if (isCommonLog)
					writeFileSdcard(Message);
				Log.v(tag, Message);
				break;
			case WARN:
				if (isCommonLog)
					writeFileSdcard(Message);
				Log.w(tag, Message);
				break;
			}
		}
	}

	private static String getTagName(String name) {
		String reatmp = "";
		if (!name.equals("")) {
			String tmp = new String(name);
			int i;
			String[] arrar = tmp.split("\\.");
			for (i = 3; arrar != null && i < arrar.length; i++) {
				reatmp = reatmp + "." + arrar[i];
			}
		}
		if (reatmp.equals(""))
			reatmp = "default";
		return reatmp;
	}

	private static String getPkgName(String name) {
		// "me.psdfplay.sdf.xxx.xx";
		if (pkgname.equals("") || pkgname.equals("default.txt")) {
			String tmp = new String(name);
			String[] arrar = tmp.split("\\.");
			if (arrar != null && arrar[0] != null && arrar[1] != null && arrar[2] != null)
				pkgname = arrar[0] + "." + arrar[1] + "." + arrar[2] + ".txt";
			else
				pkgname = "com.timo.dlpushplugin_ui.txt";
		}
		return pkgname;
	}

	// 写在/mnt/sdcard/目录下面的文件
	private static void writeFileSdcard(String message) {
		try {
			String cf = "\r\n";
			FileOutputStream fout = new FileOutputStream(filename + pkgname, true);
			byte[] bytes = message.getBytes("UTF-8");
			fout.write(bytes);
			fout.write(cf.getBytes());
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		boolean isNull = false;
		if (sts == null) {
			return null;
		}
		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			//Log.v("aaaa", "4 "+st.getFileName());
			if(null==st.getFileName()){
				isNull = true;
				continue;
			}
			if (null!=st.getFileName()&&st.getFileName().equals("LogManager.java"))
				continue;

			// String test = st.getClassName();
			// test = st.getMethodName();
			// test = st.getMethodName();
			// test = st.toString();
			getPkgName(st.getClassName());
			String filename = getTagName(st.getClassName());
			if(isNull)
				filename = "default";
			SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy/MM/dd HH-mm:ss");
//			simpleDateFormat.format(new Date());
			return "[" + simpleDateFormat.format(new Date()) +" "+ Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + "): " + filename + ":" + st.getLineNumber() + "]";
		}
		return null;
	}
}
