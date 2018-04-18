package com.tiho.dlplugin.task.silent.dao;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.bean.Resource;
import com.tiho.dlplugin.util.FileUtil;
import com.tiho.dlplugin.util.ObjectUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;

import android.content.Context;

public class IndepentSilnetDAOImpl implements IndepentSilnetDAO {
	

	private Context context;

	protected File getFile() {
		return PushDirectoryUtil.getFileInPushBaseDir(context, PushDirectoryUtil.BASE_DIR, "silence.dat");
	}

	private List<Resource> cache;

	public IndepentSilnetDAOImpl(Context context) {
		super();
		this.context = context;
	}
	

	@Override
	public List<Resource> getPushMessage() throws Exception {
		if (cache == null) {

			File f = getFile();

			if (!f.exists())
				return new LinkedList<Resource>();

			byte[] data = FileUtil.getFileRawData(f);

			f = null;

			cache = (List<Resource>) ObjectUtil.toObject(data);
		}

		return cache;
	}

	@Override
	public void savePushMessage(List<Resource> msgs) throws Exception {
		
		cache = msgs;
		File f = getFile();
		FileUtil.writeToFile(f, ObjectUtil.toByte(cache), true);

		f = null;
	}

	@Override
	public void deletePushMessage(long id) throws Exception {
		List<Resource> list = getPushMessage();
		if (list != null) {
			Iterator<Resource> it = list.iterator();
			while (it.hasNext()) {
				Resource bean = it.next();

				if (bean.getResourceId() ==id)
					it.remove();
			}

			File f = getFile();
			FileUtil.writeToFile(f, ObjectUtil.toByte(list), true);
			f = null;

			LogManager.LogShow(id + "已经删除");
		}
	}



	@Override
	public Resource queryByPack(String pack) throws Exception {
		List<Resource> list = getPushMessage();
		for (Resource resource : list) {
			if(resource.getResourceName().equals(pack)&&resource instanceof PushSilentBean){
				return resource;
			}
		}
		
		return null;
	}


	@Override
	public Resource queryByPackHash(int hash) throws Exception {
		
		List<Resource> list = getPushMessage();
		for (Resource resource : list) {
			if(resource.getResourceName().hashCode() == hash&&resource instanceof PushSilentBean)
				return resource;
		}
		
		return null;
	}


}
