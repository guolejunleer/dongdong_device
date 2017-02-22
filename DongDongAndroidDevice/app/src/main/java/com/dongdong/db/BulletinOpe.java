package com.dongdong.db;

import android.content.Context;

import com.dongdong.db.entry.BulletinBean;
import com.dongdong.db.gen.BulletinBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * 操作物业公告的执行者
 */
public class BulletinOpe {

    /**
     * 添加一条数据至数据库
     *
     * @param context      上下文
     * @param bulletinBean 数据
     */
    public static void insert(Context context, BulletinBean bulletinBean) {
        DBManager.getDaoSession(context).getBulletinBeanDao().insert(bulletinBean);
    }

    /**
     * 清空数据
     *
     * @param context 上下文
     */
    public static void delete(Context context, List<Long> ids) {
        DBManager.getDaoSession(context).getBulletinBeanDao().deleteByKeyInTx(ids);
    }

    /**
     * 查询所有数据
     *
     * @param context 上下文
     * @return 数据库所有数据
     */
    public static List<BulletinBean> queryAll(Context context) {
        QueryBuilder<BulletinBean> builder = DBManager.getDaoSession(context).
                getBulletinBeanDao().queryBuilder().orderDesc(BulletinBeanDao.Properties.Created);
        return builder.build().list();
    }
}
