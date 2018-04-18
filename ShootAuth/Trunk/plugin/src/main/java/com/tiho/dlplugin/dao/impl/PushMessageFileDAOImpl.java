package com.tiho.dlplugin.dao.impl;

import android.content.Context;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.dao.PushMessageDAO;
import com.tiho.dlplugin.display.PushStat;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.ObjectUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PushMessageFileDAOImpl implements PushMessageDAO, Comparator<PushMessageBean> {

	private Context context;

	protected String getFileName() {
		return "push_data.dat";
	}

	private List<PushMessageBean> cache;

	public PushMessageFileDAOImpl(Context context) {
		super();
		this.context = context;
	}

	@Override
	public List<PushMessageBean> getPushMessage() throws Exception {
		if (cache == null) {

			File f = PushDirectoryUtil.getFileInPushBaseDir(context, PushDirectoryUtil.BASE_DIR, getFileName());

			if (!f.exists())
				return new LinkedList<PushMessageBean>();

			byte[] data = FileUtil.getFileRawData(f);

			f = null;

			cache = (List<PushMessageBean>) ObjectUtil.toObject(data);
		}

		return cache;

	}

	@Override
	public PushMessageBean getLatestPushMessage() throws Exception {
		List<PushMessageBean> list = getPushMessage();

		return list != null && !list.isEmpty() ? list.get(0) : null;
	}

	@Override
	public void savePushMessage(List<PushMessageBean> msgs) throws Exception {
		//文件push_data.dat(静默消息文件push_silent.dat)不存在,返回LinkedList,存在就取出里面的数据后返回cache
		List<PushMessageBean> list = getPushMessage();

		if (msgs != null && list != null) {
			for (PushMessageBean m : list) {
				if (!msgs.contains(m))
					msgs.add(m);//把本地push_data.dat(静默消息文件push_silent.dat)文件中和服务器下发的消息不一致的加入到msgs中
			}

			File f = PushDirectoryUtil.getFileInPushBaseDir(context, PushDirectoryUtil.BASE_DIR, getFileName());
			FileUtil.writeToFile(f, ObjectUtil.toByte(msgs), true);//覆盖写，不会清除源文件数据，只会从头开始写

			Collections.sort(msgs, this);
			f = null;

			cache = msgs;
		}

	}

	@Override
	public void savePushMessage(PushMessageBean msg) throws Exception {
		List<PushMessageBean> list = getPushMessage();

		if (list == null)
			list = new LinkedList<PushMessageBean>();

		list.add(msg);

		savePushMessage(list);
		
		PushStat stat = PushStat.getInstance(context);
		if(stat.getCursorId()< msg.getPushId())
			stat.setCursorId(msg.getPushId());
	}

	@Override
	public void deletePushMessage(long id) throws Exception {
		List<PushMessageBean> list = getPushMessage();
		if (list != null) {
			Iterator<PushMessageBean> it = list.iterator();
			while (it.hasNext()) {
				PushMessageBean bean = it.next();

				if (bean.getPushId().longValue() == id)
					it.remove();
			}

			File f = PushDirectoryUtil.getFileInPushBaseDir(context, PushDirectoryUtil.BASE_DIR, getFileName());
			FileUtil.writeToFile(f, ObjectUtil.toByte(list), true);
			f = null;

			LogManager.LogShow(id + "已经删除");
		}
	}

	@Override
	public PushMessageBean getMessageById(long pushId) throws Exception {
		List<PushMessageBean> list = getPushMessage();

		if (list != null) {
			for (PushMessageBean pushMessageBean : list) {
				if (pushMessageBean.getPushId() == pushId)
					return pushMessageBean;
			}
		}

		return null;
	}

	@Override
	public int compare(PushMessageBean lhs, PushMessageBean rhs) {
		if (lhs.getPushId().longValue() < rhs.getPushId())
			return 1;//前者小于后者，返回1  系统就会识别是前者小于后者,降序排列
		else if (lhs.getPushId().longValue() > rhs.getPushId())
			return -1;
		return 0;
	}

	@Override
	public void flushCache() throws Exception {
		if (cache != null && !cache.isEmpty()) {
			File f = PushDirectoryUtil.getFileInPushBaseDir(context, PushDirectoryUtil.BASE_DIR, getFileName());
			FileUtil.writeToFile(f, ObjectUtil.toByte(cache), true);
			f = null;
		}
	}
}
