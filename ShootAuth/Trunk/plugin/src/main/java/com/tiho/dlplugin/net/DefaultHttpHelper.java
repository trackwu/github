package com.tiho.dlplugin.net;


import java.net.HttpURLConnection;

/**
 * Created by Jerry on 2016/8/8.
 */
public class DefaultHttpHelper extends HttpHelper{

    private static DefaultHttpHelper instance;
    public DefaultHttpHelper() {

    }

    public synchronized static DefaultHttpHelper getInstance() {
        if(instance==null){
            instance=new DefaultHttpHelper();
        }
        return instance;
    }
    @Override
    protected void addHeader(HttpURLConnection conn) {
        
    }
}
