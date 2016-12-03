package com.dongdong.db;

import android.content.Context;

import com.dongdong.db.entry.UnlockLogBean;
import com.dongdong.db.gen.UnlockLogBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 操作开门记录的执行者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class UnlockLogOpe {

    /**
     * 添加一条数据至数据库
     *
     * @param context 上下文
     * @param data    数据
     */
    public static void insertData(Context context, UnlockLogBean data) {
        DBManager.getDaoSession(context).getUnlockLogBeanDao().insert(data);
    }

    /**
     * 将数据实体通过事务添加至数据库
     *
     * @param context 上下文
     * @param list    数据集合
     */
    public static void insertData(Context context, List<UnlockLogBean> list) {
        if (null == list || list.size() <= 0) {
            return;
        }
        DBManager.getDaoSession(context).getUnlockLogBeanDao().insertInTx(list);
    }


    /**
     * 添加数据至数据库，如果存在，将原来的数据覆盖
     *
     * @param context 上下文
     * @param data    数据
     */
    public static void saveData(Context context, UnlockLogBean data) {
        DBManager.getDaoSession(context).getUnlockLogBeanDao().save(data);
    }

    /**
     * 删除一条数据
     *
     * @param context       上下文
     * @param localUnlockId 数据ID
     */
    public static void deleteData(Context context, long localUnlockId) {
        DBManager.getDaoSession(context).getUnlockLogBeanDao().deleteByKey(localUnlockId);
        // DBManager.getDaoSession(context).getUnlockLogBeanDao().deleteByKeyInTx();
        DBManager.getDaoSession(context).getUnlockLogBeanDao();
    }

    public static void deleteData(Context context, List<Long> ids) {
         DBManager.getDaoSession(context).getUnlockLogBeanDao().deleteByKeyInTx(ids);
    }

    /**
     * 根据开门类型查询符合条件的数据库数据集
     *
     * @param context  上下文
     * @param cardType 开门类型
     * @return 符合条件的数据库数据集
     */
    public static List<UnlockLogBean> queryData(Context context, int cardType) {
        UnlockLogBeanDao dao = DBManager.getDaoSession(context).getUnlockLogBeanDao();
        List<UnlockLogBean> unlockLogBeans = dao.queryBuilder().where(
                UnlockLogBeanDao.Properties.UnlockType.eq(cardType)).build().list();
        if (unlockLogBeans != null) {
            return unlockLogBeans;
        }
        return null;
    }

    /**
     * 查询所有数据
     *
     * @param context 上下文
     * @return 查询到数据库的所有数据
     */
    public static List<UnlockLogBean> queryAll(Context context) {
        QueryBuilder<UnlockLogBean> builder = DBManager.getDaoSession(context).
                getUnlockLogBeanDao().queryBuilder();
        return builder.build().list();
    }
}
