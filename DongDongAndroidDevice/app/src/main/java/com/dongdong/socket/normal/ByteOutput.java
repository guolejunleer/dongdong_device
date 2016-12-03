package com.dongdong.socket.normal;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 解包一个byte字符串
 */
public class ByteOutput {
    /**
     * 缓存byte字符串
     */
    private byte[] buffer = new byte[1400];

    /**
     * 缓存byte字符串长度
     */
    private int bufferlength = 0;

    /**
     * 当前打包字符串偏移
     */
    private int offset = 0;

    /**
     * 构造函数
     */
    public ByteOutput() {
    }

    public void setBytes(byte[] bytes, int length) throws IOException {
        if (length > 1400) {
            throw new IOException("bytes length " + length + " too long");
        }
        System.arraycopy(bytes, 0, buffer, 0, length);
        bufferlength = length;
        offset = 0;
    }

    public short getShort() {
        byte[] bytes = new byte[2];
        System.arraycopy(buffer, offset, bytes, 0, bytes.length);
        offset += bytes.length;
        return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public char getChar() {
        byte[] bytes = new byte[2];
        System.arraycopy(buffer, offset, bytes, 0, bytes.length);
        offset += bytes.length;
        return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public int getInt() {
        byte[] bytes = new byte[4];
        System.arraycopy(buffer, offset, bytes, 0, bytes.length);
        offset += bytes.length;
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8))
                | (0xff0000 & (bytes[2] << 16))
                | (0xff000000 & (bytes[3] << 24));
    }

    public long getLong() {
        byte[] bytes = new byte[8];
        System.arraycopy(buffer, offset, bytes, 0, bytes.length);
        offset += bytes.length;
        return (0xffL & (long) bytes[0]) | (0xff00L & ((long) bytes[1] << 8))
                | (0xff0000L & ((long) bytes[2] << 16))
                | (0xff000000L & ((long) bytes[3] << 24))
                | (0xff00000000L & ((long) bytes[4] << 32))
                | (0xff0000000000L & ((long) bytes[5] << 40))
                | (0xff000000000000L & ((long) bytes[6] << 48))
                | (0xff00000000000000L & ((long) bytes[7] << 56));
    }

    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    public double getDouble() {
        long l = getLong();
        // System.out.println(l);
        return Double.longBitsToDouble(l);
    }

    public byte getByte() {
        byte b = buffer[offset];
        offset += 1;
        return b;
    }

    public byte[] getBytes(int byteslength) {
        byte[] bytes = new byte[byteslength];
        System.arraycopy(buffer, offset, bytes, 0, bytes.length);
        offset += bytes.length;
        return bytes;
    }

    public String getString(int length) {
        return getString(length, "UTF-8");
    }

    public String getString(int length, String charsetName) {
        byte[] bytes = new byte[length];
        System.arraycopy(buffer, offset, bytes, 0, bytes.length);
        offset += bytes.length;
        int i = 0;
        for (; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                break;
            }
        }
        byte[] bytes2 = new byte[i];
        System.arraycopy(bytes, 0, bytes2, 0, bytes2.length);
        return new String(bytes2, Charset.forName(charsetName));
    }


    public int getRemainDataLength() {
        return bufferlength - offset;
    }
}
