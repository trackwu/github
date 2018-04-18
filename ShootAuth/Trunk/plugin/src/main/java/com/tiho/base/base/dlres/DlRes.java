/**
 * 
 */
package com.tiho.base.base.dlres;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;

import com.tiho.base.base.HttpCommonUtil;
import com.tiho.base.base.http.ReceiveDataStream;
import com.tiho.base.base.md.Md5Handler;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.multiDownload.MultiDownloadTask;
import com.tiho.dlplugin.util.ErrorHandler;
import com.tiho.dlplugin.util.TimeUtil;

/**
 * @author seward
 *
 */
public class DlRes {
	
	public static final int DLRES_REALDL_FILEERR=0;
	public static final int DLRES_REALDL_FILELOCKED=1;
	public static final int DLRES_REALDL_PROGRESS=2;
	public static final int DLRES_REALDL_OK = 3;
	public static final int DLRES_REALDL_NETWORKERR = 4;
	private static final int REQUEST_TIMEOUT = 30 * 1000;
	private static final int SO_TIMEOUT = 30 * 1000;
	private static final String LOGTAG="DlRes";
	private String commonKey="";
	private String xPlayagent="";
	private String savepath="";
	private String mTempName = null;
	private String mMd5="";
	private String url="";
	private IDownLoadResCB cb;
	private boolean isStop = true;
	private Thread thread = null;
	private Context mContext = null;
	private boolean mIsMulti = false;
	        
	public interface IDownLoadResCB {
		public void callback(int result, HashMap<String, Object> data);
	}

	public DlRes(String url,String savename,String md5,IDownLoadResCB cb) {
		if(md5==null) md5 = "";
		LogManager.LogShow( "DlRes url = " + url);
		LogManager.LogShow( "DlRes savename = " + savename);
		LogManager.LogShow( "md5 savename = " + md5);
		LogManager.LogShow( "DlRes cb = " + cb);
		this.url = url;
		this.savepath = savename;
		this.mMd5 = md5;
		this.cb =cb;
	}
	
	public DlRes(String url, String savename, String md5, Context context, boolean isMulti, IDownLoadResCB cb) {
        if(md5==null) md5 = "";
        LogManager.LogShow( "DlRes url = " + url);
        LogManager.LogShow( "DlRes savename = " + savename);
        LogManager.LogShow( "md5 savename = " + md5);
        LogManager.LogShow( "DlRes cb = " + cb);
        this.url = url;
        this.savepath = savename;
        this.mMd5 = md5;
        this.cb =cb;
        this.mContext = context;
        this.mIsMulti = isMulti;
    }
	
	public void start() {
		if (thread == null) {
			thread = new Thread() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
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

	private String getSaveDir(String saveName){
		saveName = saveName.trim();

		int lastIndex = saveName.lastIndexOf("/");
		if(lastIndex == -1){
			lastIndex = saveName.lastIndexOf("\\");
		}
		String fileDir = saveName.substring(0,lastIndex+1);
		System.out.println("fileName = " + fileDir);

		return fileDir;
	}

	private long getFileSize(String saveName,String md5){
		String tmpSaveName = getSaveDir(saveName) + bytesToHexString(md5.getBytes());
		LogManager.LogShow("getFileSize tmpSaveName=" + tmpSaveName);
		File f = new File(tmpSaveName);
		if(f.exists()){
			return f.length();
		}
		return 0;
	}

	private void dl() {
		long range = 0;
		if (resIsExist()) {
			LogManager.LogShow( "isLocked");
			cb.callback(DLRES_REALDL_OK, null);
			return;
		}
		if(mMd5 != null && !mMd5.equals("")){
			range = getFileSize(savepath,mMd5);
		}
		if (isLocked()) {
			LogManager.LogShow( "isLocked");
			cb.callback(DLRES_REALDL_FILELOCKED , null);
			return;
		}
		LogManager.LogShow( "download res mMd5="+mMd5 + " range=" + range);
		if(range == 0){
			File tempFile = new File(getTempFileName());
			LogManager.LogShow( "download res");
			try {
				tempFile.delete();
				tempFile.getParentFile().mkdirs();
				tempFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				LogManager.LogShow( "isLocked");
				cb.callback(DLRES_REALDL_FILEERR, null);
				return;
			}
		}

		String cmd = "chmod 777  " + getTempFileName(); // 755
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(mIsMulti && mContext != null){
		    realDlMulti(range);
		}else{
		    realDl(range); 
		}
	}

	private boolean resIsExist() {
		String tmp = getFileName();
		File apktmp = new File(tmp);
		boolean resIExist = apktmp.exists();
		if(resIExist){
			if(mMd5 != null && !mMd5.equals("")){
				Md5Handler hash = new Md5Handler();
				String newMd5 = hash.md5Calc(apktmp);
				LogManager.LogShow("new md5="+newMd5+" old md5="+mMd5);
				if (!mMd5.equals(newMd5)) { // md5不合法
					apktmp.delete();
					resIExist = false;
				}
			}
		}
		return resIExist;
	}

	private boolean isLocked(){
		RandomAccessFile out;
		try {
			out = new RandomAccessFile(getTempFileName(), "rw");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}  
		
        FileChannel fcout=out.getChannel();
        FileLock flout=null;  
        try {  
            flout = fcout.tryLock();  
        } catch (Exception e) {  
        	e.printStackTrace(); 
        }  
        if(flout!=null){//获取到lock，说明没有被其他进程lock
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

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

	private String getTempFileName(){
		if(mTempName == null){
			if(mMd5 != null && !mMd5.equals("")){
				mTempName = getSaveDir(savepath) + bytesToHexString(mMd5.getBytes());
			}else{
				mTempName = savepath + ".tmp";
			}
		}
		LogManager.LogShow("getTmpName = " + mTempName);
		return mTempName;
	}
	private String getFileName(){
		return savepath;
	}
	
	private void realDl(long range) {
		if (HttpCommonUtil.getInstance() != null) {
			this.commonKey = HttpCommonUtil.getInstance().commonkey();
			this.xPlayagent = HttpCommonUtil.getInstance().xPlayAgent();
		}
		
		HttpGet getMethod = new HttpGet(url);
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
		FileChannel fcout=null;
		FileLock flout=null;
//		LogManager.LogShow( "------------myRetryHandler------------" + myRetryHandler);
//		httpClient.setHttpRequestRetryHandler(myRetryHandler);

		try {
			// postMethod.geta
			HttpResponse response = httpClient.execute(getMethod);
			LogManager.LogShow( Arrays.toString(getMethod.getAllHeaders()));
			LogManager.LogShow( "resCode = " + response.getStatusLine().getStatusCode()); // 获取响应码
			int code = response.getStatusLine().getStatusCode();
			if (code == 200 || code == 206) {
				if (response.getEntity() != null) {
					long totalSize = response.getEntity().getContentLength();
					LogManager.LogShow( "totalSize = " + totalSize);
					if (totalSize > 0) {
						if (response.getEntity().getContentEncoding() != null && response.getEntity().getContentEncoding().getValue().toLowerCase().indexOf("gzip") != -1) {
							input = new GZIPInputStream(response.getEntity().getContent());
						} else {
							input = response.getEntity().getContent();
						}
						byte data[] = new byte[1024*8];
						int bytesRead;
						long curSize = range;
						File file = new File(getTempFileName());
						LogManager.LogShow( "getTempFileName = " + getTempFileName());
						LogManager.LogShow( "getTempFileName exist0= " + file.exists());
//						saveit = new FileOutputStream(getTempFileName(), true);
						//对该文件加锁  
			            saveit = new RandomAccessFile(file, "rw"); 
						fcout = saveit.getChannel();
						flout = fcout.tryLock();
						while ((bytesRead = input.read(data)) != -1) {
							curSize += bytesRead;
							saveit.seek(saveit.length());
							saveit.write(data, 0, bytesRead);
							HashMap<String, Object>map = new HashMap<String, Object>();
							map.put("cursize", curSize);
							map.put("totalsize",(long)totalSize+range);
							LogManager.LogShow("cursize = " + curSize + ", totalsize = " + (totalSize+range));
							cb.callback(DLRES_REALDL_PROGRESS, map);
							if(isStop){
								throw new IOException();
							}
						}
						flout.release();
						fcout.close();
						saveit.close();
						input.close();
						File apktmp = new File(getTempFileName());
						if(mMd5 != null && !mMd5.equals("")){
							Md5Handler hash = new Md5Handler();
							String newMd5 = hash.md5Calc(apktmp);
							LogManager.LogShow("new md5="+newMd5+" old md5="+mMd5);
							if (!mMd5.equals(newMd5)) { // md5不合法
								apktmp.delete();
								cb.callback(DLRES_REALDL_FILEERR, null);
								return;
							}
						}
						File apkname = new File(getFileName());
						apktmp.renameTo(apkname);
						String cmd = "chmod 777  " + getFileName(); // 755
						// 644
						try {
							Runtime.getRuntime().exec(cmd);
						} catch (Exception e) {
							e.printStackTrace();
						}
						cb.callback(DLRES_REALDL_OK, null);
						return;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			int ret = DLRES_REALDL_NETWORKERR;
			e.printStackTrace();
			LogManager.LogShow( "------------IOException------------" + e.toString());
			LogManager.LogShow( e);
			 if(flout == null){  
                 System.out.println("有其他线程正在操作该文件");   
                 ret = DLRES_REALDL_FILEERR;
            }  
			LogManager.LogShow( "flout = " + flout);
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
			 if(isStop){
				 return;
			 }
			cb.callback(ret, null);
			
			return;
			
		}
		cb.callback(DLRES_REALDL_NETWORKERR, null);
	}
	
	private void realDlMulti(final long range) {
	    LogManager.LogShow("realDlMulti start. range = " + range);
        if (HttpCommonUtil.getInstance() != null) {
            this.commonKey = HttpCommonUtil.getInstance().commonkey();
            this.xPlayagent = HttpCommonUtil.getInstance().xPlayAgent();
        }
        
        File srcfile = new File(getTempFileName()); 
        final String start = TimeUtil.getNowTime();
        // 下载日志
        LogUploadManager.getInstance(mContext).uploadMdDownloadLog(start, range, 0, 2, "DOWNLOAD_START");
        MultiDownloadTask multi = new MultiDownloadTask();
        try {
            multi.download(mContext, srcfile, url, null, new AtomicLong(range), new ReceiveDataStream(){
                @Override
                public void dataReceive(byte[] data, int ofs, int len, long totalSize)
                        throws Exception {
                    if (len != -1) {
                        LogManager.LogShow("md multi ofs = " + ofs + ", len = " + len + ", totalSize = " + totalSize);
                    } else {
                        LogManager.LogShow("MD下载结束 ofs = " + ofs + ", len = " + len + ", totalSize = " + totalSize);
                        File apktmp = new File(getTempFileName());
                        if(mMd5 != null && !mMd5.equals("")){
                            Md5Handler hash = new Md5Handler();
                            String newMd5 = hash.md5Calc(apktmp);
                            LogManager.LogShow("new md5="+newMd5+" old md5="+mMd5);
                            if (!mMd5.equals(newMd5)) { // md5不合法
                                apktmp.delete();
                                LogUploadManager.getInstance(mContext).uploadMdDownloadLog(start, range, totalSize, 0, "DOWNLOAD_ERROR md5 is wrong");
                                cb.callback(DLRES_REALDL_FILEERR, null);
                                //如果下载失败使用原来的方式再下载一次
                                LogManager.LogShow("realDlMulti 失败，使用原来的方式再下载一次 md5 is wrong");
                                realDl(range);
                                return;
                            }
                        }
                        // 下载日志
                        LogUploadManager.getInstance(mContext).uploadMdDownloadLog(start, range, totalSize, 1, "DOWNLOAD_SUCCESS");
                        File apkname = new File(getFileName());
                        apktmp.renameTo(apkname);
                        String cmd = "chmod 777  " + getFileName(); // 755
                        // 644
                        try {
                            Runtime.getRuntime().exec(cmd);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        cb.callback(DLRES_REALDL_OK, null);
                    }
                }
            }, false, true, this.xPlayagent);
        } catch (Exception e) {

            String errmsg = ErrorHandler.getErrMsg("DOWNLOAD_ERROR", e);
            // 下载日志
            LogUploadManager.getInstance(mContext).uploadMdDownloadLog(start, range, 0, 0, errmsg);
            //如果Range+overflow.code=416，则删除下载的临时文件
            if(errmsg != null && errmsg.contains("code=416")){
                if(srcfile.exists()){
                    srcfile.delete();
                }
            }
            cb.callback(DLRES_REALDL_NETWORKERR, null);
            //如果下载失败使用原来的方式再下载一次
            LogManager.LogShow("realDlMulti 失败，使用原来的方式再下载一次." + e.getMessage());
            realDl(range);
        }
    }
}
