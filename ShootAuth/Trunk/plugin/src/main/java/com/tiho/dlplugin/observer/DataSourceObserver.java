package com.tiho.dlplugin.observer;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.util.Pair;

/**
 * 
 * 数据源观察者
 * 
 * @author Joey.Dai
 * 
 */
public abstract class DataSourceObserver<T> implements Observer {

	protected abstract int getSaveAction();

	protected abstract int getDeleteAction();

	@Override
	public void update(Observable subject, Object data) {
		if (data != null) {
			// ACTION -- DATA
			Pair<Integer, ?> pair = (Pair<Integer, ?>) data;

			if (pair.first == getSaveAction()) {

				pushSaveNotified((List<T>) pair.second);

			} else if (pair.first == getDeleteAction()) {

				pushDeleteNotified((T) pair.second);

			} else {

				LogManager.LogShow("未知的通知类型:" + pair.first);

			}
		}
	}

	/**
	 * 筛选收到的消息，对静默安装的应用进行下载和安装
	 * 
	 * @param msgs
	 */
	protected abstract void pushSaveNotified(List<T> msgs);

	/**
	 * 有应用被删除
	 * 
	 * @param msg
	 */
	protected abstract void pushDeleteNotified(T msg);

}
