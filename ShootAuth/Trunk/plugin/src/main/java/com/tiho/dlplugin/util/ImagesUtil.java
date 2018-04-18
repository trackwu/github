package com.tiho.dlplugin.util;

import java.io.IOException;
import java.io.InputStream;

import com.tiho.base.common.LogManager;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImagesUtil {
	private final static String PIC_RESOUCE_PATH ="com/timo/dlpushplugin/res/";
	
	public static Drawable getBimtmapDrawable(String picName){
		BitmapDrawable bitMapDrawable = null;
		Class c = ImagesUtil.class;
		InputStream is = c.getResourceAsStream("/" + PIC_RESOUCE_PATH + picName + ".png");// +" , classloader:"+c.getClassLoader());
		LogManager.LogShow("ImageUtil.class.getResourceAsStream == " + is + "  picName:" + picName +" , classloader:"+c.getClassLoader());
		if(is != null){
			bitMapDrawable = new BitmapDrawable(is);
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bitMapDrawable;
	}
	

}
