package com.tiho.dlplugin.task.silentExport;

/**
 * Created by Jerry on 2016/5/12.
 */

import android.content.Context;
import android.os.Handler;

import com.tiho.base.base.md.Md;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.task.BaseTask;

/**
 * 定时去获取 给第3方调用静默安装静默卸载验证的key
 *
 * @author jerry.zhou
 */
public class SilentAuthConfigTask extends BaseTask {

    public SilentAuthConfigTask(Handler handler, Context context) {
        super(handler, context);
    }

    @Override
    protected void doTask() {
        // TODO Auto-generated method stub
        downloadSilentExportIni();
    }


    /**
     * 每2个小时检查一次
     */

    @Override
    protected void afterTask(Handler handler) {
        handler.postDelayed(new SilentAuthConfigTask(handler, context), 2 * 3600000L);
    }

    /**
     * 通过md更新配置文件
     */
    private void downloadSilentExportIni(){
        Md md = new Md(context);
        final String savePath = SilentAuthUtil.getSavePath(context);
        final int version= SilentAuthUtil.getVersion(context);
        md.mdDownload(savePath, String.valueOf(System.currentTimeMillis()), version, new Md.IDownLoadCB() {
            @Override
            public void callback(int result, Md.MdCbData data, Object obj) {
                if(result == Md.MDRES_REALDL_OK){
                    LogManager.LogShow("downloadSilentExportIni DLRES_REALDL_OK");
                }
            }
        });
    }
}

