package com.dongdong.db.entry;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 物业公告实体类
 */
@Entity(generateConstructors = false)
public class BulletinBean implements Comparable<BulletinBean> {
    @Id(autoincrement = true)
    private Long id;

    private String notice;
    private String created;
    private String title;
    private String noticeid;

    public BulletinBean() {
    }

    public BulletinBean(String notice, String created, String title, String noticeid) {
        this.notice = notice;
        this.created = created;
        this.title = title;
        this.noticeid = noticeid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getNoticeid() {
        return noticeid;
    }

    public void setNoticeid(String noticeid) {
        this.noticeid = noticeid;
    }

    @Override
    public int compareTo(BulletinBean b) {
        if (b != null) {
            return this.getCreated().compareTo(b.getCreated());
        }
        return 0;
    }

    @Override
    public String toString() {
        return "id is " + id + ";notice is " + notice + ";created is " + created
                + ";title is " + title + ";noticeid is" + noticeid;
    }
}
