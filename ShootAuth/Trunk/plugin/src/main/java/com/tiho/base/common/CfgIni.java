/**
 * 
 */
package com.tiho.base.common;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

/**
 * @author seward
 * 
 */
public class CfgIni {
	static CfgIni ini = null;
	private IniEditor iniedit = null;

	public static CfgIni getInstance() {
		if (ini == null) {
			ini = new CfgIni();
		}
		return ini;
	}

	private CfgIni() {
		File file = new File(Environment.getExternalStorageDirectory(), "config_shoot.ini");

		iniedit = new IniEditor();
		try {
			iniedit.load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getValue(String section, String option, String defaultOption) {
		String tmp = iniedit.get(section, option);
		if (tmp == null)
			tmp = defaultOption;
		return tmp;
	}

	public boolean getValue(String section, String option, boolean defaultOption) {
		String tmp = iniedit.get(section, option);
		if (tmp != null && tmp.equals("true"))
			return true;
		return false;
	}

	public int getValue(String section, String option, int defaultOption) {
		String tmp = iniedit.get(section, option);

		if (tmp != null) {

			int value = defaultOption;
			try {
				value = Integer.parseInt(tmp);
			} catch (NumberFormatException e) {
				// TODO: handle exception
				value = defaultOption;
			}
			return value;

		}
		return defaultOption;
	}

}
