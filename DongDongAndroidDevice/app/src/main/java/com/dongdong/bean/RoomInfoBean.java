package com.dongdong.bean;

import com.dongdong.db.entry.RoomCardBean;

import java.util.List;

public class RoomInfoBean {
    private int roomId;
    private int cardIndex;
    private int cardCount;
    private List<RoomCardBean> platCards;

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

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public List<RoomCardBean> getPlatCards() {
        return platCards;
    }

    public void setPlatCards(List<RoomCardBean> platCards) {
        this.platCards = platCards;
    }

    @Override
    public String toString() {
        return "(roomId:" + roomId + ",cardIndex:" + ",cardCount:" + cardCount+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomInfoBean that = (RoomInfoBean) o;

        return roomId == that.roomId;

    }

    @Override
    public int hashCode() {
        return roomId;
    }
}
