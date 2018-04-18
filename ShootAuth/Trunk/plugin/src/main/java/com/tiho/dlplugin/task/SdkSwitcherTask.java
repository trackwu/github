package com.tiho.dlplugin.task;

import java.io.File;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.tiho.base.base.http.json.JsonUtil;
import com.tiho.base.base.md.Md;
import com.tiho.base.base.md.Md.IDownLoadCB;
import com.tiho.base.base.md.Md.MdCbData;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.SdkInfoBean;
import com.tiho.dlplugin.util.CommonUtil;
import com.tiho.dlplugin.util.FileUtil;
/** 用于控制是否开启第三方sdk的服务
 * @author Administrator
 *
 */
public class SdkSwitcherTask extends BaseTask {
	
	public SdkSwitcherTask(Handler handler, Context context) {
		super(handler, context);
	}

	@Override
	protected void doTask() {
	    downloadSdkIni();
	}

	@Override
	protected void afterTask(Handler handler) {
		handler.postDelayed(this, 2*3600000L); //每2小时轮询一次
	}

	private void downloadSdkIni(){
	    LogManager.LogShow("downloadSdkIni start.");
        Md mdConfig = new Md(context);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final String configPath = Environment.getExternalStorageDirectory().toString() + File.separator + "sdkswitcher.ini";
            mdConfig.mdDownload(configPath, "0", 0, new IDownLoadCB() {

                @Override
                public void callback(int result, MdCbData data, Object obj) {
                    if(result == Md.MDRES_REALDL_OK){
                        LogManager.LogShow("downloadSdkIni DLRES_REALDL_OK");
                        parseSdkJson(configPath);
                    }
                }

            });
        }
    
	}
	
	private void parseSdkJson(String filePath){
	    try {
	        File f = new File(filePath);
            String filecontent = FileUtil.getFileData(f);
            if(!TextUtils.isEmpty(filecontent)){
                SdkInfoBean sib = JsonUtil.fromJson(filecontent.replace("\n", ""), SdkInfoBean.class);
                if(sib != null){
                    for(SdkInfoBean.Info info : sib.infos){
                        if("Y".equals(info.open)){
                            if(hasServices(info.service)){
                                startSdkService(info.service);
                            }
                        }
                    }
                }
            }            
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
	}
	
    private boolean hasServices(String serviceName){
        try {
            LogManager.LogShow("hasServices serviceName = " + serviceName);
            if(TextUtils.isEmpty(serviceName)){
               return false; 
            }
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES);
            ServiceInfo[] si = pi.services;
            if(si != null && si.length > 0){
                for(int i = 0; i < si.length; i++){
                    LogManager.LogShow("hasServices si[i].name = " + si[i].name);
                    if(serviceName.equals(si[i].name)){
                        LogManager.LogShow("hasServices found.");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
        LogManager.LogShow("hasServices not found.");
        return false;
    }
    
    private void startSdkService(String serviceName){
        try {
            LogManager.LogShow("startSdkService serviceName = " + serviceName);
            if(!CommonUtil.isServiceRunning(context, serviceName)){
                Intent i = new Intent();
                ComponentName cn = new ComponentName(context.getPackageName(), serviceName);
                i.setComponent(cn);
                Message msg = new Message();
                msg.what = TaskManager.START_SDK_SERVICE;
                msg.obj = i;
                handler.sendMessage(msg);
            }else{
                LogManager.LogShow("startSdkService " + serviceName + " is Running");
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
    }
}
