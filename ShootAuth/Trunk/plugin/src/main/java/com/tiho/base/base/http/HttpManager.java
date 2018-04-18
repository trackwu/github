package com.tiho.base.base.http;

import com.tiho.dlplugin.http.RequestCallback;

import android.content.Context;
import android.os.Handler;


/**
 * 在另外的线程发送http请求
 * @author Joey.Dai
 *
 */
public class HttpManager {

	private Context context;

	private static HttpManager instance;

	private HttpManager(Context con) {
		this.context =con;
	}

	public static HttpManager getInstance(Context con) {
		if (instance == null)
			instance = new HttpManager(con);

		return instance;
	}
	
	
	
	/**
	 * get请求
	 * @param url
	 * @param handler
	 * @param ra
	 */
	public void simpleGet(final String url, final Handler handler, final RequestCallback ra) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String res = HttpHelper.getInstance(context).simpleGet(url);

				ra.setData(res);
				handler.post(ra);

			}
		}).start();
	}

	
	/**
	 * 获取原始数据
	 * @param url
	 * @param handler
	 * @param ra
	 */
	public void rawGet(final String url, final Handler handler, final RequestCallback ra) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] res = HttpHelper.getInstance(context).rawGet(url);

				ra.setData(res);
				handler.post(ra);

			}
		}).start();
	}

	
	/**
	 * post请求
	 * @param url
	 * @param body
	 * @param handler
	 * @param callback
	 */
	public void simplePost(final String url, final String body, final Handler handler, final RequestCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String res = HttpHelper.getInstance(context).simplePost(url, body);

				callback.setData(res);
				handler.post(callback);
			}
		}).start();
	}

}
