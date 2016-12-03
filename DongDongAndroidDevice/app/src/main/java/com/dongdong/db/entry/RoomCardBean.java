package com.dongdong.db.entry;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 卡号实体类
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
@Entity(generateConstructors = false)
public class RoomCardBean {

    @Id(autoincrement = true)
    private Long id;

    private int roomId;

    private String cardNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    @Override
    public String toString() {
        return "id is " + id + ";roomId is " + roomId + ";cardNum is " + cardNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomCardBean that = (RoomCardBean) o;

        if (roomId != that.roomId) return false;
        return cardNum != null ? cardNum.equals(that.cardNum) : that.cardNum == null;

    }

    @Override
    public int hashCode() {
        int result = roomId;
        result = 31 * result + roomId;
        result = 31 * result + (cardNum != null ? cardNum.hashCode() : 0);
        return result;
    }
}
