package com.tiho.dlplugin.http;

import java.sql.Timestamp;

import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.bean.PushUninstallBean;

public class Converter {

	public static PushMessageBean toMessageBO(PushMessageItem dto){
		PushMessageBean bo = new PushMessageBean();
		bo.setPushId(dto.getId());
		bo.setPushType(dto.getType());
		bo.setWeight(dto.getPriority());

		long timeLeft = dto.getLeftLife() * 60000L;
		bo.setExpireTime(new Timestamp(System.currentTimeMillis() + timeLeft));
		bo.setLan(dto.getLan());
		bo.setTitle(dto.getName());
		bo.setSlogan(dto.getSlogan());
		bo.setIcon(dto.getIconUrl());
		bo.setUrl(dto.getUrl());
		bo.setUrlType(dto.getUrlType());
		
		for (TimePeriodItem t : dto.getPeriod()) {
			bo.getBestTimes().add(com.tiho.dlplugin.util.Pair.of(t.getBegin(), t.getEnd()));
		}
		
		if(dto.getType() == PushMessageBean.TYPE_APP){
			AppInfoItem info = dto.getAppinfo();
			bo.setAppname(info.getAppname());
			bo.setMd5(info.getMd5());
			bo.setBytes(info.getBytes());
			bo.setVercode(info.getVercode());
			bo.setPackName(info.getPackageName());
			bo.setShowtype(info.getShowtype());
			bo.setAutoRun(info.getAuto_run());
			bo.setAutoInstall(info.getAuto_install());
			bo.setTimeract(dto.getAppinfo().getTimeract());
		}
		
		return bo;
	}
	
	
	public static PushUninstallBean toUninstallBO(UnInstallItem item){
		PushUninstallBean bean = new PushUninstallBean() ;
		bean.setPackName(item.getPack());
		bean.setUninstallTime(item.getTime());
		return bean;
	}
	
}
