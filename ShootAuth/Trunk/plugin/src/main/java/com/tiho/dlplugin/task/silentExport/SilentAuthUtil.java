package com.tiho.dlplugin.task.silentExport;

import android.content.Context;
import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jerry on 2016/5/13.
 */
public class SilentAuthUtil {
    public static final String CONFIG_FILE_NAME = "SilentAuth.xml";
    private static final String TAG_NAME_RESOURCES = "resources";
    private static final String TAG_NAME_VERSION = "version";
    private static final String TAG_NAME_AUTH = "auth";

    public static String getSavePath(Context context) {
       return context.getFilesDir().getPath() + File.separator + CONFIG_FILE_NAME;
    }

    public static int getVersion(Context context) {
        int version = 0;
        File file = new File(getSavePath(context));
        if (file.exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(is, "UTF-8");
                int type = parser.getEventType();
                while (type != XmlPullParser.END_DOCUMENT) {
                    boolean isFind = false;
                    switch (type) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if (TextUtils.equals(parser.getName(), TAG_NAME_RESOURCES)) {
                                String attrName = parser.getAttributeName(0);
                                if (TextUtils.equals(attrName, TAG_NAME_VERSION)) {
                                    String attrValue = parser.getAttributeValue(0);
                                    if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                        version = Integer.parseInt(attrValue);
                                    }
                                    isFind = true;
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            break;
                    }
                    if (isFind) {
                        break;
                    }
                    type = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return version;
    }

    public static List<String> getAuthList(Context context) {
        List<String> authKeys = new ArrayList<String>();
        File file = new File(getSavePath(context));
        if (file.exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(is, "UTF-8");
                int type = parser.getEventType();
                while (type != XmlPullParser.END_DOCUMENT) {
                    switch (type) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if (TextUtils.equals(parser.getName(), TAG_NAME_AUTH)) {
                                String value = parser.nextText();
                                if (!TextUtils.isEmpty(value)) {
                                    authKeys.add(value);
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            break;
                    }
                    type = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return authKeys;
    }


}
