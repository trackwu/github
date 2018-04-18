package com.tiho.base.base.http;


/**
 * 数据接收到接口
 * @author Joey.Dai
 *
 */
public interface ReceiveDataStream {

	/**
	 * 收到数据
	 * @param data 收到的数据
	 * @param offset 开始位置
	 * @param len 长度
	 */
	public void dataReceive(byte[] data, int offset, int len, long totalSize)throws Exception;
	
}
