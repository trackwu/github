package com.tiho.dlplugin.update;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jerry on 2016/9/9.
 */
public class DecodeFileUpdate {

    private static final int BUFFER_SIZE = 0x1000;

    protected static void decodeFile(File srcFile, File outFile) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(srcFile),BUFFER_SIZE);
        FileOutputStream out = new FileOutputStream(outFile);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = in.read(buffer);
            while (length != -1) {
                decode(buffer, length);
                out.write(buffer, 0, length);
                length = in.read(buffer);
            }
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static void encodeFile(File srcFile, File outFile) throws IOException {
        decodeFile(srcFile,outFile);
    }
    //对字符串进行简单加密和解密
    private static byte[] decode(byte[] input, int len) {
        byte[] seed = {(byte) 0xa4, 0x02, 0x06, 0x05, 0x01, 0x09, 0x2e, 0x4d, 0x5c, (byte) 0xe1, 0x7c, 0x55, (byte) 0x8a, (byte) 0xcc, (byte) 0xac};//加密种子
        int i, j = 0;
        for (i = 0; i < len; i++) {
            input[i] = (byte) (input[i] ^ seed[j]);
            j = j < 14 ? j + 1 : 0;
        }
        return input;
    }
}
