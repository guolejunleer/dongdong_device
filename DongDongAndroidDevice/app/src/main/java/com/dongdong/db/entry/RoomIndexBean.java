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
public class RoomIndexBean {

    @Id(autoincrement = true)
    private Long id;

    private int roomId;

    private int cardIndex;

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

    public int getCardIndex() {
        return cardIndex;
    }

    public void setCardIndex(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    @Override
    public String toString() {
        return "(id:" + id + ",roomId:" + roomId + ",cardIndex:" + cardIndex + ")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomIndexBean that = (RoomIndexBean) o;

        return roomId == that.roomId;

    }

    @Override
    public int hashCode() {
        return roomId;
    }
}
