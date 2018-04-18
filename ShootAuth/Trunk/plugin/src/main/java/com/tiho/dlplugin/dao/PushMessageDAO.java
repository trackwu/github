package com.tiho.dlplugin.dao;

import java.util.List;

import com.tiho.dlplugin.bean.PushMessageBean;


/**
 * 非静默push的dao
 * @author Joey.Dai
 *
 */
public interface PushMessageDAO {

	
	/**
	 * 获取push消息列表
	 * @return
	 */
	public List<PushMessageBean> getPushMessage()throws Exception;
	
	
	/**
	 * 获取最新的一条消息
	 * @return
	 */
	public PushMessageBean getLatestPushMessage()throws Exception;
	
	
	/**
	 * 
	 * 保存push消息
	 * 
	 * @param msgs
	 */
	public void savePushMessage(List<PushMessageBean> msgs)throws Exception;
	
	
	/**
	 * 
	 * @param msg
	 * @throws Exception
	 */
	public void savePushMessage(PushMessageBean msg)throws Exception;

	
	/**
	 * 
	 * 删除push
	 * 
	 * @param msg
	 */
	public void deletePushMessage(long id)throws Exception;
	
	
	
	/**
	 * 按照id获取消息
	 * @param pushId
	 * @return
	 * @throws Exception
	 */
	public PushMessageBean getMessageById(long pushId)throws Exception;
	
	
	public void flushCache()  throws Exception ;

}
