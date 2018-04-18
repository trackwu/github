package com.tiho.base.base.manufactory.aidl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

public class SignatureUtil {
	
	private Context mContext;
	
	public void setContext(Context context){
		this.mContext = context.getApplicationContext();
	}
	
	/**
	 * 
	 * @param archiveFilePath
	 * @param flags PackageManager.GET_SIGNATURES
	 * @return
	 */
	public Signature[] getNotInstalledPackageSignature(String archiveFilePath){
		PackageManager pm = mContext.getPackageManager();
		PackageInfo  packageInfo = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_SIGNATURES);
		if(packageInfo != null){
			if(packageInfo.signatures != null && packageInfo.signatures.length >0){
	            return packageInfo.signatures;
			}
		}
		return null;
    }

	/**
	 * 获取已安装apk签名信息
	 * @param context
	 * @param packageName
	 * @return
	 */
	public Signature[] getInstalledPackageSignature(String packageName){
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        Iterator<PackageInfo> it = apps.iterator();
        while(it.hasNext()){
                PackageInfo info = it.next();
                if(info.packageName.equals(packageName)){
                	if(info.signatures != null && info.signatures.length >0){
                        return info.signatures;
                	}
                	break;
                }
        }
        return null;
	}
	
	/**
	 * 签名信息对比
	 * @param s1
	 * @param s2
	 * @return
	 */
	public boolean isSignaturesSame(Signature[] s1, Signature[] s2) {
		if (s1 == null) {
			return false;
		}
		if (s2 == null) {
			return false;
		}
		HashSet<Signature> set1 = new HashSet<Signature>();
		for (Signature sig : s1) {
		    set1.add(sig);
		}
		HashSet<Signature> set2 = new HashSet<Signature>();
		for (Signature sig : s2) {
			set2.add(sig);
		}
		
		if (set1.equals(set2)) {
			return true;
		}
		return false;
	}
}
