package com.tiho.dlplugin.install;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {

    /**
     * 是否是空字符串
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 是否是空白内容
     *
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static String stringAfter(String str, String pattern) {
        int index = str.indexOf(pattern);

        return index == -1 ? null : str.substring(index + pattern.length());
    }

    public static String stringBetween(String src, String begin, String end) {
        String after = stringAfter(src, begin);
        if (after != null) {
            int index = after.indexOf(end);
            if (index != -1)
                return after.substring(0, index);
        }

        return null;
    }

    public static String md5(byte[] d) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("MD5");
        int i;
        String md5hash = null;
        digest.update(d, 0, d.length);
        byte[] md5sum = digest.digest();
        BigInteger bigInt = new BigInteger(1, md5sum);
        md5hash = bigInt.toString(16);

        if (md5hash != null && md5hash.length() != 33) {
            String tmp = "";
            for (i = 1; i < (33 - md5hash.length()); i++) {
                tmp = tmp.concat("0");
            }
            md5hash = tmp.concat(md5hash);
        }

        return md5hash;
    }

    public static byte[] getBytesUtf8(final String string) {
        if (string == null) {
            return null;
        }
        return string.getBytes(Charset.forName("UTF-8"));
    }

    public static String newStringUtf8(final byte[] bytes) {
        return bytes == null ? null : new String(bytes, Charset.forName("UTF-8"));
    }
}
