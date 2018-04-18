package com.tiho.dlplugin.task.scheduleact;

import android.content.Context;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.ScheduleActivationBean;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.ScheduleActivationDAO;
import com.tiho.dlplugin.util.PackageUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ScheduleActList {

	private static ScheduleActList instance;

	public static synchronized ScheduleActList getInstance(Context c) {
		if (instance == null)
			instance = new ScheduleActList(c);

		return instance;
	}

	private Context context;
	private List<ScheduleActivationBean> todayList;

	private List<String> todayDone;// 今天已经激活过的列表

	private ScheduleActList(Context context) {
		super();
		this.context = context;
		todayList = new LinkedList<ScheduleActivationBean>();
		todayDone = new LinkedList<String>();
		reset();
	}

	public void resetTodayDone() {
		todayDone.clear();
	}

	public void doneOne(String pack) {
		todayDone.add(pack);
	}

	/**
	 * 重置列表，每天凌晨重置
	 */
	public void reset() {

		LogManager.LogShow("重置激活列表");

		ScheduleActivationDAO silentDao = DAOFactory.getScheduleActSilentDAO(context);
		//删除"schedule_act_silent"表中今天之前的激活列表,并返回今天的激活列表集合
		List<ScheduleActivationBean> repo = silentDao.getTodayTask();

		Random r = new Random();

		todayList.clear();

		addToList(repo, r);

		ScheduleActivationDAO rgDao = DAOFactory.getScheduleActRegularDAO(context);
		//删除"schedule_act"表中今天之前的激活列表,并返回今天的激活列表集合
		List<ScheduleActivationBean> ro = rgDao.getTodayTask();

		addToList(ro, r);

		Collections.shuffle(todayList);

		if (LogManager.debugOpen) {

			LogManager.LogShow("重置结果:");
			for (ScheduleActivationBean b : todayList) {
				LogManager.LogShow(b.toString());
			}
		}
	}

	private void addToList(List<ScheduleActivationBean> repo, Random r) {
		for (ScheduleActivationBean scheduleActivationBean : repo) {
			int rate = r.nextInt(101);

			LogManager.LogShow("pack:" + scheduleActivationBean.getPackName() + ",random:" + rate + " ," + scheduleActivationBean.getRate());
			//加入激活列表的条件是应用已经安装，并且在概率范围内
			if (rate <= scheduleActivationBean.getRate() && PackageUtil.isInstalled(scheduleActivationBean.getPackName(), context)) {

				tryReplaceWithSilent(scheduleActivationBean);

			}
		}
	}

	private void tryReplaceWithSilent(ScheduleActivationBean b) {

		Iterator<ScheduleActivationBean> it = todayList.iterator();
		ScheduleActivationBean old = null;
		while (it.hasNext()) {
			ScheduleActivationBean r = it.next();
			if (r.getPackName().equals(b.getPackName())) {
				old = r;
				break;
			}
		}

		if (old == null) {
			todayList.add(b);
			LogManager.LogShow(b + " 加入到激活列表中");
		} else {
			if (!old.isFromSilent()) {
				todayList.remove(old);
				todayList.add(b);
				LogManager.LogShow(b + " 加入到激活列表中");
			}
		}

	}

	public void load(String pack) {
		ScheduleActivationDAO stDao = DAOFactory.getScheduleActSilentDAO(context);
		ScheduleActivationBean stBean = stDao.get(pack);

		ScheduleActivationDAO regDao = DAOFactory.getScheduleActRegularDAO(context);
		ScheduleActivationBean regBean = regDao.get(pack);

		// 优先用静默的
		ScheduleActivationBean bean = stBean != null ? stBean : regBean;

		if (bean != null) {

			Random rm = new Random();
			int rate = rm.nextInt(101);

			if (rate <= bean.getRate() && PackageUtil.isInstalled(bean.getPackName(), context)) {
				todayList.add(bean);

				LogManager.LogShow(bean + " 加入到激活列表中");

				Collections.shuffle(todayList);
			}
		}
	}

	/**
	 * 找个命中的
	 * 
	 * @param count
	 * @return
	 */
	public ScheduleActivationBean isHit(int count) {

		for (ScheduleActivationBean scheduleActivationBean : todayList) {

			boolean installed = PackageUtil.isInstalled(scheduleActivationBean.getPackName(), context);
			boolean counted = scheduleActivationBean.getCount() <= count;
			boolean done = todayDone.contains(scheduleActivationBean.getPackName());

			LogManager.LogShow(scheduleActivationBean + "安装状态:" + installed + ",次数:" + counted + ",已激活:" + done);

			// 应用已安装并且满足数量,而且今天还没激活过
			if (installed && counted && !done) {
				return scheduleActivationBean;
			}
		}

		return null;
	}

	public boolean hasMore() {
		return !todayList.isEmpty();
	}

	public void removeOne(String pack) {
		Iterator<ScheduleActivationBean> it = todayList.iterator();
		while (it.hasNext()) {
			ScheduleActivationBean b = it.next();
			if (b.getPackName().equals(pack))
				it.remove();
		}
	}

	public void removeOne(ScheduleActivationBean bean) {
		todayList.remove(bean);
	}

}
