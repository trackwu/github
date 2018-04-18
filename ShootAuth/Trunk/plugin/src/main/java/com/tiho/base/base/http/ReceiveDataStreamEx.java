package com.tiho.base.base.http;


/**
 * 数据接收到接口Ex
 * @author Think.Han
 *
 */
public interface ReceiveDataStreamEx {

	/**
	 * 收到数据
	 * @param data 收到的数据
	 * @param offset 开始位置
	 * @param len 长度
	 * @param total 总长度
	 */
	public void dataReceive(byte[] data , int offset , int len, long total)throws Exception;
	
}
