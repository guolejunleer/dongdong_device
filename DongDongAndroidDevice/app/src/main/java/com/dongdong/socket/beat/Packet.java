package com.dongdong.socket.beat;

/**
 * DeviceService数据包打包类
 */
public class Packet {
    private ByteInput mByteInput;

    /**
     * 内部接口: 1 如果音视频包太大，则需要分包回调给上层
     */
    public interface DSMediaPacketCallback {
        /**
         * 音视频数据分片包
         *
         * @param mdpkt       音视频数据头指针
         * @param mdpktlength 音视频数据长度
         */
        void onMediaPacket(byte[] mdpkt, int mdpktlength);
    }

    /**
     * 构造函数
     */
    public Packet() {
        mByteInput = new ByteInput();
    }

    /**
     * 打包协议头
     */
    private void packetHeader(int cmdflag, short cmdid, short totalseg,
                              short subseg) {
        mByteInput.initOffset();
        mByteInput.putByte((byte) 0xdc); // groupcode
        mByteInput.putShort(cmdid);
        mByteInput.putByte((byte) 1); // version
        mByteInput.putInt(cmdflag);
        mByteInput.putShort(totalseg);
        mByteInput.putShort(subseg);
        mByteInput.putShort((short) 0); // segflag
        mByteInput.putShort((short) 0); // reserved1
        mByteInput.putInt(0); // reserved2
    }

    /**
     * 打包协议头
     */
    private void packetHeader(int cmdflag, short cmdid, short totalseg,
                              short subseg, short reserved1) {
        mByteInput.initOffset();
        mByteInput.putByte((byte) 0xdc); // groupcode
        mByteInput.putShort(cmdid);
        mByteInput.putByte((byte) 1); // version
        mByteInput.putInt(cmdflag);
        mByteInput.putShort(totalseg);
        mByteInput.putShort(subseg);
        mByteInput.putShort((short) 0); // segflag
        mByteInput.putShort(reserved1); // reserved1
        mByteInput.putInt(0); // reserved2
    }

    /**
     * 搜索回应
     *
     * @param cmdflag 与请求消息中cmdflag一致
     * @return result
     */
    public byte[] scanReponese(int cmdflag) {
        ALinuxData.debugLog("beat.Packet.clazz--->>>scanReponese cmdflag:"
                + cmdflag);
        packetHeader(cmdflag, ALinuxData.CMD_SCAN_RESPONSE, (short) 1, (short) 1);
        mByteInput.putInt(ALinuxData.mCameraId);
        int length = 32 + 64 + 16 + 16 + 2 + 2 + 4 + 32;
        for (int i = 0; i < length; i++) {
            mByteInput.putByte((byte) 0);
        }
        return mByteInput.getCopyBytes();
    }

    /**
     * 搜索回应
     *
     * @param cmdflag 与请求消息中cmdflag一致
     * @return result
     */
    public byte[] scanReponese(int cmdflag, short reserved1) {
        ALinuxData.debugLog("beat.Packet.clazz--->>>scanReponese cmdflag:"
                + cmdflag + ",reserved1:" + reserved1);
        packetHeader(cmdflag, ALinuxData.CMD_SCAN_RESPONSE, (short) 1,
                (short) 1, reserved1);
        mByteInput.putInt(ALinuxData.mCameraId);
        int length = 32 + 64 + 16 + 16 + 2 + 2 + 4 + 32;
        for (int i = 0; i < length; i++) {
            mByteInput.putByte((byte) 0);
        }
        return mByteInput.getCopyBytes();
    }

    /**
     * KeepAlive回应
     *
     * @param cmdflag 与请求消息中cmdflag一致
     * @param result
     */
    public byte[] keepAliveRequest(int cmdflag, int result) {
        ALinuxData.debugLog("beat.Packet.clazz--->>> keepAliveRequest result:" + result);
        packetHeader(cmdflag, ALinuxData.CMD_KEEPALIVE_RESPONSE, (short) 1,
                (short) 1);
        mByteInput.putInt(result);
        return mByteInput.getCopyBytes();
    }

    /**
     * GetParam请求核心板获取IP电话（VOIP）参数
     *
     * @param cmdflag 与请求消息中cmdflag一致
     * @return result
     */
    public byte[] getVOIPParamRequest(int cmdflag, int deviceId, int voipType) {
        ALinuxData.debugLog("beat.Packet.clazz--->>>getVOIPParamRequest deviceId:"
                + deviceId + ";voipType:" + voipType);
        packetHeader(cmdflag, ALinuxData.CMD_GETPARAM_REQUEST, (short) 1,
                (short) 1);
        mByteInput.putInt(deviceId);
        mByteInput.putInt(voipType);
        return mByteInput.getCopyBytes();
    }

}
