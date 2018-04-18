package com.tiho.dlplugin.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushConfigBean;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.condition.ConditionManager;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.dao.PushDataSource;
import com.tiho.dlplugin.export.PushServiceImp;
import com.tiho.dlplugin.task.TaskRegister;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.Pair;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PushHelpImpl implements IPushHelper {

    protected Context c;

    private PushConfigDAO configDao;

    private PushNotifyManager manager;

    private PushDataSource dataSource;

    private PushStat stat;

    private boolean firstPush = true;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PushServiceImp.WHAT_START_PUSH) {
                // 开关为开
                PushMessageBean push = queryPushMessage();

                if (push != null) {

                    LogManager.LogShow("从数据源中获取到消息:" + push);

                    showPushMessage(push);

                    stat.setCursorId(push.getPushId());
                    firstPush = false;

                    registerNextPush();
                } else {
                    LogManager.LogShow("没有消息供显示");
                    registerNextPush();
                }
            }
        }
    };


    public PushHelpImpl(Context c) {
        super();
        this.c = c;
        // PushConfigFileDAOImpl
        this.configDao = DAOFactory.getConfigDAO(c);
        //Push通知栏管理器
        manager = PushNotifyManager.getInstance(c);
        //被观察者
        dataSource = PushDataSource.getInstance(c);

        stat = PushStat.getInstance(c);
    }

    @Override
    public final void startPush() {

        LogManager.LogShow("消息推送模块启动");
        // 注册下一次的推送时间
        registerNextPush();
    }

    private void registerNextPush() {
        try {
            long next = getNextPushTime(configDao.getConfigByKey("config", PushConfigBean.class));
            LogManager.LogShow("next push check in " + next / 60000 + " minutes.");
            TaskRegister.registerExactTask(c, next, new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    checkPushStat();
                }
            }, null, true);
        } catch (Exception e) {
            LogManager.LogShow("获取config配置失败", e);
        }
    }

    /**
     * 检查推送状态
     */
    protected void checkPushStat() {
        try {
            LogManager.LogShow("检查push状态");

            if (ConditionManager.inWorkTime(c)) {
                // 推送之前都要检查push开关状态
//				UpdateMethod.checkversion(c, handler);
                handler.sendEmptyMessage(PushServiceImp.WHAT_START_PUSH);
            } else {
                LogManager.LogShow("Push时间未到，不推送");
            }
        } catch (Exception e) {
            LogManager.LogShow(e);
        }
    }

    /**
     * 下一次的推送时间，距离现在的时间(单位：毫秒)
     *
     * @param config
     * @return
     */
    private long getNextPushTime(PushConfigBean config) {

        if (firstPush)
            return 60000L;

        Random rm = new Random();
        int minutes = rm.nextInt(config.getPushGap()) + config.getPushGap();

        return minutes * 60000L;
    }

    /**
     * 从数据源中查询合适的消息
     *
     * @return
     */
    protected PushMessageBean queryPushMessage() {
        PushMessageBean target = null;

        try {

            List<PushMessageBean> messages = dataSource.getPushMessage();

            if (messages != null) {

                Iterator<PushMessageBean> it = messages.iterator();
                boolean flush = false;

                int count = 0;
                while (it.hasNext()) {

                    PushMessageBean msg = it.next();
                    count++;

                    if (msg.isDone() || msg.getPushId() > stat.getCursorId()) {
                        LogManager.LogShow(msg.getPushId() + " is done or bigger than " + stat.getCursorId() + " , so we skip.isDone:" + msg.isDone());
                        continue;
                    } else if (timeExpired(msg) || (msg.getPushType() == PushMessageBean.TYPE_APP && PackageUtil.isInstalled(msg.getPackName(), c))) {
                        LogManager.LogShow(msg.getPushId() + "已经安装或者过期，不推送");
                        it.remove();
                        flush = true;
                    } else if (manager.overLimit(msg.getPushId()) || manager.isDisplaying(msg.getPushId())) {
                        continue;
                    }

                    // 如果处于最优时间段，就选这个
                    if (inBestPeriod(msg.getBestTimes())) {
                        target = msg;
                        break;
                    } else {
                        LogManager.LogShow(msg.getPushId() + "不处于最优时间段，跳过，选下一个");
                    }

                    // 如果查询到最后还是没有找到合适的消息，就把cursor移到头部，下次再来一遍
                    if (count == messages.size()) {
                        stat.setCursorId(stat.getMaxId());
                        LogManager.LogShow("Reset cursor to " + stat.getMaxId());
                    }
                }

                if (flush)
                    dataSource.flushCache();
            } else {
                LogManager.LogShow("本地没有push消息");
            }
        } catch (Exception e) {
            LogManager.LogShow("查询push消息失败", e);
        }
        return target;
    }


    //如果是push.jar，应用类型的push不通知
    //TODO 目前先这样，以后要改掉
//	private boolean filterForPushJar(PushMessageBean msg){
//		return EnvArgu.isFromJar() &&  msg.getPushType() == PushMessageBean.TYPE_APP ;
//	}

    /**
     * 是否超过有效期
     *
     * @param msg
     * @return
     */
    private boolean timeExpired(PushMessageBean msg) {
        Timestamp ex = msg.getExpireTime();

        boolean result = ex.getTime() < System.currentTimeMillis();

        LogManager.LogShow(msg.getPushId() + "是否已过期:" + result);

        return result;

    }

    /**
     * 是否处于最优时间段
     *
     * @param bestTimes
     * @return
     */
    private boolean inBestPeriod(List<Pair<Time, Time>> bestTimes) {

        long oneDay = 3600000L * 24;

        Time now = new Time(System.currentTimeMillis() % oneDay);

        for (Pair<Time, Time> pair : bestTimes) {
            if (now.getTime() >= pair.first.getTime() && now.getTime() <= pair.second.getTime())
                return true;
        }

        return false;
    }

    /**
     * 显示push消息，推送到通知栏
     *
     * @param c
     * @param msg
     */
    protected void showPushMessage(PushMessageBean msg) {
        try {
            manager.doNotify(msg);
        } catch (Exception e) {
            LogManager.LogShow("推送失败", e);
        }

    }

}
