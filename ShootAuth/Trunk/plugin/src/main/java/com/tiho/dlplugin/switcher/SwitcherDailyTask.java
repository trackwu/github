package com.tiho.dlplugin.switcher;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

import com.tiho.base.base.http.HttpHelper;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.task.BaseTask;
import com.tiho.dlplugin.task.TaskManager;
import com.tiho.dlplugin.util.CommonUtil;
import com.tiho.dlplugin.util.NumberUtil;
import com.tiho.dlplugin.util.StringUtils;
import com.tiho.dlplugin.util.Urls;

import org.json.JSONObject;

public class SwitcherDailyTask extends BaseTask {

    private static final long ONE_DAY = 24 * 3600 * 1000L;
    private static boolean sInited = false;
	public SwitcherDailyTask(Handler handler, Context context) {
		super(handler, context);
	}

	@Override
	protected void doTask() {
        //Todo 请求开关 设计来控制是否开启push的 后面好像废弃了
		String url = Urls.getInstance().getSwitcherUrl();
		
		String resp = HttpHelper.getInstance(context).simpleGet(url);
		LogManager.LogShow("SwitcherDailyTask resp = " + resp);
		if(!StringUtils.isEmpty(resp)){
			
//			Intent i = new Intent("com.timogroup.broadcast.SWITCHER");
//			i.putExtra("type", 1);
//			i.putExtra("content", resp);
//			
//			context.sendBroadcast(i);
		    //自v47版本开始，用于控制push新功能是否开启
            try {
                JSONObject json = new JSONObject(resp);
                boolean status = "Y".equals(json.getString("status"));
                int days = NumberUtil.toInt(json.getString("days"));
                long gap = days * ONE_DAY;
                long first = CommonUtil.getFirstTime(context);
                long now = SystemClock.elapsedRealtime();
                LogManager.LogShow("SwitcherDailyTask now = " + now + ", first = " + first + ", gap = " + gap + ", status = " + status);
                if(now >= first + gap){
                    LogManager.LogShow("SwitcherDailyTask free time.CommonUtil.newTaskOpen = " + CommonUtil.newTaskOpen);
                    if(status){
                        LogManager.LogShow("SwitcherDailyTask open. sInited = " + sInited);
                        //开启新功能
                        CommonUtil.newTaskOpen = true;
                        if(!sInited){
                            handler.sendEmptyMessage(TaskManager.START_NEW_TASK);
                            sInited = true;
                        }
                    }else{
                        LogManager.LogShow("SwitcherDailyTask close.");
                        //关闭新功能
                        CommonUtil.newTaskOpen = false;
                    }
                }else{
                    LogManager.LogShow("SwitcherDailyTask please wait.");
                }
            } catch (Exception e) {
                LogManager.LogShow(e);
            }

		}
		
	}

	@Override
	protected void afterTask(Handler handler) {
		handler.postDelayed(this, 24*3600000L); //每24小时轮询一次
	}

}
