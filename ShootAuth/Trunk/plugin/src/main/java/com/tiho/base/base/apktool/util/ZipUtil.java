package com.tiho.base.base.apktool.util;

import com.tiho.base.base.apktool.exception.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;


/**
 * Created by huan on 2016/1/4.
 */
public final class ZipUtil {

    private static final byte[] SIGNATURE = new byte[]{0x50, 0x4b, 0x05, 0x06};

    private ZipUtil() {

    }

    /**
     * 写注释
     *
     * @param file
     * @param comment
     * @return
     * @throws IOException
     */
    public static void writeComment(File file, String comment) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");

            // 获取注释长度的偏移地址
            long pos = getCommentLengthAddress(raf);

            // 写注释
            byte[] commentBytes = comment.getBytes();
            raf.seek(pos);
            raf.writeShort(Short.reverseBytes((short) commentBytes.length));
            raf.write(commentBytes);

            // 设置文件长度
            raf.setLength(pos + ShortUtil.getShortSize() + commentBytes.length);
        } finally {
            // 无论是否成功或是异常都要走finally关闭资源
            if (null != raf) {
                raf.close();
            }
        }
    }

    /**
     * 读注释
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String readComment(File file) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");

            // 获取注释长度的偏移地址
            long pos = getCommentLengthAddress(raf);

            // 获取注释长度
            byte[] commentLengthBytes = new byte[ShortUtil.getShortSize()];
            raf.seek(pos);
            raf.readFully(commentLengthBytes);
            int commentLength = ShortUtil.bytes2Short(commentLengthBytes);

            // 获取注释字节数组
            byte[] commentBytes = new byte[commentLength];
            pos += ShortUtil.getShortSize();
            raf.seek(pos);
            raf.readFully(commentBytes);

            // 获取注释
            String comment = new String(commentBytes);

            return comment;
        } finally {
            // 无论是否成功或是异常都要走finally关闭资源
            if (null != raf) {
                raf.close();
            }
        }
    }

    /**
     * 获取注释长度的偏移地址
     *
     * @param raf
     * @return
     * @throws IOException
     */
    private static long getCommentLengthAddress(RandomAccessFile raf) throws IOException, NotFoundException {
        long pos = raf.length();
        byte[] bytes = new byte[SIGNATURE.length];

        pos -= 4;
        while (pos > 4) {
            raf.seek(pos);
            raf.readFully(bytes);

            // 字节数组比较
            if (Arrays.equals(SIGNATURE, bytes)) {
                break;
            }
            pos -= 1;
        }

        // 注释长度的偏移地址未找到抛一个异常
        if (pos < 4) {
            throw new NotFoundException("Address not found");
        }

        // 获取注释长度的偏移地址
        pos += 20;

        return pos;
    }

    /**
     * 文本是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
