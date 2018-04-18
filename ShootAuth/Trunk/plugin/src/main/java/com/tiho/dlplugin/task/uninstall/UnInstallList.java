package com.tiho.dlplugin.task.uninstall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.task.InstallTxtTask;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;



/**
 * 应用卸载列表，被卸载掉应用的列表
 * @author Joey.Dai
 *
 */
public class UnInstallList {

	private static final String UNINSTALL_FILE_NAME = "unst.dat";

	private static final Set<String> uninstalls = new HashSet<String>();
	
	private static volatile boolean init = false ; 
	

	public static void add(Context context , String pack){
		if(!init)
			init(context);
		//TODO 检查该包名是否属于“不受限、无限次可静默的产品”
		if(InstallTxtTask.inSet(pack)){
			LogManager.LogShow("UnInstallList add inSet " + pack);
			return;
		}
		uninstalls.add(pack);
		
		FileUtil.appendToFile(getFile(context), pack);
	}
	
	public static boolean deleted(Context context , String pack){
		if(!init)
			init(context);
		//TODO 检查该包名是否属于“不受限、无限次可静默的产品”
		if(InstallTxtTask.inSet(pack)){
			LogManager.LogShow("UnInstallList deleted inSet " + pack);
			return false;
		}
		return uninstalls.contains(pack);
	}

	private static File getFile(Context c) {
		File dir = PushDirectoryUtil.getDir(c, PushDirectoryUtil.BASE_DIR);

		return new File(dir, UNINSTALL_FILE_NAME);
	}

	private static void init(Context c) {
		File f = getFile(c);

		if (f.exists()) {
			FileInputStream fis = null;
			InputStreamReader isr = null;
			BufferedReader br = null;

			try {
				fis = new FileInputStream(f);
				isr = new InputStreamReader(fis);
				br = new BufferedReader(isr);

				String line = null;
				while ((line = br.readLine()) != null) {
					uninstalls.add(line.trim());
				}
				
				init = true ;
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null) {
						br.close();
					}
					if (isr != null)
						isr.close();

					if (fis != null)
						fis.close();

				} catch (IOException e) {
					LogManager.LogShow("流关闭出错"  ,  e);
				}
			}

		}
	}

}
