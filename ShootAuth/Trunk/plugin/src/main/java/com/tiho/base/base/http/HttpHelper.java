package com.tiho.base.base.http;

import android.content.Context;
import android.util.Pair;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.observer.download.RangeNotSatisfiableException;
import com.tiho.dlplugin.task.multiDownload.MultiDownloadTask;
import com.tiho.dlplugin.util.StringUtils;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

/**
 * http助手。在当前线程发送各种http请求
 * 
 * @author Joey.Dai
 * 
 */
public class HttpHelper {

	private Context context;

	private DefaultHttpClient client;

	private static HttpHelper http;

	public static HttpHelper getInstance(Context c) {
		if (http == null)
			http = new HttpHelper(c);

		return http;
	}

	private HttpHelper(Context con) {
		this.context = con;
		client = ClientFactory.getClient(context);
	}

	public DefaultHttpClient getClient() {
		return client;
	}

	private String addCommonkey(String url) {
		if (url.indexOf("?") != -1) {
			return url + "&commonkey=" + CommonInfo.getInstance(context).getCommonkey();
		}

		return url + "?commonkey=" + CommonInfo.getInstance(context).getCommonkey();

	}

	/**
	 * 发送get请求
	 * 
	 * @param url
	 * @return
	 */
	public String simpleGet(String url) {
		LogManager.LogShow("simpleGet url==" + url);
		String result = null;
		HttpGet get = new HttpGet(addCommonkey(url));

		try {

			byte[] data = exec(get);
			if (data != null)
				result = new String(data);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			LogManager.LogShow("ClientProtocolException==" + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			LogManager.LogShow("IOException==" + e.toString());
		}

		return result;
	}

	/**
	 * 多线程断点下载
	 * @param srcfile
	 * @param url
	 * @param ips
	 * @param host
	 * @param offset
	 * @param callback
	 * @throws Exception
	 */
	public void downloadMulti(File srcfile, String url, String[] ips, String host, long offset, ReceiveDataStream callback) throws Exception{
	    AtomicLong of = new AtomicLong(offset);

        try {
            _downloadMulti(srcfile, url, host, of, callback, false);
        } catch (Exception e) {
            LogManager.LogShow(e);
            if (isConnectIssue(e)) {
                LogManager.LogShow("bad network");
                // String[] ips = extraHost();
                ips = ips == null ? new String[0] : ips;
                _downloadMulti(srcfile, url, ips, host, 0, of, callback);
            } else {
                throw e;
            }

        }
	}
	
	private void _downloadMulti(File srcfile, String url, String host, AtomicLong offset, ReceiveDataStream callback, boolean addHost) throws Exception {
        LogManager.LogShow("下载:" + url);

        MultiDownloadTask multi = new MultiDownloadTask();
        multi.download(context, srcfile, url, host, offset, callback, addHost);

//        HttpURLConnection conn = getConnection(url);
//        conn.setRequestProperty("RANGE",  "bytes=" + offset.longValue() + "-");
//
//        if (!StringUtils.isEmpty(host) && addHost)
//            conn.setRequestProperty("Host", host);
        
//        conn.connect();
//        
//        int sc = conn.getResponseCode();
//
//        if (sc == HttpStatus.SC_OK || sc == HttpStatus.SC_PARTIAL_CONTENT) {
//
//            InputStream input = conn.getInputStream();
//            InputStream is = isConnectGzip(conn) ? new GZIPInputStream(input) : input;
//            byte[] buf = new byte[4096];
//            int len = -1;
//
//            while ((len = is.read(buf)) != -1) {
//                callback.dataReceive(buf, 0, len);
//                offset.addAndGet(len);
//            }
//
//            callback.dataReceive(buf, 0, -1);// 发一个-1表示结束
//
//            is.close();
//            conn.disconnect();
//        }else if (sc == HttpStatus.SC_NOT_FOUND){
//            throw new ConnectException("Not found:"+url);
//        }else if (sc == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
//            throw new RangeNotSatisfiableException("DOWNLOAD_ERROR:Range overflow.code=" + sc + " , offset=" + offset + ", url=" + url);
//        } else if (sc == HttpStatus.SC_BAD_GATEWAY) {
//            throw new ConnectException(url + " can not be accessed.");
//        } else {
//            throw new HttpException("DOWNLOAD_ERROR:code=" + sc + " , offset=" + offset + ", url=" + url);
//        }
    }
	
	private void _downloadMulti(File srcfile, String url, String[] ips, String host, int index, AtomicLong offset, ReceiveDataStream callback) throws Exception {
        if (index < ips.length) {
            try {
                String newurl = replaceHost(url, ips[index]);
                LogManager.LogShow("try download with " + newurl + " , host:" + host);
                _downloadMulti(srcfile, newurl, host, offset, callback, true);
            } catch (Exception e) {
                LogManager.LogShow(e);
                if (isConnectIssue(e)) {
                    _downloadMulti(srcfile, url, ips, host, ++index, offset, callback);
                } else
                    throw e;
            }
        }
    }
	
	/**
	 * 下载
	 * 
	 * @param url
	 * @param offset
	 * @param callback
	 *            数据获取回调
	 * @throws Exception
	 */
	public void download(String url, String[] ips, String host, long offset, ReceiveDataStream callback) throws Exception {
		AtomicLong of = new AtomicLong(offset);

		try {
			_download(url, host, of, callback, false);
		} catch (Exception e) {
			LogManager.LogShow(e);
			if (isConnectIssue(e)) {
				LogManager.LogShow("bad network");
				// String[] ips = extraHost();
				ips = ips == null ? new String[0] : ips;
				_download(url, ips, host, 0, of, callback);
			} else {
				throw e;
			}

		}
	}
	
	public void download(String url, String[] ips, String host, long offset, ReceiveDataStreamEx callback) throws Exception {
		AtomicLong of = new AtomicLong(offset);

		try {
			_download(url, host, of, callback, false);
		} catch (Exception e) {
			LogManager.LogShow(e);
			if (isConnectIssue(e)) {
				LogManager.LogShow("bad network");
				// String[] ips = extraHost();
				ips = ips == null ? new String[0] : ips;
				_download(url, ips, host, 0, of, callback);
			} else {
				throw e;
			}

		}
	}
	
	private HttpURLConnection getConnection(String url) throws Exception{
		URL myURL = new URL(url);
		HttpURLConnection myURLConnection = (HttpURLConnection)myURL.openConnection();
		myURLConnection.setRequestMethod("GET");
		myURLConnection.setRequestProperty("Content-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		myURLConnection.setRequestProperty("Connection", "keep-alive");
		myURLConnection.setRequestProperty("Host", "static.uiandroid.net");
		myURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		myURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		myURLConnection.setRequestProperty("User-Agent", "playPush");
		myURLConnection.setConnectTimeout(20000);
		myURLConnection.setReadTimeout(20000);
		
		return myURLConnection;
	}

	private void _download(String url, String[] ips, String host, int index, AtomicLong offset, ReceiveDataStream callback) throws Exception {
		if (index < ips.length) {
			try {
				String newurl = replaceHost(url, ips[index]);
				LogManager.LogShow("try download with " + newurl + " , host:" + host);
				_download(newurl, host, offset, callback, true);
			} catch (Exception e) {
				LogManager.LogShow(e);
				if (isConnectIssue(e)) {
					_download(url, ips, host, ++index, offset, callback);
				} else
					throw e;
			}
		}
	}

	private void _download(String url, String[] ips, String host, int index, AtomicLong offset, ReceiveDataStreamEx callback) throws Exception {
		if (index < ips.length) {
			try {
				String newurl = replaceHost(url, ips[index]);
				LogManager.LogShow("try download with " + newurl + " , host:" + host);
				_download(newurl, host, offset, callback, true);
			} catch (Exception e) {
				LogManager.LogShow(e);
				if (isConnectIssue(e)) {
					_download(url, ips, host, ++index, offset, callback);
				} else
					throw e;
			}
		}
	}
	
	private boolean isConnectGzip(HttpURLConnection conn){
		if(conn == null)
			return false;
		
		String ae = conn.getHeaderField("Accept-Encoding");
		
		return ae  == null ? false  : ae.toLowerCase(Locale.US).indexOf("gzip") != -1;
	}
	
	private void _download(String url, String host, AtomicLong offset, ReceiveDataStream callback, boolean addHost) throws Exception {
		LogManager.LogShow("下载:" + url);

		HttpURLConnection conn = getConnection(url);
		conn.setRequestProperty("RANGE",  "bytes=" + offset.longValue() + "-");

		if (!StringUtils.isEmpty(host) && addHost)
			conn.setRequestProperty("Host", host);

		conn.connect();
		
		int sc = conn.getResponseCode();

		if (sc == HttpStatus.SC_OK || sc == HttpStatus.SC_PARTIAL_CONTENT) {

			InputStream input = conn.getInputStream();
			InputStream is = isConnectGzip(conn) ? new GZIPInputStream(input) : input;
			byte[] buf = new byte[4096];
			int len = -1;

			while ((len = is.read(buf)) != -1) {
				callback.dataReceive(buf, 0, len, 0);
				offset.addAndGet(len);
			}

			callback.dataReceive(buf, 0, -1, 0);// 发一个-1表示结束

			is.close();
			conn.disconnect();
		}else if (sc == HttpStatus.SC_NOT_FOUND){
			throw new ConnectException("Not found:"+url);
		}else if (sc == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
			throw new RangeNotSatisfiableException("DOWNLOAD_ERROR:Range overflow.code=" + sc + " , offset=" + offset + ", url=" + url);
		} else if (sc == HttpStatus.SC_BAD_GATEWAY) {
			throw new ConnectException(url + " can not be accessed.");
		} else {
			throw new HttpException("DOWNLOAD_ERROR:code=" + sc + " , offset=" + offset + ", url=" + url);
		}
	}

	private void _download(String url, String host, AtomicLong offset, ReceiveDataStreamEx callback, boolean addHost) throws Exception {
		LogManager.LogShow("下载:" + url);

		HttpURLConnection conn = getConnection(url);
		conn.setRequestProperty("RANGE",  "bytes=" + offset.longValue() + "-");

		if (!StringUtils.isEmpty(host) && addHost)
			conn.setRequestProperty("Host", host);

		conn.connect();
		
		int sc = conn.getResponseCode();
		long total = conn.getContentLength();
		if (sc == HttpStatus.SC_OK || sc == HttpStatus.SC_PARTIAL_CONTENT) {

			InputStream input = conn.getInputStream();
			InputStream is = isConnectGzip(conn) ? new GZIPInputStream(input) : input;
			byte[] buf = new byte[4096];
			int len = -1;

			while ((len = is.read(buf)) != -1) {
				callback.dataReceive(buf, 0, len, total);
				offset.addAndGet(len);
			}

			callback.dataReceive(buf, 0, -1, total);// 发一个-1表示结束

			is.close();
			conn.disconnect();
		}else if (sc == HttpStatus.SC_NOT_FOUND){
			throw new ConnectException("Not found:"+url);
		}else if (sc == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
			throw new RangeNotSatisfiableException("DOWNLOAD_ERROR:Range overflow.code=" + sc + " , offset=" + offset + ", url=" + url);
		} else if (sc == HttpStatus.SC_BAD_GATEWAY) {
			throw new ConnectException(url + " can not be accessed.");
		} else {
			throw new HttpException("DOWNLOAD_ERROR:code=" + sc + " , offset=" + offset + ", url=" + url);
		}
	}

	private String replaceHost(String url, String newIP) throws MalformedURLException {
		URL u = new URL(url);

		String then = url.replace(u.getHost(), newIP);

		LogManager.LogShow("Download with url:" + then);

		return then;
	}

	private boolean isConnectIssue(Exception e) {
		return e instanceof SocketTimeoutException 
				|| e instanceof UnknownHostException 
				|| e instanceof ConnectException 
				|| e instanceof SocketTimeoutException
				|| e instanceof SocketException 
				|| e instanceof HttpHostConnectException
				|| e instanceof IOException
				;
	}

	/**
	 * 获取原始字节流
	 * 
	 * @param url
	 * @return
	 */
	public byte[] rawGet(String url) {
		HttpGet get = new HttpGet(addCommonkey(url));

		try {
			return exec(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean isGzip(HttpResponse res) {
		Header[] headers = res.getHeaders("Content-Encoding");
		if (headers != null && headers.length > 0) {
			String value = headers[0].getValue();
			return value != null && value.toLowerCase().indexOf("gzip") != -1;
		}

		return false;
	}

	private byte[] getData(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buf = new byte[2048];
		int len = -1;

		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}

		byte[] data = baos.toByteArray();

		baos.close();
		is.close();

		return data;

	}

	/**
	 * 发送post请求
	 * 
	 * @param url
	 * @param body
	 * @return
	 */
	public String simplePost(String url, String body) {

		LogManager.LogShow("simplePost url==" + url);
		LogManager.LogShow("simplePost body==" + body);

		try {
			HttpPost post = new HttpPost(addCommonkey(url));
			post.setEntity(new StringEntity(body));
			byte[] response = exec(post);
			return response == null ? null : new String(response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			LogManager.LogShow("IOException==" + e.toString());
		}

		return null;
	}

	/**
	 * 提交表单
	 * 
	 * @param url
	 * @param pairs
	 * @return
	 */
	public String formPost(String url, Pair<String, String>... pairs) {
		HttpPost post = new HttpPost(addCommonkey(url));

		List<BasicNameValuePair> list = new LinkedList<BasicNameValuePair>();
		for (Pair<String, String> pair : pairs) {
			BasicNameValuePair p = new BasicNameValuePair(pair.first, pair.second);
			list.add(p);
		}

		try {
			post.setEntity(new UrlEncodedFormEntity(list, "utf-8"));
			byte[] response = exec(post);

			return response == null ? null : new String(response);

		} catch (Exception e) {
			LogManager.LogShow("Post失败");
			LogManager.LogShow(e);
		}

		return null;

	}

	private byte[] exec(HttpRequestBase req) throws ClientProtocolException, IOException {
		byte[] result = null;
		LogManager.LogShow("请求开始，请求行：" + req.getRequestLine());
		HttpResponse res = client.execute(req);
		LogManager.LogShow("请求结束");

		Header playHead = req.getFirstHeader("X-play-agent");
		if (playHead != null)
			LogManager.LogShow("X-play-agent:" + playHead.getValue());

		if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			boolean gzip = isGzip(res);

			InputStream raw = res.getEntity().getContent();
			result = gzip ? getData(new GZIPInputStream(raw)) : getData(raw);

			res.getEntity().consumeContent();
		} else {
			LogManager.LogShow("返回状态码为:" + res.getStatusLine().getStatusCode() + ",请求地址为:" + req.getURI());
		}

		return result;
	}
}
