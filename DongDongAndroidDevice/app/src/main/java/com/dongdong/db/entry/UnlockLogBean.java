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

    private String cardOrPhoneNum;

    private int unlockTime;

    private int upload;

    private String roomNum;

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

    public int getUpload() {
        return upload;
    }

    public void setUpload(int upload) {
        this.upload = upload;
    }

    public String getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(String roomNum) {
        this.roomNum = roomNum;
    }

    @Override
    public String toString() {
        return "(id:" + id + ";unlockType: " + unlockType + ";cardOrPhoneNum:"
                + cardOrPhoneNum +";roomNum:"+roomNum+ ")";
    }

}
