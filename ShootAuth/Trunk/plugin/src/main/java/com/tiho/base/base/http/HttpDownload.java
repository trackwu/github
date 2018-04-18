package com.tiho.base.base.http;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.util.StringUtils;

import android.content.Context;

/**
 * http下载
 * 
 * @author Joey.Dai
 * 
 */
public class HttpDownload {

	/**
	 * 下载地址
	 */
	private String url;

	/**
	 * 保存目录
	 */
	private String saveDir;

	/**
	 * 临时文件
	 */
	private File tmpFile;

	/**
	 * 最终文件
	 */
	private File finalFile;
	private String finalName;

	/**
	 * 当前的偏移量
	 */
	private long offset;

	/**
	 * 总长度
	 */
	private long totalLength;

	private String suffix;

	/**
	 * 数据接收回调
	 */
	private DownloadStatus callback;

	private Context context;

	private String userAgent;

	/**
	 * 构造一个下载器
	 * 
	 * @param url
	 *            地址
	 * @param saveDir
	 *            存放目录
	 */
	public HttpDownload(Context context, String url, String saveDir, String userAgent, long length, String suffix) {
		this(context, url, saveDir, userAgent, null, length, suffix);
	}

	/**
	 * 
	 * @param url
	 *            地址
	 * @param saveDir
	 *            存放目录
	 * @param callback
	 *            数据回调
	 */
	public HttpDownload(Context context, String url, String saveDir, String userAgent, DownloadStatus callback, long length, String suffix) {
		super();
		this.context = context;
		this.url = url;
		this.saveDir = saveDir;
		this.callback = callback;
		this.userAgent = userAgent;
		this.totalLength = length;
		this.suffix = suffix;

		try {
			initTmpFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getTmpFile() {
		return tmpFile;
	}

	/**
	 * 开始下载
	 * 
	 * @throws HttpException
	 * @throws IOException
	 */
	public void execute() throws Exception {
		
		HttpGet get = new HttpGet(url);
		get.addHeader("RANGE", "bytes=" + offset + "-");
		HttpResponse response = executeHttp(get);

		int sc = response.getStatusLine().getStatusCode();

		if (sc == HttpStatus.SC_OK || sc == HttpStatus.SC_PARTIAL_CONTENT) {
			processData(sc, response);

		} else if (sc == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
			tmpFile.delete();
		}
	}

	private void processData(int sc, HttpResponse response) throws IOException {

		parseFinalFile(response.getFirstHeader("content-disposition"));
		 if (finalFile.exists()) {
			 tmpFile.delete();
			 finalFile.renameTo(tmpFile);
			 LogManager.LogShow(finalFile.getName()+"已经存在");
			 
			 return;
		 }
		 
		 

		RandomAccessFile raf = null;
		raf = getCurrentFile();

		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		
		is = isGzip(response) ? new GZIPInputStream(is) : is;
		
		byte[] buf = new byte[4096];
		int len = -1;

		while ((len = is.read(buf)) != -1) {
			offset += len;

			raf.write(buf, 0, len);

			notifyCallback();
		}

		// if (isDone()) {
		// tmpFile.renameTo(finalFile);
		// }

		closeFile(raf);

	}

	private boolean isGzip(HttpResponse resp) {
		Header head = resp.getFirstHeader("Content-Encoding");

		return head != null && head.getValue().toLowerCase().indexOf("gzip") != -1;
	}

	private void notifyCallback() {
		if (callback != null)
			callback.downloaded(finalFile.getName(), offset, totalLength);
	}

	private void closeFile(Closeable ca) {
		try {
			if (ca != null)
				ca.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private HttpResponse executeHttp(HttpGet get) throws HttpException {
		try {
			return ClientFactory.getCustomUAClient(context, userAgent).execute(get);
		} catch (Exception e) {
			throw new HttpException(e.getMessage(), e);
		}
	}

	/**
	 * 解析总长度
	 * 
	 * @param contentRangeHeader
	 */
	// private void parseTotalLength(int sc, HttpResponse response) {
	// if (sc == HttpStatus.SC_OK) {
	// // 如果是200 ， 以content-length作为总长度
	// Header h = response.getFirstHeader("Content-Length");
	// totalLength = NumberUtil.toLong(h.getValue());
	// } else if (sc == HttpStatus.SC_PARTIAL_CONTENT) {
	//
	// // 如果是206，把Content-Range 最后部分作为总长度
	// Header contentRangeHeader = response.getFirstHeader("Content-Range");
	// if (contentRangeHeader != null) {
	// String contentRange = contentRangeHeader.getValue();
	// String[] ranges = contentRange.split(" ");
	// if (ranges.length == 2) {
	// String[] bytes = ranges[1].split("/");
	// if (bytes.length == 2)
	// totalLength = NumberUtil.toLong(bytes[1]);
	// }
	// }
	// }
	// }

	private RandomAccessFile getCurrentFile() throws IOException {

		RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw");

		raf.seek(offset);

		return raf;
	}

	/**
	 * 初始化临时文件
	 * 
	 * @throws IOException
	 */
	private void initTmpFile() throws IOException {
		tmpFile = new File(saveDir, String.valueOf(url.hashCode()));

		File parent = tmpFile.getParentFile();
		if (!parent.exists())
			parent.mkdirs();

		if (!tmpFile.exists())
			tmpFile.createNewFile();

		if (totalLength == -1)
			offset = 0;
		else
			offset = tmpFile.length();
	}

	/**
	 * 判断最终的文件名
	 * 
	 * @param contentDispositionHeader
	 */
	private void parseFinalFile(Header contentDispositionHeader) {
		String name = null;

		if (contentDispositionHeader != null) {
			// 按照content-disposition取文件名
			name = StringUtils.stringBetween(contentDispositionHeader.getValue(), "filename=\"", "\"");
		}

		if (StringUtils.isEmpty(name)) {
			// 没有的话，按照url哈希取正值
			name = String.valueOf(Math.abs(url.hashCode())) + "." + suffix;
		}
		if(!StringUtils.isEmpty(finalName)){
			name =finalName;
		}

		finalFile = new File(saveDir, name);
	}
	
	public void setFinalName(String finalName){
		this.finalName=finalName;
	}

	public void genTargetFile() {
		tmpFile.renameTo(finalFile);
	}

	public File getFinalFile() {
		return finalFile;
	}
	
	public File setFinalFile(File f) {
		return this.finalFile=f;
	}

	public static interface DownloadStatus {

		public void downloaded(String name, long current, long total);
	}
}
