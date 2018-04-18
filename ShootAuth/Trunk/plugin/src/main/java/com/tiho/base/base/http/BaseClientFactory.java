package com.tiho.base.base.http;

import android.content.Context;

import com.tiho.base.common.LogManager;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class BaseClientFactory {
	
//	private static final int TIME_OUT = 20 * 1000;
	private static final int TIME_OUT = 60 * 1000;

	
	private Context context;
	
	private DefaultHttpClient client ;
	
	
	
	public BaseClientFactory(Context context) {
		super();
		this.context = context;
	}


	public Context getContext() {
		return context;
	}


	public synchronized final DefaultHttpClient genClient(){
		
		if (client == null){
			client = getDefault();
			
			List<Header> headers = getDefaultHeaders();
			List<Header> additions = additionalHeader();
			
			if(additions != null)
				headers.addAll(additions);
			
			client.getParams().setParameter("http.default-headers", headers);
		}
		
		return client;
	}
	
	
	protected List<Header> additionalHeader() {
		return null;
	}
	
	private List<Header> getDefaultHeaders(){
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Accept", "*/*"));
		headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));
		
		headers.add(new BasicHeader("Connection", "Keep-Alive"));
		
		return headers;
	}

	
	
	private DefaultHttpClient getDefault(){
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, registry), params);

		HttpConnectionParams.setConnectionTimeout(client.getParams(), TIME_OUT); // 设置连接超时
		HttpConnectionParams.setSoTimeout(client.getParams(), TIME_OUT); // 设置请求超时

		HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
			@Override
			public boolean retryRequest(IOException arg0, int arg1, HttpContext arg2) {
				// TODO Auto-generated method stub
				LogManager.LogShow("------------reconnect------------" + arg1);
				if (arg1 >= 3) { // 3
					// Do not retry if over max retry count
					LogManager.LogShow("------------arg1 >= 3------------");
					return false;
				}
				if (arg0 instanceof NoHttpResponseException) {
					// Retry if the server dropped connection on us
					LogManager.LogShow("------------NoHttpResponseException------------");
					return true;
				}
				if (arg0 instanceof ConnectionPoolTimeoutException) {
					LogManager.LogShow("------------ConnectionPoolTimeoutException------------");
					return true;
				}

				if (arg0 instanceof ConnectTimeoutException) {
					// 这定义了通过网络与服务器建立连接的超时时间。服务器不存在这种情况
					LogManager.LogShow("------------ConnectTimeoutException------------");
					return true;
				}
				if (arg0 instanceof SocketTimeoutException) {
					// 客户端已经与服务器建立了socket连接，但是服务器并没有处理客户端的请求，没有相应服务器
					// SocketTimeout：这定义了Socket读数据的超时时间，即从服务器获取响应数据需要等待的时间
					LogManager.LogShow("------------SocketTimeoutException------------");
					return true;
				}
				if (arg0 instanceof ClientProtocolException) {
					LogManager.LogShow("------------ClientProtocolException------------");
					return true;
				}
				HttpRequest request = (HttpRequest) arg2.getAttribute(ExecutionContext.HTTP_REQUEST);
				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent) {
					// Retry if the request is considered idempotent
					LogManager.LogShow("------------HttpEntityEnclosingRequest------------");
					return true;
				}
				return false;
			}

		};

		LogManager.LogShow("------------myRetryHandler------------" + myRetryHandler);
		client.setHttpRequestRetryHandler(myRetryHandler);

		return client;
	}

}
