package com.tiho.dlplugin.observer.download;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;

/**
 * 下载列表
 * 
 * @author Joey.Dai
 * 
 */
public class DownloadList {

	private List<PushMessageBean> list = new LinkedList<PushMessageBean>();

	private Object lock = new Object();

	public DownloadList(List<PushMessageBean> init) {
		if (init != null) {

			_addAll(init);

		}
	}
	
	public DownloadList(){
		this(null);
	}

	private void _add(PushMessageBean msg) {
		synchronized (list) {
			if(!list.contains(msg))
				list.add(msg);
		}
	}

	private void _addAll(List<PushMessageBean> list) {
		synchronized (list) {
			list.addAll(list);
		}
	}

	private void _notify() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public void addToDownloadList(List<PushMessageBean> added) {
		_addAll(added);
		_notify();

	}

	public void addToDownloadList(PushMessageBean msg) {
		_add(msg);
		_notify();
	}
	
	public void sort(){
		Collections.sort(list, new Comparator<PushMessageBean>() {

			@Override
			public int compare(PushMessageBean lhs, PushMessageBean rhs) {
				return rhs.getPushId().intValue() - lhs.getPushId().intValue();
			}
		});
	}

	public PushMessageBean takeOne() throws InterruptedException {
		if (list.isEmpty())
			await();
		return list.get(0);
	}

	public void await() throws InterruptedException {
		synchronized (lock) {
			lock.wait();
		}
	}

	public void consumed(PushMessageBean msg) {
		synchronized (lock) {
			if (!list.isEmpty()) {
				boolean b = list.remove(msg);
				LogManager.LogShow("移除下载队列结果：" + b);
			}
		}
	}

}
