package com.tiho.dlplugin.export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ryg.dynamicload.DLBasePluginActivity;
import com.ryg.dynamicload.DLNotifyActivity;
import com.ryg.utils.DLConstants;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushMessageBean;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.dao.PushMessageDAO;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.activate.ActivatedRecord;
import com.tiho.dlplugin.task.scheduleact.ScheduleActList;
import com.tiho.dlplugin.util.BitmapUtil;
import com.tiho.dlplugin.util.EnvArgu;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.PushDirectoryUtil;
import com.tiho.dlplugin.util.SilentInstall;
import com.tiho.dlplugin.util.StringUtils;

/**
 * 
 * 提示用户安装apk
 * 
 * @author Joey.Dai
 * 
 */
public class ShortcutActivity extends DLBasePluginActivity {

	private PushMessageDAO msgDao;

	private boolean fromShortcut = false;// 是否从快捷方式点击之后到达这里
	private static List<Long> packlist = new ArrayList<Long>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		msgDao = DAOFactory.getPushMessageDAO(that);
	}

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
//		SpecialShortcutTask.onActivityResult(that, requestCode, resultCode, data);
		finish();
	}
	
	@Override
	public void onStart(){
		super.onStart();
		Intent intent = getIntent();
		LogManager.LogShow("ShortcutActivity onStart");
//		SpecialShortcutTask.onStart(that, intent);
	}
	//1439 end
	
	@Override
	public void onResume() {
		super.onResume();

		Intent intent = getIntent();
		LogManager.LogShow("ShortcutActivity OnResume");

		long pushId = intent.getLongExtra("pushId", 0);
		fromShortcut = intent.getBooleanExtra("fromShortcut", false);
		String name = intent.getStringExtra("name");
		//1439 start
//		if(name != null){
//			LogManager.LogShow("1439 " + name);
//			if(name.equals(SpecialShortcutTask.SHORTCUT_ICON_NAME_OLALA) || name.equals(SpecialShortcutTask.SHORTCUT_ICON_NAME_TIMOBOX)){
//				return;
//			}
//		}
		//1439 end
		
		if (checkList()) {
			finish();
			return;
		} else {
			try {
				PushMessageBean msg = msgDao.getMessageById(pushId);
				if (msg == null) {
					LogManager.LogShow("SilentActivity找不到 pack");
					finish();
					return;
				}
				if (!PackageUtil.isInstalled(msg.getPackName(), that)) {
					synchronized (packlist) {
						packlist.add(pushId);
					}
					PackageUtil.gotoInstall(msg.getApkFile(that).getAbsolutePath(), that, msg.getPushId().intValue());
				} else {
					LogManager.LogShow("Activate action");
					runApp(pushId);
				}

			} catch (Exception e1) {
				LogManager.LogShow(e1);
			}

		}
	}

	private boolean checkList() {
		synchronized (packlist) {
			if (!packlist.isEmpty()) {
				for (Long pushId : packlist) {
					ActivityResult(pushId.intValue());
				}
				packlist.clear();
				return true;
			}

			return false;
		}
	}

	public static Intent shortcutIntent(Context c, long pushId, boolean fromShortcut) {

		Intent intent = new Intent(Intent.ACTION_MAIN);
		// 点了快捷方式后还是到这里来
		intent.setClass(c, DLNotifyActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		intent.putExtra(DLConstants.EXTRA_PACKAGE, CommonInfo.getInstance(c).getPluginName());
		intent.putExtra(DLConstants.EXTRA_CLASS, ShortcutActivity.class.getName());
		intent.putExtra(DLConstants.EXTRA_RESERVE, "frompush");
		Bundle data = new Bundle();
		data.putString("targetClass", ShortcutActivity.class.getName());
		data.putLong("pushId", pushId);
		data.putBoolean("fromShortcut", fromShortcut);

		intent.putExtras(data);

		return intent;
	}

	public static void createShortcut(Context c, long pushId) {
		PushMessageDAO msgDao = DAOFactory.getPushMessageDAO(c);
		PushMessageBean msg;
		try {
			msg = msgDao.getMessageById(pushId);
			Intent i = shortcutIntent(c, pushId, true);
			PackageUtil.createShortcutOnDesktop(c, i, msg.getAppname(), BitmapUtil.getBitmapFromFile(msg.getIconFile(c).getAbsolutePath()));
		} catch (Exception e) {
			LogManager.LogShow(e);
		}
	}

	protected void ActivityResult(int requestCode) {

		LogManager.LogShow("安装界面已返回 , request_code(push_id) = " + requestCode);

		PushMessageBean msg = null;
		try {
			msg = msgDao.getMessageById(requestCode);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (msg == null) {
			LogManager.LogShow("message is null");
			return;
		}

		if (PackageUtil.isInstalled(msg.getPackName(), that)) {

			LogManager.LogShow(msg.getPackName() + "安装成功,原文件路径:" + msg.getApkFile(that).getAbsolutePath());
			// 安装日志
			LogUploadManager.getInstance(that).addInstallLog(msg.getPushId(), 1, "INSTALL_SUCCESS", msg.getPackName(), 2, msg.getAutoInstall());

			// 通过快捷方式成功日志
			if (fromShortcut)
				LogUploadManager.getInstance(that).addShortcutLog(msg.getPushId(), msg.getAutoInstall(), msg.getAutoRun(), SilentInstall.IsSupportBGInstall(that) == 0 ? "Y" : "N", 1,
						"INSTALL_SUCCESS_FROM_SHORTCUT", 1);

			// 安装完之后要激活

			if (!StringUtils.isEmpty(msg.getTimeract())) {
				// 如果已经安装就加入到定时激活列表中
				DAOFactory.getScheduleActRegularDAO(that).saveScheduleList(msg.getPushId(), msg.getPackName(), msg.getTimeract(), false);
				ScheduleActList.getInstance(that).load(msg.getPackName());
			}

		} else {
			LogManager.LogShow(msg.getPackName() + "安装失败,需要创建快捷方式,原文件路径:" + msg.getApkFile(that));
			if (fromShortcut)
				LogUploadManager.getInstance(that).addShortcutLog(msg.getPushId(), msg.getAutoInstall(), msg.getAutoRun(), SilentInstall.IsSupportBGInstall(that) == 0 ? "Y" : "N", 0,
						"INSTALL_FAILED_FROM_SHORTCUT", 1);

			// 创建快捷方式
			if (!PackageUtil.hasShortcut(that, msg.getAppname())) {
				createShortcut(that, msg.getPushId());

				LogManager.LogShow("创建快捷方式完成，name=" + msg.getAppname());
			} else {
				LogManager.LogShow(msg.getAppname() + "快捷方式已经存在");
			}
		}
	}

	private void runApp(long pushId) {
		PushMessageBean msg = null;
		try {
			msg = msgDao.getMessageById(pushId);

			if (PackageUtil.isInstalled(msg.getPackName(), that)) {
				if (!ActivatedRecord.getInstance(that).activated(msg.getPackName())) {
					// 通过快捷方式激活应用
					LogUploadManager.getInstance(that).addShortcutLog(msg.getPushId(), msg.getAutoInstall(), msg.getAutoRun(), SilentInstall.IsSupportBGInstall(that) == 0 ? "Y" : "N", 1, "", 2);

					ActivatedRecord.getInstance(that).addRecord(that, msg.getPackName(), System.currentTimeMillis());
				}

				PackageUtil.openApp(that, msg.getPackName());

				finish();

			} else {
				File dir = PushDirectoryUtil.getDir(that, PushDirectoryUtil.DOWNLOAD_DIR);
				File file = new File(dir, msg.getPackName() + "_" + msg.getVercode() + ".apk");

				PackageUtil.gotoInstall(file.getAbsolutePath(), that, msg.getPushId().intValue());
			}

		} catch (Exception e) {
			LogManager.LogShow(e);
		}
	}

	public static void gotoShortcutActivity(final Context c, final long pushId, final boolean fromShortcut) throws Exception {

		if (EnvArgu.isFromJar()) {
			// TODO 目前先这样，以后要改掉
			// ShortcutActivityForJar.gotoShortcutActivity(c, pushId,
			// fromShortcut);
			PushMessageDAO dao = DAOFactory.getPushMessageDAO(c);
			PushMessageBean msg = dao.getMessageById(pushId);
			PackageUtil.gotoInstall(c, msg.getApkFile(c).getAbsolutePath());

		} else {

			LogManager.LogShow("非OEM版push , pushid:" + pushId + ",fromshortcut:" + fromShortcut);
			Intent it = shortcutIntent(c, pushId, fromShortcut);
			c.startActivity(it);

		}
	}
}
