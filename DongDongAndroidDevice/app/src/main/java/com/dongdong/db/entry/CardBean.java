package com.dongdong.db.entry;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 卡号实体类
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
@Entity(generateConstructors = false)
public class CardBean {

    @Id(autoincrement = true)
    private Long id;

    private String cardNum;

    private long time;

    public CardBean() {
    }

    public CardBean(String cardNum, long time) {
        this.cardNum = cardNum;
        this.time = time;
    }
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "id is " + id + " ;cardNum is " + cardNum + ";time is " + time;
    }
}
