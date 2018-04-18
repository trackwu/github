package com.tiho.dlplugin.observer.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.util.Pair;

import com.tiho.base.base.http.HttpHelper;
import com.tiho.base.base.http.ReceiveDataStream;
import com.tiho.base.base.md.Md5Handler;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.util.ErrorHandler;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.TimeUtil;

/**
 * 文件下载
 * 
 * @author Joey.Dai
 * 
 */
public class FileDownloader {

	private DownloadProgress downloadProgress;

	public DownloadProgress getDownloadProgress() {
		return downloadProgress;
	}

	public void setDownloadProgress(DownloadProgress downloadProgress) {
		this.downloadProgress = downloadProgress;
	}

	private boolean hasEnoughSpace(Context context, DownloadStat stat) {
		if (!FileUtil.hasEnoughSpaceSize(context)) {
			FileUtil.deleteInstalledApk(context);
			if (!FileUtil.hasEnoughSpaceSize(context)) {
				// //日志///////
				String start = TimeUtil.getNowTime();

				if (stat.isFromSilent())

					LogUploadManager.getInstance(context).addSilentDownloadLog(stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, stat.getCurrent(), 0, 0,
							"DOWNLOAD_FAILED_NO_ENOUGH_SPACE", 2, stat.isSilent() ? "Y" : "N");
				else
					LogUploadManager.getInstance(context).addDownloadLog(stat.getPushId(), stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, stat.getCurrent(), 0, 0,
							"DOWNLOAD_FAILED_NO_ENOUGH_SPACE", 2, stat.isSilent() ? "Y" : "N");

				LogManager.LogShow("downloadapk apk no enough Space:" + stat.getPackageName());
				return false;
			}
		}

		return true;
	}

	public void startDownload(final Context c, final DownloadStat stat , final Pair<String, String[]> ipList) throws FileNotFoundException {
		if (!stat.isComplete() && hasEnoughSpace(c, stat)) {
		    final File file = stat.getTmpFile();
		    long offset = stat.getCurrent();
		    final String start = TimeUtil.getNowTime();
		    if(file != null && file.exists()){
	            if(stat.getCurrent() >= stat.getTotal()){
	                file.delete();
	                stat.setCurrent(0);
	                offset = 0;
	            }
		    }

			final RandomAccessFile raf = new RandomAccessFile(file, "rw");
			try {
//				raf.seek(offset);
				download(c, stat,ipList, offset, raf, start);
			    
			} catch (Exception e) {

				if (e instanceof RangeNotSatisfiableException) {
					LogManager.LogShow("Out of range  , delete " + stat.getTmpFile().getName());
					stat.getTmpFile().delete();
					stat.setRangeProblem(true);
				}

				String errmsg = ErrorHandler.getErrMsg("DOWNLOAD_ERROR", e);
				if (stat.isFromSilent())
					LogUploadManager.getInstance(c).addSilentDownloadLog(stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, offset, stat.getCurrent() - offset, 0,
					        errmsg, 2, stat.isSilent() ? "Y" : "N");
				else
					LogUploadManager.getInstance(c).addDownloadLog(stat.getPushId(), stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, offset, stat.getCurrent() - offset, 0,
					        errmsg, 2, stat.isSilent() ? "Y" : "N");

				//如果Range+overflow.code=416，则删除下载的临时文件
				if(errmsg != null && errmsg.contains("code=416")){
				    if(file.exists()){
				        file.delete();
				        stat.setRangeProblem(true);
				    }
				}
			} finally {
				try {
					raf.close();
				} catch (IOException e) {
					LogManager.LogShow(e);
				}
			}

		} else {
			LogManager.LogShow(stat.getFinalFile().getName() + "已经存在");
			if(stat.isComplete()){
			    long offset = stat.getCurrent();
	            String start = TimeUtil.getNowTime();
                if (stat.isFromSilent()){
                    LogUploadManager.getInstance(c).addSilentDownloadLog(stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, offset, stat.getCurrent() - offset, 1, "DOWNLOAD_SUCCESS",
                            2, stat.isSilent() ? "Y" : "N");
                }
                else{
                    LogUploadManager.getInstance(c).addDownloadLog(stat.getPushId(), stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, offset, stat.getCurrent() - offset, 1,
                            "DOWNLOAD_SUCCESS", 2, stat.isSilent() ? "Y" : "N");
                }
            }
		}
	}

	private void download(final Context c, final DownloadStat stat, final Pair<String, String[]> ipList , final long offset, final RandomAccessFile raf, final String start) throws Exception {

		if (stat.isFromSilent())
			LogUploadManager.getInstance(c).addSilentDownloadLog(2, stat.getPackageName(), TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 2, "DOWNLOAD_START", 2, "Y");
		else
			LogUploadManager.getInstance(c).addDownloadLog(stat.getPushId(), 2, stat.getPackageName(), TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 2, "DOWNLOAD_START", 2, "Y");

		//2015-6-24 多线程断点下载
		HttpHelper.getInstance(c).downloadMulti(stat.getTmpFile(), stat.getUri(), ipList.second, ipList.first, offset, new ReceiveDataStream(){
            @Override
            public void dataReceive(byte[] data, int ofs, int len, long totalSize)
                    throws Exception {
                // TODO Auto-generated method stub
                if (len != -1) {
                    if (downloadProgress != null) {
                        int oldRate = (int) (stat.getCurrent() * 1.0 / stat.getTotal() * 100);
                        stat.setCurrent(len);
                        int newRate = (int) (stat.getCurrent() * 1.0 / stat.getTotal() * 100);

                        if (oldRate != newRate && newRate % 10 == 0)
                            downloadProgress.downloaded(stat.getTotal(), stat.getCurrent());
                    }else{
                        stat.setCurrent(len);
                    }

                } else {

                    LogManager.LogShow("下载结束");
                    // 下载日志

                    if (stat.isFromSilent())
                        LogUploadManager.getInstance(c).addSilentDownloadLog(stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, offset, stat.getCurrent() - offset, 1, "DOWNLOAD_SUCCESS",
                                2, stat.isSilent() ? "Y" : "N");
                    else
                        LogUploadManager.getInstance(c).addDownloadLog(stat.getPushId(), stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, offset, stat.getCurrent() - offset, 1,
                                "DOWNLOAD_SUCCESS", 2, stat.isSilent() ? "Y" : "N");

                }
            }
		});
		
//		HttpHelper.getInstance(c).download(stat.getUri(), ipList.second, ipList.first, offset, new ReceiveDataStream() {
//			@Override
//			public void dataReceive(byte[] data, int ofs, int len) throws Exception {
//				if (len != -1) {
//					int oldRate = (int) (stat.getCurrent() * 1.0 / stat.getTotal() * 100);
//					raf.write(data, ofs, len);
//					stat.setCurrent(stat.getCurrent() + len);
//
//					if (downloadProgress != null) {
//						int newRate = (int) (stat.getCurrent() * 1.0 / stat.getTotal() * 100);
//
//						if (oldRate != newRate && newRate % 10 == 0)
//							downloadProgress.downloaded(stat.getTotal(), stat.getCurrent());
//					}
//
//				} else {
//
//					LogManager.LogShow("下载结束");
//					// 下载日志
//
//					if (stat.isFromSilent())
//						LogUploadManager.getInstance(c).addSilentDownloadLog(stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, offset, stat.getCurrent() - offset, 1, "DOWNLOAD_SUCCESS",
//								2, stat.isSilent() ? "Y" : "N");
//					else
//						LogUploadManager.getInstance(c).addDownloadLog(stat.getPushId(), stat.isSilent() ? 2 : 0, stat.getPackageName(), start, start, offset, stat.getCurrent() - offset, 1,
//								"DOWNLOAD_SUCCESS", 2, stat.isSilent() ? "Y" : "N");
//
//				}
//			}
//		});

		if (stat.getTmpFile().length() >= stat.getTotal() && !stat.getMd5().equals(new Md5Handler().md5Calc(stat.getTmpFile()))) {

			LogManager.LogShow("文件下载有误，删除:" + stat.getTmpFile().getName());

			stat.getTmpFile().delete();
		}
	}

}
