package com.tiho.base.base.apktool;

import android.content.pm.PackageManager;

import com.tiho.base.base.apktool.util.ZipUtil;
import com.tiho.base.common.LogManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huan on 2016/1/4.
 */
public final class ApkTool {

    private ApkTool() {

    }

    /**
     * 通过meta读取数据
     *
     * @param packageManager
     * @param path
     * @return
     */
    public static DataReader getReader(PackageManager packageManager, String path) {
        return new MetaDataReader(packageManager, path);
    }

    /**
     * 通过文件读取数据
     *
     * @param file
     * @return
     */
    public static DataReader getReader(File file) {
        return new ZipDataReader(getData(file));
    }

    /**
     * 写数据
     *
     * @param file
     * @param map
     */
    public static void putData(File file, Map<String, String> map) {
        try {
            String channel = generateData(map);
            ZipUtil.writeComment(file, channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成数据
     *
     * @param map
     * @return
     */
    private static String generateData(Map<String, String> map) {
        StringBuffer buffer = new StringBuffer();
        for (String key : map.keySet()) {
            buffer.append(key);
            buffer.append("=");
            buffer.append(map.get(key));
            buffer.append("&");
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }

        return buffer.toString();
    }

    private static Map<String, String> getData(File file) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            String comment = ZipUtil.readComment(file);
            LogManager.LogShow("comment = " + comment);//test
            if (!ZipUtil.isEmpty(comment)) {
                String items[] = comment.split("&");
                for (String item : items) {
                    String str[] = item.split("=");
                    if (str.length >= 2) {
                        map.put(str[0], str[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }
}
