/**
 * 
 */
package com.tiho.base.base.dlapk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tiho.base.base.Base64;
import com.tiho.base.base.HttpCommonUtil;
import com.tiho.base.base.http.GzipDecompressingEntity;
import com.tiho.base.base.md.Md5Handler;
import com.tiho.base.common.CfgIni;
import com.tiho.base.common.LogManager;

/**
 * @author seward
 * 
 */
public class DlApk {
	public static final int DLAPK_REALDL_NEWVERSION = 0;
	public static final int DLAPK_REALDL_OK = 1;
	public static final int DLAPK_REALDL_FILEERR = 2;
	public static final int DLAPK_REALDL_PROGRESS = 3;
	public static final int DLAPK_REALDL_NETWORKERR = 4;
	public static final int DLAPK_CHECK_NETWORKERR = 5;
	public static final int DLAPK_CHECK_TIMEOUT = 6;
	public static final int DLAPK_CHECK_OK = 7;

	private static final int REQUEST_TIMEOUT = 30 * 1000;
	private static final int SO_TIMEOUT = 30 * 1000;

	private String apk = "";
	public long apkver = 0;
	private String url = "";
	private String md5 = "";
	private String savepath = "";
	private IDownLoadApkCB cb;
	private String commonKey = "";
	private String xPlayagent = "";
	private String xml = "";

	private ApkSelf apkSelf = null;
	private boolean isStop = true;
	private Thread thread = null;
	private int dlMethod = 0;// 0:先获取url,接着下载，1：通过url下载apk 2：检查版本

	public static interface IDownLoadApkCB {
		public void callback(int result, HashMap<String, Object> data);
	}

	// 通过url直接下载

	public DlApk(String url, String apk, String savepath, String md5, IDownLoadApkCB cb) {
		String mcc = "";
		if (HttpCommonUtil.getInstance() != null)
			mcc = HttpCommonUtil.getInstance().getImsi();
		mcc = (mcc.length() > 3) ? mcc.substring(0, 3) : "999";
		if (HttpCommonUtil.getInstance() != null) {
			this.commonKey = HttpCommonUtil.getInstance().commonkey();
			this.xPlayagent = HttpCommonUtil.getInstance().xPlayAgent();
		}

		this.apk = apk;
		this.apkver = 0;
		this.savepath = savepath;
		this.cb = cb;

		this.url = url;
		this.md5 = md5;

		this.xml = "{\"id\":\"" + new String(Base64.encode(apk.getBytes())) + "\"}";
		dlMethod = 1;
		LogManager.LogShow("apkver = " + apkver + " apk = " + apk + " savepath = " + savepath);
		LogManager.LogShow("this.url = " + this.url + " this.md5 = " + this.md5 + " savepath = " + savepath);
	}

	// 直接下载，不判断版本号（先检查版本，然后找到url后，直接下载）
	public DlApk(String apk, String savepath, IDownLoadApkCB cb, boolean isJson) {
		String mcc = "";
		if (HttpCommonUtil.getInstance() != null)
			mcc = HttpCommonUtil.getInstance().getImsi();
		mcc = (mcc.length() > 3) ? mcc.substring(0, 3) : "999";
		if (HttpCommonUtil.getInstance() != null) {
			this.commonKey = HttpCommonUtil.getInstance().commonkey();
			this.xPlayagent = HttpCommonUtil.getInstance().xPlayAgent();
		}
		this.apk = apk;
		this.apkver = 0;
		this.savepath = savepath;
		this.cb = cb;
		// {"id":"bWUucG93ZXJwbGF5LnBsYXlhcHA\u003d"}
		this.xml = "{\"id\":\"" + new String(Base64.encode(apk.getBytes())) + "\"}";
		dlMethod = 0;
		LogManager.LogShow("this.xml = " + this.xml);
		LogManager.LogShow("apkver = " + apkver + " apk = " + apk + " savepath = " + savepath);
	}

	public DlApk(String apk, int versionCode, String savepath, IDownLoadApkCB cb, boolean isJson) {
		this(apk, savepath, cb, isJson);
		this.apkver = versionCode;
	}

	public void checkVer() {
		dlMethod = 2;
		start();
	}

	public void start() {
		if (thread == null) {
			thread = new Thread() {

				@Override
				public void run() {
					isStop = false;
					dl();
					thread = null;
				}

			};
			thread.start();
		}
	}

	public void stop() {
		isStop = true;
	}

	HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

		@Override
		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
			// TODO Auto-generated method stub
			LogManager.LogShow("------------reconnect------------" + executionCount);
			LogManager.LogShow(exception);
			if (executionCount >= 5) { // 5
				// Do not retry if over max retry count
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				// Retry if the server dropped connection on us
				LogManager.LogShow("------------NoHttpResponseException------------");
				return true;
			}
			if (exception instanceof ConnectionPoolTimeoutException) {
				LogManager.LogShow("------------ConnectionPoolTimeoutException------------");
				return true;
			}

			if (exception instanceof ConnectTimeoutException) {
				// 这定义了通过网络与服务器建立连接的超时时间。服务器不存在这种情况
				LogManager.LogShow("------------ConnectTimeoutException------------");
				return true;
			}
			if (exception instanceof SocketTimeoutException) {
				// 客户端已经与服务器建立了socket连接，但是服务器并没有处理客户端的请求，没有相应服务器
				// SocketTimeout：这定义了Socket读数据的超时时间，即从服务器获取响应数据需要等待的时间
				LogManager.LogShow("------------SocketTimeoutException------------");
				return true;
			}
			if (exception instanceof ClientProtocolException) {
				LogManager.LogShow("------------ClientProtocolException------------");
				return true;
			}
			HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
			if (idempotent) {
				// Retry if the request is considered idempotent
				LogManager.LogShow("------------HttpEntityEnclosingRequest------------");
				return true;
			}

			return false;
		}

	};

	private void dl() {
		// 是否存在apk已经下载成功
		ApkSelf self = null;
		LogManager.LogShow("Update dlMethod = " + dlMethod);
		if (dlMethod == 0) {
			self = checkVersion();
		} else if (dlMethod == 1) {
			self = new ApkSelf();
			self.setId(this.apk);
			self.setUrl(this.url);
			self.setMd5(this.md5);
			self.setVersionCode((int) this.apkver);
			LogManager.LogShow("Update dlMethod = " + dlMethod);
			apkSelf = self;
		} else if (dlMethod == 2) {
			self = checkVersion();
			if (self != null) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("apkver", self.getVersionCode());
				map.put("url", self.getUrl());
				map.put("md5", self.getMd5());
				map.put("pkgname", self.getId());
				cb.callback(DLAPK_CHECK_OK, map);
			} else {
				cb.callback(DLAPK_CHECK_NETWORKERR, null);
			}
			return;
		}
		if (self != null) {
			LogManager.LogShow("Update apkSelf.getVersionCode() = " + apkSelf.getVersionCode());

			// if(apkver>0&&apkSelf.getVersionCode() <= apkver){
			// cb.callback(DLAPK_REALDL_NEWVERSION, null);
			// return;
			// }
			LogManager.LogShow("tempFile = " + getTempFileName());
			if (isLocked()) {
				LogManager.LogShow("isLocked");
				cb.callback(DLAPK_REALDL_FILEERR, null);
				return;
			}
			if (!apkIsExist(apk)) {
				long range = 0;
				File tempFile = new File(getTempFileName());
				LogManager.LogShow("Update apkPath" + getTempFileName());
				if (tempFile.exists()) {
					range = tempFile.length();
				} else {
					try {
						tempFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LogManager.LogShow("isLocked");
						cb.callback(DLAPK_REALDL_FILEERR, null);
						return;
					}
					String cmd = "chmod 777  " + getTempFileName(); // 755
					try {
						Runtime.getRuntime().exec(cmd);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

				realDl(range);
				return;
			} else {
				LogManager.LogShow("no Update apkPath");

				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("path", getFileName());

				cb.callback(DLAPK_REALDL_OK, map);
				return;
			}
		}
		LogManager.LogShow("no Update network err");
		cb.callback(DLAPK_CHECK_NETWORKERR, null);
	}

	private boolean isLocked() {
		RandomAccessFile out;
		try {
			out = new RandomAccessFile(getTempFileName(), "rw");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		FileChannel fcout = out.getChannel();
		FileLock flout = null;
		try {
			flout = fcout.tryLock();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (flout != null) {// 获取到lock，说明没有被其他进程lock
			try {
				flout.release();
				fcout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return false;
		}
		try {
			fcout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private boolean apkIsExist(String apk) {
		String tmp = savepath + apkSelf.getId() + ".apk";
		File apktmp = new File(tmp);
		if (apktmp.exists()) {
			Md5Handler hash = new Md5Handler();
			if (apkSelf.getMd5().equals(hash.md5Calc(apktmp))) {
				return true;
			} else {
				apktmp.delete();
			}
		}
		return false;
	}

	private String getTempFileName() {
		String tmp = savepath + apkSelf.getId() + apkSelf.getVersionCode();
		String tempFile = tmp + ".apk.tmp";

		return tempFile;
	}

	private String getFileName() {
		String tmp = savepath + apkSelf.getId();
		String tempFile = tmp + ".apk";
		return tempFile;
	}

	private void realDl(long range) {

		if (HttpCommonUtil.getInstance() != null) {
			this.commonKey = HttpCommonUtil.getInstance().commonkey();
			this.xPlayagent = HttpCommonUtil.getInstance().xPlayAgent();
		}
		HttpGet getMethod = new HttpGet(apkSelf.getUrl());
		LogManager.LogShow("apkSelf.getUrl() = " + apkSelf.getUrl());
		getMethod.addHeader("Content-Type", "application/octet-stream");
		getMethod.addHeader("X-play-agent", xPlayagent);
		getMethod.addHeader("Accept", "*/*");
		getMethod.addHeader("Accept-Encoding", "gzip, deflate");
		getMethod.addHeader("Connection", "Keep-Alive");
		getMethod.addHeader("User-Agent", "Playbase");
		if (range > 0)
			getMethod.addHeader("Range", "bytes=" + range + "-");
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
		HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
		getMethod.setParams(httpParams);

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		InputStream input = null;
		RandomAccessFile saveit = null;
		FileChannel fcout = null;
		FileLock flout = null;
		LogManager.LogShow("------------myRetryHandler------------" + myRetryHandler);
		httpClient.setHttpRequestRetryHandler(myRetryHandler);

		try {
			// postMethod.geta
			HttpResponse response = httpClient.execute(getMethod);
			LogManager.LogShow(Arrays.toString(getMethod.getAllHeaders()));
			LogManager.LogShow("resCode = " + response.getStatusLine().getStatusCode()); // 获取响应码
			int code = response.getStatusLine().getStatusCode();
			if (code == 200 || code == 206) {
				if (response.getEntity() != null) {
					long totalSize = response.getEntity().getContentLength();
					LogManager.LogShow("totalSize = " + totalSize);
					if (totalSize > 0) {
						if (response.getEntity().getContentEncoding() != null && response.getEntity().getContentEncoding().getValue().toLowerCase().indexOf("gzip") != -1) {
							input = new GZIPInputStream(response.getEntity().getContent());
						} else {
							input = response.getEntity().getContent();
						}
						byte data[] = new byte[1024 * 8];
						int bytesRead;
						long curSize = range;
						File file = new File(getTempFileName());
						LogManager.LogShow("getTempFileName = " + getTempFileName());
						LogManager.LogShow("getTempFileName exist0= " + file.exists());
						// saveit = new FileOutputStream(getTempFileName(),
						// true);
						// 对该文件加锁
						if (!file.exists())
							file.createNewFile();
						saveit = new RandomAccessFile(file, "rw");
						fcout = saveit.getChannel();
						flout = fcout.tryLock();
						while ((bytesRead = input.read(data)) != -1) {
							curSize += bytesRead;
							saveit.seek(saveit.length());
							saveit.write(data, 0, bytesRead);
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("cursize", curSize);
							map.put("totalsize", (long) totalSize + range);
							cb.callback(DLAPK_REALDL_PROGRESS, map);
							if (isStop) {
								throw new IOException("!!!!!!!!this excption is ok.apk stop by user !!!!!!!!");
							}
						}
						flout.release();
						fcout.close();
						saveit.close();
						input.close();
						File apktmp = new File(getTempFileName());
						Md5Handler hash = new Md5Handler();
						if (apkSelf.getMd5().equals(hash.md5Calc(apktmp))) {
							File apkname = new File(getFileName());
							apkname.delete();
							apktmp.renameTo(apkname);
							String cmd = "chmod 777  " + getFileName(); // 755
																		// 644
							try {
								Runtime.getRuntime().exec(cmd);
							} catch (Exception e) {
								e.printStackTrace();
							}

							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("path", apkname.getAbsolutePath());
							cb.callback(DLAPK_REALDL_OK, map);
							return;
						} else {
							apktmp.delete();
							// cb
							LogManager.LogShow("apkSelf.getMd5() = " + apkSelf.getMd5());
							LogManager.LogShow("hash.md5Calc(apktmp) = " + hash.md5Calc(apktmp));
							cb.callback(DLAPK_REALDL_FILEERR, null);
							return;
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			int ret = DLAPK_REALDL_NETWORKERR;
			e.printStackTrace();
			LogManager.LogShow("------------IOException------------" + e.toString());
			LogManager.LogShow(e);
			if (flout == null) {
				System.out.println("有其他线程正在操作该文件");
				ret = DLAPK_REALDL_FILEERR;
			}

			LogManager.LogShow("flout = " + flout);
			try {
				if (flout != null)
					flout.release();
				if (fcout != null)
					fcout.close();
				if (saveit != null)
					saveit.close();
				if (input != null)
					input.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			LogManager.LogShow("isStop = " + isStop);
			if (isStop) {
				return;
			}
			cb.callback(ret, null);
			return;

		}
		cb.callback(DLAPK_REALDL_NETWORKERR, null);
	}

	private ApkSelf checkVersion() {
		if (HttpCommonUtil.getInstance() != null) {
			this.commonKey = HttpCommonUtil.getInstance().commonkey();
			this.xPlayagent = HttpCommonUtil.getInstance().xPlayAgent();
		}
		// 先将参数放入List，再对参数进行URL编码
		List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("commonkey", commonKey));

		// 对参数编码
		String param = URLEncodedUtils.format(params, "UTF-8");
		LogManager.LogShow(param);
		String hostjson = CfgIni.getInstance().getValue("base", "dlapk", "http://122.227.207.66:8888/api/v21/apks/checkversion?format=json");
		param = hostjson + "&" + param;

		LogManager.LogShow("checkVersion = " + param);
		HttpPost postMethod = new HttpPost(param);
		postMethod.addHeader("Content-Type", "application/octet-stream");
		postMethod.addHeader("X-play-agent", xPlayagent);
		LogManager.LogShow("check version xplayagent:" + xPlayagent);

		postMethod.addHeader("Accept", "*/*");
		postMethod.addHeader("Accept-Encoding", "gzip, deflate");
		postMethod.addHeader("Connection", "Keep-Alive");
		postMethod.addHeader("User-Agent", "Playbase");
		postMethod.setEntity(new ByteArrayEntity(xml.getBytes()));
		LogManager.LogShow("xml = " + xml);
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
		HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
		// HttpProtocolParams.setUseExpectContinue(httpParams, false);
		postMethod.setParams(httpParams);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		InputStream input = null;

		try {
			// postMethod.geta
			HttpResponse response = httpClient.execute(postMethod);
			LogManager.LogShow(Arrays.toString(postMethod.getAllHeaders()));
			LogManager.LogShow("resCode = " + response.getStatusLine().getStatusCode()); // 获取响应码
			LogManager.LogShow("response.getEntity() = " + response.getEntity());
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				if (response.getEntity() != null) {
					// int count = (int)
					// response.getEntity().getContentLength();
					// LogManager.LogShow( "count = " + count);
					// if (count > 0) {

					String json;
					if (response.getEntity().getContentEncoding() != null && response.getEntity().getContentEncoding().getValue().toLowerCase().indexOf("gzip") != -1) {
						json = EntityUtils.toString(new GzipDecompressingEntity(response.getEntity()));
						LogManager.LogShow("GZIPInputStream = ");
					} else {
						json = EntityUtils.toString(response.getEntity());
						LogManager.LogShow("no  GZIPInputStream= ");
					}
					LogManager.LogShow("no  isgzip= ");
					parserJson(json);
				}
			}
			if (apkSelf != null && (apkSelf.getApkSize() == 0 || apkSelf.getUrl() == null || apkSelf.getUrl().length() == 0))
				apkSelf = null;
			return apkSelf;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogManager.LogShow("------------IOException------------" + e.toString());
			LogManager.LogShow(e);
		}
		return null;
	}

	private boolean parserJson(String json) {
		LogManager.LogShow("json = " + json);
		String url = "";
		try {
			JSONObject jsonObj = new JSONObject(json);
			if (null != jsonObj) {
				url = jsonObj.getString("resHost");
				LogManager.LogShow("url = " + url);
				jsonObj = jsonObj.getJSONObject("list");
				LogManager.LogShow("jsonObj = " + jsonObj);
				if (null != jsonObj) {
					JSONArray jsonArray = null;
					jsonArray = jsonObj.getJSONArray("data");
					if (jsonArray != null && jsonArray.length() > 0) {
						JSONObject jsonObjReal = jsonArray.optJSONObject(0);
						if (null != jsonObjReal) {
							apkSelf = new ApkSelf();
							int size = jsonObjReal.getInt("size");
							url = url + jsonObjReal.getString("path");
							int versionCode = jsonObjReal.getInt("code");
							String md5 = jsonObjReal.getString("md5");
							apkSelf.setApkSize(size);
							apkSelf.setVersionCode(versionCode);
							apkSelf.setMd5(md5);
							apkSelf.setUrl(url);
							apkSelf.setId(this.apk);
							LogManager.LogShow("apkSelf = " + apkSelf.toString());
							return true;
						}
					}
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogManager.LogShow(e);
			apkSelf = null;
		}
		return false;
	}
}
