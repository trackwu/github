package com.tiho.dlplugin.net;

import android.text.TextUtils;


import com.tiho.base.common.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Http请求类
 * Created by Jerry on 2016/5/27.
 */
public abstract class HttpHelper {
    protected static final int BUFFER_LEN = 2048;
    protected static final String UTF_8 = "UTF-8";

    public final String simpleGet(String urlString) {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader bufferedReader = null;
        String content = null;
        HttpURLConnection conn = null;
        try {
            conn = buildDefaultConnection(urlString, "GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            LogManager.LogShow(String.format("GET url = %s code =%d", urlString,responseCode));
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if ((is = conn.getInputStream()) != null) {
                    if(isGzip(conn.getContentEncoding())){
                        is=new GZIPInputStream(is);
                    }
                    String charsetName = getCharsetName(conn);
                    isr = new InputStreamReader(is, charsetName);
                    bufferedReader = new BufferedReader(isr);
                    content = bufferedReader.readLine();
                }
            }
        } catch (IOException e) {
            if (e instanceof MalformedURLException) {
                LogManager.LogShow(String.format("GET url = %s MalformedURLException", urlString));
            } else if (e instanceof ProtocolException) {
                LogManager.LogShow(String.format("GET url = %s ProtocolException", urlString));
            }
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (is != null) {
                    is.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }


    public final String simplePost(String urlString, String body) {
        InputStream is = null;
        OutputStream os = null;
        InputStreamReader isr = null;
        BufferedReader bufferedReader = null;
        String content = null;
        HttpURLConnection conn = null;
        try {
            conn = buildDefaultConnection(urlString, "POST");
            conn.connect();
            conn.setDoOutput(true);
            os = conn.getOutputStream();
            if (!TextUtils.isEmpty(body)) {
                os.write(body.getBytes(UTF_8));
                os.flush();
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if ((is = conn.getInputStream()) != null) {
                    if(isGzip(conn.getContentEncoding())){
                        is=new GZIPInputStream(is);
                    }
                    String charsetName = getCharsetName(conn);
                    isr = new InputStreamReader(is, charsetName);
                    bufferedReader = new BufferedReader(isr);
                    content = bufferedReader.readLine();
                }
            }
        } catch (IOException e) {
            if (e instanceof MalformedURLException) {
                LogManager.LogShow(String.format("POST url = %s MalformedURLException", urlString));
            } else if (e instanceof ProtocolException) {
                LogManager.LogShow(String.format("POST url = %s ProtocolException", urlString));
            }
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (is != null) {
                    is.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }


    protected int getBytes(String urlString, long range, IDataReceiveCallBack callBack) {
        InputStream is = null;
        HttpURLConnection conn = null;
        String info =null;
        int result=IDataReceiveCallBack.FAILURE;
        try {
            conn = buildDefaultConnection(urlString, "GET");
            if (range > 0)
                conn.setRequestProperty("Range", "bytes=" + range + "-");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if ((responseCode == HttpURLConnection.HTTP_OK && range == 0)
                    || (responseCode == HttpURLConnection.HTTP_PARTIAL && range > 0)) {
                byte[] buffer = new byte[BUFFER_LEN];
                int len;
                int current = 0;
                int total = conn.getContentLength();
                if ((is = conn.getInputStream()) != null) {
                    if(isGzip(conn.getContentEncoding())){
                        is=new GZIPInputStream(is);
                    }
                    while ((len = is.read(buffer)) != -1) {
                        current += len;
                        callBack.onProgress(current, total, buffer, len);
                    }
                    result=IDataReceiveCallBack.SUCCESS;
                    info ="Success";
                } else {
                    info ="InputStream is null";
                }
            } else {
                info =String.format("ResponseCode is %d , Range is %d", responseCode, range);
            }
        } catch (IOException e) {
            if (e instanceof MalformedURLException) {
                LogManager.LogShow(String.format("GET url = %s MalformedURLException", urlString));
            } else if (e instanceof ProtocolException) {
                LogManager.LogShow(String.format("GET url = %s ProtocolException", urlString));
            }
            e.printStackTrace();
            info=e.getMessage();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(TextUtils.isEmpty(info)){
            LogManager.LogShow(String.format("url =%s ,info = %s",urlString,info));
        }
        return result;
    }


    protected final boolean isGzip(String contentEncoding) {
        return !TextUtils.isEmpty(contentEncoding) && contentEncoding.contains("gzip");
    }

    protected HttpURLConnection buildDefaultConnection(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        return conn;
    }


    /**
     * @return 返回默认传输编码 UTF-8
     */
    protected String getCharsetName(HttpURLConnection conn) {
        return UTF_8;
    }


    protected abstract void addHeader(HttpURLConnection conn);


    public interface IDataReceiveCallBack {
        public static final int SUCCESS = 1;
        public static final int FAILURE = -1;

        void onProgress(int current, int total, byte[] buffer, int len);
    }
}
