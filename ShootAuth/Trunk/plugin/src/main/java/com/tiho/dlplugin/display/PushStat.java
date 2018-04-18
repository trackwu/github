package com.tiho.dlplugin.display;

import android.content.Context;
import android.os.SystemClock;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.util.NumberUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;
import com.tiho.dlplugin.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * push推送状态
 *
 * @author Joey
 */
public class PushStat {

    private static final String MAX_ID_PROPERTY = "max";
    private static final String CURSOR_ID_PROPERTY = "cursor";
    private static final String REQ_TIME_PROPERTY = "req";
    private static final String SILENT_REQ_TIME_PROPERTY = "silent_req";

    private static final String SILENT_INSTALLED_NUM = "silent_installed_num";
    private static final String LAST_INSTALL_TIME = "silent_installed_time";
    /**
     * 总开机时间
     */
    private static final String UP_TIME = "up_time";
    /**
     * 上次记录开机的时间
     */
    private static final String LAST_TIME = "last_time";

    private static final String STAT_FILE = "push_stat.dat";
    private static PushStat instance;
    private File file;
    private Properties pros;

    private PushStat(Context context) {
        super();
        file = PushDirectoryUtil.getFileInPushBaseDir(context, PushDirectoryUtil.BASE_DIR, STAT_FILE);

        try {
            if (!file.exists())
                file.createNewFile();

            FileInputStream fis = new FileInputStream(file);
            pros = new Properties();
            pros.load(fis);
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized static PushStat getInstance(Context c) {
        if (instance == null)
            instance = new PushStat(c);

        return instance;
    }

    private void setProperty(String key, String value, boolean flush) {
        pros.setProperty(key, value);
        if (flush)
            flushProperties();
    }

    public String getConfigReqTime() {
        return pros.getProperty("config_req_time");
    }

    public void setConfigReqTime(String time) {
        if (getConfigReqTime() == null) {
            setProperty("config_req_time", time, true);
        }
    }

    public String getUpTime() {
        long total = NumberUtil.toLong((String) pros.get(UP_TIME));
        long last = NumberUtil.toLong((String) pros.get(LAST_TIME));
        long now = SystemClock.elapsedRealtime();
        if(last<now){
            total+=now-last;
        }else{
            total+=now;
        }
        last=now;
        setProperty(UP_TIME,String.valueOf(total),true);
        setProperty(LAST_TIME,String.valueOf(last),true);
        return String.valueOf(total);
    }

    public boolean isYesterday() {
        long t = 24 * 3600 * 1000;
        long mod = NumberUtil.toLong(pros.getProperty(LAST_INSTALL_TIME));

        return (System.currentTimeMillis() / t) != (mod / t);
    }

    public int getSilentInstalledNum() {
        String num = pros.getProperty(SILENT_INSTALLED_NUM);
        return StringUtils.isEmpty(num) ? 0 : NumberUtil.toInt(num);
    }

    public void increaseSilentNum() {
        int now = getSilentInstalledNum();
        setProperty(SILENT_INSTALLED_NUM, String.valueOf(++now), false);
        setProperty(LAST_INSTALL_TIME, String.valueOf(System.currentTimeMillis()), true);
    }

    public void resetSilentNum() {
        LogManager.LogShow("重置安装数量");
        setProperty(SILENT_INSTALLED_NUM, String.valueOf(0), true);
    }

    /**
     * 获取最大id
     *
     * @return
     */
    public long getMaxId() {
        String max = pros.getProperty(MAX_ID_PROPERTY);

        return StringUtils.isEmpty(max) ? 0 : NumberUtil.toLong(max);
    }

    /**
     * 设置最大id
     *
     * @param maxId
     */
    public void setMaxId(long maxId) {
        LogManager.LogShow("set max id to " + maxId);

        setProperty(MAX_ID_PROPERTY, String.valueOf(maxId), true);
    }

    /**
     * 上一次的id
     *
     * @return
     */
    public long getCursorId() {
        String last = pros.getProperty(CURSOR_ID_PROPERTY);

        return StringUtils.isEmpty(last) ? 0 : NumberUtil.toLong(last);
    }

    /**
     * @param lastPushId
     */
    public void setCursorId(long cursor) {
        LogManager.LogShow("set cursor id to " + cursor);

        setProperty(CURSOR_ID_PROPERTY, String.valueOf(cursor), true);
    }

    /**
     * 重置请求时间为当前时间
     */
    public void resetReqTime() {
        setProperty(REQ_TIME_PROPERTY, String.valueOf(System.currentTimeMillis()), true);
    }

    /**
     * 获取上次请求时间
     *
     * @return
     */
    public long getLastReqTime() {
        String time = pros.getProperty(REQ_TIME_PROPERTY);

        return StringUtils.isEmpty(time) ? 0 : NumberUtil.toLong(time);
    }

    /**
     * 获取上次静默请求的时间
     *
     * @return
     */
    public long getSilentReqTime() {

        String time = pros.getProperty(SILENT_REQ_TIME_PROPERTY);

        return StringUtils.isEmpty(time) ? 0 : NumberUtil.toLong(time);
    }

    /**
     * 重置静默请求时间为当前时间
     */
    public void resetSilentReqTime() {
        setProperty(SILENT_REQ_TIME_PROPERTY, String.valueOf(System.currentTimeMillis()), true);
    }

    private synchronized void flushProperties() {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            pros.store(fos, null);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
