package com.dongdong.db.entry;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 开门记录实体类
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
@Entity(generateConstructors = false)
public class UnlockLogBean {

    @Id(autoincrement = true)
    private Long id;

    private int unlockType;

    private int deviceId;

    private int roomId;

    private int userId;

    private String cardOrPhoneNum;

    private int unlockTime;

    public UnlockLogBean() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getUnlockType() {
        return unlockType;
    }

    public void setUnlockType(int unlockType) {
        this.unlockType = unlockType;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCardOrPhoneNum() {
        return cardOrPhoneNum;
    }

    public void setCardOrPhoneNum(String cardOrPhoneNum) {
        this.cardOrPhoneNum = cardOrPhoneNum;
    }

    public int getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(int unlockTime) {
        this.unlockTime = unlockTime;
    }

    @Override
    public String toString() {
        return "{id:" + id + ";unlockType: " + unlockType + ";cardOrPhoneNum:"
                + cardOrPhoneNum + "}";
    }
}
