package com.dongdong.socket.beat;

public interface PeerAddressCallback {
    /**
     * 对端ip回调
     *
     * @param ip 对端ip
     * @return state
     */
    int onPeerAddress(String ip);

    /**
     * 核心板版本
     *
     * @param regstatus
     * @param version
     * @return state
     */
    int onDrive(int regstatus, String version);

    /**
     * 云之讯信息
     *
     * @param sid   主帐号
     * @param pwd   密码
     * @param appid 应用id
     * @return state
     */
    int onUcpaasInfo(String sid, String pwd, String appid);

    /**
     * 云通讯信息
     *
     * @param userId     登录云通讯帐号
     * @param accountSid 云通讯主帐号
     * @param appKey     应用Token
     * @param appToken   应用Key
     * @param appToken   厂商固话
     * @return state
     */
    int onGetYunTongXunInfo(String userId, String accountSid,
                            String appToken, String appKey, String vendorPhone);
}
