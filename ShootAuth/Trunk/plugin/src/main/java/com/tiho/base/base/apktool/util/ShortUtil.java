package com.tiho.base.base.apktool.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by huan on 2016/1/4.
 */
public final class ShortUtil {

    private ShortUtil() {

    }

    /**
     * short转换成字节数组（小端序）
     *
     * @param bytes
     * @return
     */
    public static short bytes2Short(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(bytes[0]);
        buffer.put(bytes[1]);
        return buffer.getShort(0);
    }

    /**
     * 字节数组转换成short（小端序）
     *
     * @param data
     * @return
     */
    public static byte[] short2Bytes(short data) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(data);
        buffer.flip();
        return buffer.array();
    }

    /**
     * 获取Short类型字节长度
     *
     * @return
     */
    public static int getShortSize() {
        return Short.SIZE / Byte.SIZE;
    }
}
