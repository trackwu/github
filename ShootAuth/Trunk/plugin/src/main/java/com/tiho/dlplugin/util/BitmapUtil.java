package com.tiho.dlplugin.util;

import java.io.File;

import com.tiho.base.common.LogManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitmapUtil {

	/**
	 * 从文件路径获取bitmap
	 * 
	 * @param fileName
	 * @return Bitmap
	 */
	public static Bitmap getBitmapFromFile(String fileName) {
		Bitmap bitmap = null;
		LogManager.LogShow("getBitmapFromFile filename = " + fileName);
		File f = new File(fileName);
		if (null != f && f.exists()) {
			LogManager.LogShow("getBitmapFromFile file exists!");
		}
		try {
			bitmap = BitmapFactory.decodeFile(fileName );
			LogManager.LogShow("getBitmapFromFile bitmap = " + bitmap);
		} catch (Exception e) {
			LogManager.LogShow("getBitmapFromFile Exception" + e.getMessage());
		}
		return bitmap;
	}
	
	
	
	public static BitmapFactory.Options getOptions() {
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inJustDecodeBounds = true;
		int samplewidth = option.outWidth / 72;
		int sampleheight = option.outHeight / 72;
		option.inSampleSize = (samplewidth > sampleheight) ? samplewidth : sampleheight;
		LogManager.LogShow("getBitmapFromFile width = " + option.outWidth + ", height = " + option.outHeight + ", inSampleSize = " + option.inSampleSize);
		option.inJustDecodeBounds = false;

		return option;
	}
	
	
	/**
	 * 不用加扩展名
	 * @param picName
	 * @return
	 */
	public static Bitmap getBitmap(String picName) {
		Drawable drawable = ImagesUtil.getBimtmapDrawable(picName);
		if (drawable != null) {
			return ((BitmapDrawable) drawable).getBitmap();
		}
		return null;
	}
}
