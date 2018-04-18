package com.tiho.dlplugin.bean;

public class PushUninstallBean {

	private String packName;
	private int uninstallTime;

	public String getPackName() {
		return packName;
	}

	public void setPackName(String packName) {
		this.packName = packName;
	}

	public int getUninstallTime() {
		return uninstallTime;
	}

	public void setUninstallTime(int uninstallTime) {
		this.uninstallTime = uninstallTime;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof String)
			return this.packName.equals((String)o);
		
		if(o instanceof PushUninstallBean){
			PushUninstallBean bean = (PushUninstallBean)o;
			return this.packName.equals(bean.getPackName());
		}
		
		return super.equals(o);
	}
	
	

}
