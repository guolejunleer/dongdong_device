package com.dongdong.socket.normal;

import java.io.*;
import java.net.*;

public class UdpClientSocket {
	private byte[] buffer = new byte[1024];
	private DatagramSocket ds = null;

	public UdpClientSocket() throws Exception {
		ds = new DatagramSocket();
	}

	public final void setSoTimeout(final int timeout) throws Exception {
		ds.setSoTimeout(timeout);
	}

	public final int getSoTimeout() throws Exception {
		return ds.getSoTimeout();
	}

	public final DatagramSocket getSocket() {
		return ds;
	}

	public final DatagramPacket send(final String host, final int port,
			final byte[] bytes, int length) throws IOException {
		DatagramPacket dp = new DatagramPacket(bytes, length,
				InetAddress.getByName(host), port);
		ds.send(dp);
		return dp;
	}

	public final String receive(final String lhost, final int lport)
			throws Exception {
		DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
		ds.receive(dp);
		String info = new String(dp.getData(), 0, dp.getLength());
		return info;
	}

	public final void close() {
		try {
			ds.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}