package com.tiho.dlplugin.task.multiDownload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpException;
import org.apache.http.HttpStatus;

import android.content.Context;

import com.tiho.base.base.http.ReceiveDataStream;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.observer.download.RangeNotSatisfiableException;
import com.tiho.dlplugin.util.NetworkUtil;
import com.tiho.dlplugin.util.StringUtils;

/**
 * 
 * 
 */
public class MultiDownloadTask {

    // private static transient
    // 分段下载的线程个数
    private int threadNum = 5;
    private URL url = null;
    private long threadLength = 0;
    // 目标文件路径与名字
    public String fileDir = "";
    public String fileName = "";
    public String fileNameReal = "";
    public boolean statusError = false;
    private String charset;
    public long sleepSeconds = 5;
    private Context mContext;
    private long mCurrentSize = 0;
    private long mFileLength = 0;
    public File srcFile;
    public String host;
    public boolean addHost;
    public ReceiveDataStream receiveCallback;
    private boolean mIsMd = false;
    private String mXPlayagent = null;
    private long mTotalSize = 0;
    public void download(Context context, File srcfile, String urlStr, String host, AtomicLong offset, ReceiveDataStream callback, boolean addHost) throws Exception {
        if(NetworkUtil.isWifiOn(context)){
            this.threadNum = 5;
        }else{
            this.threadNum = 1;
        }
        statusError = false;
        long contentLength = 0;
        mContext = context;
        CountDownLatch latch = new CountDownLatch(threadNum);
        ChildThread[] childThreads = new ChildThread[threadNum];
        long[] startPos = new long[threadNum];
        long[] endPos = new long[threadNum];
        this.srcFile = srcfile;
        this.host = host;
        this.addHost = addHost;
        this.receiveCallback = callback;
//        try {
            this.fileDir = this.srcFile.getParent() + "/";//PushDirectoryUtil.getDir(mContext, PushDirectoryUtil.SILENT_DOWNLOAD_DIR).getAbsolutePath() + "/";
            this.fileNameReal = this.srcFile.getName();
            this.fileName = this.fileNameReal + ".tmp";
            if ("".equalsIgnoreCase(this.fileName)) {
                this.fileName = UUID.randomUUID().toString();
            }

            File file = new File(fileDir + fileName);
            File tempFile = new File(fileDir + fileName + "_temp");
            this.url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            setHeader(con);
            
            con.connect();
            int sc = con.getResponseCode();

            LogManager.LogShow("Multidownload sc = " + sc);
            if (sc == HttpStatus.SC_OK || sc == HttpStatus.SC_PARTIAL_CONTENT) {
                contentLength = con.getContentLength();
                LogManager.LogShow("Multidownload contentLength = " + contentLength);
                mTotalSize = contentLength;
                con.disconnect();
            }else if(sc == HttpStatus.SC_NOT_FOUND){
                throw new ConnectException("Not found:"+url);
            }else if (sc == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
                throw new RangeNotSatisfiableException("DOWNLOAD_ERROR:Range overflow.code=" + sc + " , offset=" + offset + ", url=" + url);
            } else if (sc == HttpStatus.SC_BAD_GATEWAY) {
                throw new ConnectException(url + " can not be accessed.");
            } else {
                throw new HttpException("DOWNLOAD_ERROR:code=" + sc + " , offset=" + offset + ", url=" + url);
            }
            
            try {
            // 得到content的长度
            setFileLength(contentLength);
            // 把context分为threadNum段的话，每段的长度。
            this.threadLength = contentLength / threadNum;

            // 第一步，分析已下载的临时文件，设置断点，如果是新的下载任务，则建立目标文件。
            setThreadBreakpoint(file, tempFile, contentLength, startPos, endPos);

            // 第二步，分多个线程下载文件
            ExecutorService exec = Executors.newCachedThreadPool();
            for (int i = 0; i < threadNum; i++) {

                // 开启子线程，并执行。
                ChildThread thread = new ChildThread(this, latch, i,
                        startPos[i], endPos[i]);
                childThreads[i] = thread;
                exec.execute(thread);
            }

                // 等待CountdownLatch信号为0，表示所有子线程都结束。
                latch.await();
                exec.shutdown();

                // 删除临时文件
                long downloadFileSize = file.length();
                if (downloadFileSize == contentLength) {
                    tempFile.delete();
                    file.renameTo(new File(this.fileDir + this.fileNameReal));
                }
                if(callback != null){
                    callback.dataReceive(null, 0, -1, mTotalSize);// 发一个-1表示结束
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
    
    public void download(Context context, File srcfile, String urlStr, String host, AtomicLong offset, ReceiveDataStream callback, boolean addHost, boolean isMd, String xPlayagent) throws Exception {
        this.mIsMd = isMd;
        this.mXPlayagent = xPlayagent;
        download(context, srcfile, urlStr, host, offset, callback, addHost);
    }
    
    public String download(Context context, String urlStr, String charset, File srcfile) {
        statusError = false;
        this.charset = charset;
        long contentLength = 0;
        mContext = context;
        CountDownLatch latch = new CountDownLatch(threadNum);
        ChildThread[] childThreads = new ChildThread[threadNum];
        long[] startPos = new long[threadNum];
        long[] endPos = new long[threadNum];
        this.srcFile = srcfile;
        
        try {
            this.fileDir = this.srcFile.getParent() + "/";//PushDirectoryUtil.getDir(mContext, PushDirectoryUtil.SILENT_DOWNLOAD_DIR).getAbsolutePath() + "/";
            // 从url中获得下载的文件格式与名字
//            this.fileNameReal = urlStr.substring(urlStr.lastIndexOf("/") + 1,
//                    urlStr.lastIndexOf("?") > 0 ? urlStr.lastIndexOf("?")
//                            : urlStr.length());
            this.fileNameReal = this.srcFile.getName();
            this.fileName = this.fileNameReal + ".tmp";
            if ("".equalsIgnoreCase(this.fileName)) {
                this.fileName = UUID.randomUUID().toString();
            }

            File file = new File(fileDir + fileName);
            File tempFile = new File(fileDir + fileName + "_temp");
            this.url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            setHeader(con);
            // 得到content的长度
            contentLength = con.getContentLength();
            setFileLength(contentLength);
            // 把context分为threadNum段的话，每段的长度。
            this.threadLength = contentLength / threadNum;

            // 第一步，分析已下载的临时文件，设置断点，如果是新的下载任务，则建立目标文件。
            setThreadBreakpoint(file, tempFile, contentLength, startPos, endPos);

            // 第二步，分多个线程下载文件
            ExecutorService exec = Executors.newCachedThreadPool();
            for (int i = 0; i < threadNum; i++) {

                // 开启子线程，并执行。
                ChildThread thread = new ChildThread(this, latch, i,
                        startPos[i], endPos[i]);
                childThreads[i] = thread;
                exec.execute(thread);
            }

            try {
                // 等待CountdownLatch信号为0，表示所有子线程都结束。
                latch.await();
                exec.shutdown();

                // 删除临时文件
                long downloadFileSize = file.length();
                if (downloadFileSize == contentLength) {
                    tempFile.delete();
                    file.renameTo(new File(this.fileDir + this.fileNameReal));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileDir + fileName;
    }

    private void setThreadBreakpoint(File file, File tempFile,
            long contentLength, long[] startPos, long[] endPos) {
        RandomAccessFile tempFileFos = null;
        try {
            if (file.exists()) {
                System.out.println("file " + fileName + " has exists!");

                long localFileSize = file.length();
                // 下载的目标文件已存在，判断目标文件是否完整
                if (localFileSize <= contentLength) {
                    System.out.println("Now download continue ... ");

                    tempFileFos = new RandomAccessFile(tempFile, "rw");
                    // 遍历目标文件的所有临时文件，设置断点的位置，即每个临时文件的长度
                    long realLength = 0;
                    for (int i = 0; i < threadNum; i++) {
                        tempFileFos.seek(4 + 24 * i + 8);
                        endPos[i] = tempFileFos.readLong();

                        tempFileFos.seek(4 + 24 * i + 16);
                        startPos[i] = tempFileFos.readLong();
                        if(i > 0){
                            realLength += (startPos[i] - endPos[i-1] - 1);
                        }else{
                            realLength = startPos[i];
                        }
                    }
                    setCurrentFileSize(realLength, -1);
                    if(realLength == contentLength){
                        LogManager.LogShow("setThreadBreakpoint realLength == contentLength " + contentLength);
                    }
                } else {
                    System.out.println("This file has download complete!");
                }

            } else {
                // 如果下载的目标文件不存在，则创建新文件
                file.createNewFile();
                tempFile.createNewFile();
                tempFileFos = new RandomAccessFile(tempFile, "rw");
                tempFileFos.writeInt(threadNum);

                for (int i = 0; i < threadNum; i++) {

                    // 创建子线程来负责下载数据，每段数据的起始位置为(threadLength * i)
                    startPos[i] = threadLength * i;
                    tempFileFos.writeLong(startPos[i]);

                    /*
                     * 设置子线程的终止位置，非最后一个线程即为(threadLength * (i + 1) - 1)
                     * 最后一个线程的终止位置即为下载内容的长度
                     */
                    if (i == threadNum - 1) {
                        endPos[i] = contentLength;
                    } else {
                        endPos[i] = threadLength * (i + 1) - 1;
                    }
                    // end position
                    tempFileFos.writeLong(endPos[i]);
                    // current position
                    tempFileFos.writeLong(startPos[i]);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                tempFileFos.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * 
     * @author annegu
     * @since 2009-07-16
     * 
     */
    public class ChildThread extends Thread {
        private MultiDownloadTask task;
        private int id;
        private long startPosition;
        private long endPosition;
        private final CountDownLatch latch;
        private RandomAccessFile file = null;
        private RandomAccessFile tempFile = null;

        public ChildThread(MultiDownloadTask task, CountDownLatch latch, int id,
                long startPos, long endPos) {
            super();
            this.task = task;
            this.id = id;
            this.startPosition = startPos;
            this.endPosition = endPos;
            this.latch = latch;

            try {
                file = new RandomAccessFile(this.task.fileDir
                        + this.task.fileName, "rw");
                tempFile = new RandomAccessFile(this.task.fileDir
                        + this.task.fileName + "_temp", "rw");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            System.out.println("Thread " + id + " run ...");
            HttpURLConnection con = null;
            InputStream inputStream = null;
            long count = 0;

            try {
                System.out.println(id + "===1 ====" + tempFile.readInt());
                tempFile.seek(4 + 24 * id);
                System.out.println(id + "===2 ====" + tempFile.readLong());
                System.out.println(id + "===3 ====" + tempFile.readLong());
                System.out.println(id + "===4 ====" + tempFile.readLong());
            } catch (IOException e2) {
                e2.printStackTrace();
            }

            for (;;) {
                try {
                    // 打开URLConnection
                    con = (HttpURLConnection) task.url.openConnection();
                    setHeader(con);

                    if (startPosition < endPosition) {
                        // 设置下载数据的起止区间
                        con.setRequestProperty("Range", "bytes="
                                + startPosition + "-" + endPosition);
                        System.out.println("Thread " + id
                                + " startPosition is " + startPosition
                                + ", and endPosition is " + endPosition);

                        file.seek(startPosition);

                        con.connect();
                        // 判断http status是否为HTTP/1.1 206 Partial Content或者200 OK
                        // 如果不是以上两种状态，把status改为STATUS_HTTPSTATUS_ERROR
                        if (con.getResponseCode() != HttpURLConnection.HTTP_OK
                                && con.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                            System.out.println("Thread " + id + ": code = "
                                    + con.getResponseCode() + ", status = "
                                    + con.getResponseMessage());
                            this.task.statusError = true;
                            file.close();
                            con.disconnect();
                            System.out.println("Thread " + id + " finished.");
                            latch.countDown();
                            break;
                        }

                        inputStream = con.getInputStream();
                        int len = 0;
                        byte[] b = new byte[10240];
                        while (!this.task.statusError
                                && (len = inputStream.read(b)) != -1) {
                            file.write(b, 0, len);

                            count += len;
                            startPosition += len;

                            // set tempFile now position
                            tempFile.seek(4 + 24 * id + 16);
                            tempFile.writeLong(startPosition);
                            setCurrentFileSize(getCurrentFileSize(id) + len, id);
                        }

                        file.close();
                        tempFile.close();
                        inputStream.close();
                        con.disconnect();
                    }

                    System.out.println("Thread " + id + " finished.");
                    latch.countDown();
                    break;
                } catch (IOException e) {
                    try {
                        // outputStream.flush();
                        TimeUnit.SECONDS.sleep(getSleepSeconds());
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }
            }
        }
    }

    private void setHeader(HttpURLConnection con) {
        try {
            if(!mIsMd){
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
                con.setRequestProperty("Connection", "keep-alive");
                if (!StringUtils.isEmpty(host) && addHost){
                    con.setRequestProperty("Host", host);
                }else{
                    con.setRequestProperty("Host", "static.uiandroid.net");
                }
                con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                con.setRequestProperty("Accept-Encoding", "gzip, deflate");
                con.setRequestProperty("User-Agent", "playPush");
                con.setConnectTimeout(60000);
                con.setReadTimeout(60000);
            }else{
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/octet-stream");
                con.setRequestProperty("X-play-agent", this.mXPlayagent);
                con.setRequestProperty("Accept", "*/*");
                con.setRequestProperty("Accept-Encoding", "gzip, deflate");
                con.setRequestProperty("Connection", "Keep-Alive");
                con.setRequestProperty("User-Agent", "Playbase");
                con.setConnectTimeout(60000);
                con.setReadTimeout(60000);
            }
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public long getSleepSeconds() {
        return sleepSeconds;
    }

    public void setSleepSeconds(long sleepSeconds) {
        this.sleepSeconds = sleepSeconds;
    }

    public void setCurrentFileSize(long size, int id){
        mCurrentSize = size;
        if(this.receiveCallback != null){
            try {
                this.receiveCallback.dataReceive(null, 0, (int)size, mTotalSize);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
//        LogManager.LogShow("setCurrentFileSize mCurrentSize = " + mCurrentSize + ",  mFileLength = " + mFileLength + ", file = " + this.srcFile.getName() + ", id = " + id);
    }
    
    public long getCurrentFileSize(int id){
//        LogManager.LogShow("getCurrentFileSize mCurrentSize = " + mCurrentSize + ", id = " + id);
        return mCurrentSize;
    }
    
    public void setFileLength(long length){
        mFileLength = length;
        LogManager.LogShow("setFileLength mFileLength = " + mFileLength);
    }
    
    public long getFileLength(){
        return mFileLength;
    }
}
