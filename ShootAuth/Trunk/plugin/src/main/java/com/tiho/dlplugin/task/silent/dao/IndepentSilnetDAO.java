package com.tiho.dlplugin.task.silent.dao;

import java.util.List;

import com.tiho.dlplugin.bean.Resource;

public interface IndepentSilnetDAO {
	/**
	 * 获取push消息列表
	 * @return
	 */
	public List<Resource> getPushMessage()throws Exception;
	
	
	public Resource queryByPack(String pack)throws Exception;
	
	public Resource queryByPackHash(int hash)throws Exception;
	
	
	/**
	 * 
	 * 保存push消息
	 * 
	 * @param msgs
	 */
	public void savePushMessage(List<Resource> msgs)throws Exception;
	
	
	
	/**
	 * 
	 * 删除silent
	 * 
	 * @param msg
	 */
	public void deletePushMessage(long id)throws Exception;
	
	
	
	
}
