package com.dongdong.socket.beat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import java.nio.charset.Charset;

/**
 * 打包一个byte字符串
 */
@SuppressLint("NewApi")
public class ByteInput {
	/**
	 * 打包一个byte字符串
	 */
	private byte[] buffer = new byte[1024];

	/**
	 * 当前打包字符串偏移
	 */
	private int offset = 0;

	/**
	 * 构造函数
	 */
	public ByteInput() {
	}

	public void initOffset() {
		offset = 0;
	}

	public void putShort(short data) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data & 0xff00) >> 8);
		System.arraycopy(bytes, 0, buffer, offset, bytes.length);
		offset += bytes.length;
	}

	public void putChar(char data) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (data);
		bytes[1] = (byte) (data >> 8);
		System.arraycopy(bytes, 0, buffer, offset, bytes.length);
		offset += bytes.length;
	}

	public void putInt(long intBits) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (intBits & 0xff);
		bytes[1] = (byte) ((intBits & 0xff00) >> 8);
		bytes[2] = (byte) ((intBits & 0xff0000) >> 16);
		bytes[3] = (byte) ((intBits & 0xff000000) >> 24);
		System.arraycopy(bytes, 0, buffer, offset, bytes.length);
		offset += bytes.length;
	}

	public void putLong(long data) {
		byte[] bytes = new byte[8];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data >> 8) & 0xff);
		bytes[2] = (byte) ((data >> 16) & 0xff);
		bytes[3] = (byte) ((data >> 24) & 0xff);
		bytes[4] = (byte) ((data >> 32) & 0xff);
		bytes[5] = (byte) ((data >> 40) & 0xff);
		bytes[6] = (byte) ((data >> 48) & 0xff);
		bytes[7] = (byte) ((data >> 56) & 0xff);
		System.arraycopy(bytes, 0, buffer, offset, bytes.length);
		offset += bytes.length;
	}

	public void putFloat(float data) {
		int intBits = Float.floatToIntBits(data);
		putInt(intBits);
	}

	public void putDouble(double data) {
		long intBits = Double.doubleToLongBits(data);
		putInt(intBits);
	}

	public void putBytes(byte[] bytes, int offset, int byteslength) {
		System.arraycopy(bytes, offset, buffer, this.offset, byteslength);
		this.offset += byteslength;
	}

	public void putBytes(byte[] bytes) {
		System.arraycopy(bytes, 0, buffer, offset, bytes.length);
		offset += bytes.length;
	}

	public void putByte(byte b) {
		buffer[offset] = b;
		offset += 1;
	}

	@SuppressLint("NewApi")
	public void putString(String data, String charsetName) {
		Charset charset = Charset.forName(charsetName);
		System.arraycopy(data.getBytes(charset), 0, buffer, offset,
				data.getBytes(charset).length);
		offset += data.getBytes(charset).length;
	}

	public void putString(String data) {
		putString(data, "UTF-8");
	}

	public byte[] getCopyBytes() {
		byte[] bytes = new byte[offset];
		System.arraycopy(buffer, 0, bytes, 0, bytes.length);
		return bytes;
	}

	public byte[] getBytes() {
		return buffer;
	}

	public int getLength() {
		return offset;
	}
}
