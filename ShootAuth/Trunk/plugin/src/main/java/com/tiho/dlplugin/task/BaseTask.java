package com.tiho.dlplugin.task;

import android.content.Context;
import android.os.Handler;

public abstract class BaseTask implements Runnable{

	protected Handler handler;
	protected Context context;
	

	public BaseTask(Handler handler, Context context) {
		super();
		this.handler = handler;
		this.context = context;
	}

	@Override
	public void run() {
			doTask();
			afterTask(handler);

	}

	protected abstract void doTask();
	protected abstract void afterTask(Handler handler);
}
