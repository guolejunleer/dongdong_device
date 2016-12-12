package com.dongdong.socket.normal;

import com.dongdong.db.entry.UnlockLogBean;

import java.util.List;

/**
 * 解析核心板传过来的数据后的结果回调
 */
public interface DeviceServiceCallback {

    ////////////////////////////////必选实现/////////////////////////////////////

    /**
     * 打开音视频数据请求
     *
     * @param cmdFlag   请求消息的用户数据，相应的回应消息回传该值
     * @param channelId 设备通道ID
     * @param mediaMode 播放类型
     */
    int onPlayRequest(int cmdFlag, int channelId, int mediaMode);

    /**
     * 关闭音视频数据请求
     *
     * @param cmdFlag   请求消息的用户数据，相应的回应消息回传该值
     * @param channelId 设备通道ID
     * @param mediaMode 播放类型
     */
    int onStopRequest(int cmdFlag, int channelId, int mediaMode);

    /**
     * 呼叫通知回应
     *
     * @param cmdFlag 与请求消息中cmdFlag一致
     * @param result  执行结果
     * @param roomNum 房号
     */
    int onCallResult(int cmdFlag, int result, String roomNum, int timer);

    /**
     * 开锁请求
     *
     * @param unlockType     开锁类型
     * @param cardOrPhoneNum 卡号或者手机号
     * @return state
     */
    int onUnlockRequest(int unlockType, String cardOrPhoneNum ,String roomNum);

    /**
     * 发送音视频流请求
     *
     * @param cmdFlag    请求消息的用户数据，相应的回应消息回传该值
     * @param channelId  设备通道ID
     * @param sequence   音视频数据总分片索引,第一个分片开始从1依次递增
     * @param isKeyFrame 是否为关键帧
     * @param mediaData  音视频包
     */
    int onSendMediaRequest(int cmdFlag, int channelId, int sequence,
                           byte isKeyFrame, byte[] mediaData);

    //////////////////////////////可选实现/////////////////////////////////////

    /**
     * 设置音频音量请求
     */
    int onSetVolumeRequest();

    /**
     * 设置视频品质请求
     */
    int onSetVideoModeRequest();

    /**
     * 设置视频属性请求
     */
    int onSetVideoAttrRequest();

    /**
     * 获取网口配置信息回应
     */
    int onGetNetResult(int cmdFlag, InfoNetParam netParam);

    /**
     * 设置网口配置信息回应
     */
    int onSetNetResult(int cmdFlag, int result);

    /**
     * 检查刷卡卡号回应
     *
     * @param cmdFlag    与请求消息中cmdflag一致
     * @param result     执行结果
     * @param cardNum 卡号
     */
    int onCheckCardResult(int cmdFlag, int result, String cardNum);

    /**
     * 检查开门密码回应
     *
     * @param cmdFlag    与请求消息中cmdflag一致
     * @param result     执行结果
     * @param roomNum 房号
     * @param pwd        开门密码
     */
    int onCheckPasswordResult(int cmdFlag, int result,
                              String roomNum, String pwd);

    /**
     * 删除刷卡卡号请求
     *
     * @param cardNum 卡号
     */
    int onDelCardRequest(int cmdFlag, String cardNum);

    /**
     * 推送消息透传请求
     */
    int onTunnelPushRequest();

    /**
     * 客户端信令透传请求
     */
    int onTunnelCmdRequest();

    // 新增

    /**
     * 监视、通话回调界面状态
     *
     * @param status 状态值 见DSDefine.java
     */
    int onPlayOrStopDeviceStatusCallback(int status);

    /**
     * 拨打电话
     *
     * @param roomNum  房号
     * @param phoneNum 拨打电话
     */
    int onPhoneCallRequest(String roomNum, String phoneNum);

    /**
     * 停止拨打电话
     */
    int onStopPhoneCallRequest();

    /**
     * 禁止打电话
     *
     * @param reason 禁止拨打原因  0:余额不足；1:拨打过于频繁，稍后再试
     * @return state
     */
    int onDisablePhoneCallRequest(int reason);


    /**
     * 开门记录返回状态回应
     *
     * @param result      返回状态码
     * @param unlockCount 返回开门记录数量
     * @param unlockIndex  开门记录开门Index数据集合
     * @return state
     */
    int onUnlockStateResult(int result, int unlockCount,
                            List<Integer> unlockIndex);

    /**
     * 获取平台时间
     *
     * @param platformTime 平台时间
     * @return state
     */
    int onGetTimestampResult(int platformTime);

    /**
     * 获取历史开门记录请求
     *
     */
    int onGetHistoryUnLockRecordRequest();

}
