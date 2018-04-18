package com.tiho.dlplugin.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.update.UpdateMethod;
import com.tiho.dlplugin.util.BroadCastUtil;
import com.tiho.dlplugin.util.NetworkUtil;

/**
 * 2小时执行一次自更新任务
 * @author Administrator
 *
 */
public class UpdateTask extends BaseTask {
	private boolean mUpdateStart = true;
    
	public UpdateTask(Handler handler, Context context) {
		super(handler, context);
		registerNetWorkReceiver();
	}

	@Override
	protected void doTask() {
	    try {
	        LogManager.LogShow("UpateTask start. context = " + context);
	        if(NetworkUtil.isNetworkOk(context)){
	            mUpdateStart = true;
	            new UpdateMethod().checkversion(context, handler);
	        }else{
	            mUpdateStart = false;
	        }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
		
	}

	@Override
	protected void afterTask(Handler handler) {
		handler.postDelayed(this, 2*3600000L); //每2小时轮询一次
	}

	private void registerNetWorkReceiver(){
	    BroadCastUtil.registerReceiverEvent(context, ConnectivityManager.CONNECTIVITY_ACTION, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogManager.LogShow("UpdateTask action = " + intent.getAction() + ", mUpdateStart = " + mUpdateStart);
                if(!mUpdateStart && NetworkUtil.isNetworkOk(context)){
                    LogManager.LogShow("UpateTask Receiver doTask.");
                    doTask();
                }
            }
        });
	}
}
