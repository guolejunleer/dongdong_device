package com.dongdong.db;

import android.content.Context;
import android.text.TextUtils;

import com.dongdong.db.entry.UnlockLogBean;
import com.dongdong.db.gen.UnlockLogBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;

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
     * 修改一条数据
     *
     * @param context 上下文
     * @param data    数据
     */
    public static void updateData(Context context, UnlockLogBean data) {
        DBManager.getDaoSession(context).getUnlockLogBeanDao().update(data);
    }

    public static void updateDataByUnLockList(Context context,
                                              List<UnlockLogBean> unlockLogBeanList) {
        DBManager.getDaoSession(context).getUnlockLogBeanDao().updateInTx(unlockLogBeanList);
    }

    /**
     * 删除一条数据
     *
     * @param context  上下文
     * @param unlockId 数据ID
     */
    public static void deleteDataByUnlockId(Context context, long unlockId) {
        DBManager.getDaoSession(context).getUnlockLogBeanDao().deleteByKey(unlockId);
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
     * 根据上传状态查询符合条件的数据库数据集
     *
     * @param context 上下文
     * @param unlockState  上传状态
     * @return 符合条件的数据库数据集
     */
    public static List<UnlockLogBean> queryDataByUnlockState(Context context, int unlockState) {
        UnlockLogBeanDao dao = DBManager.getDaoSession(context).getUnlockLogBeanDao();
        List<UnlockLogBean> unlockLogBeans = dao.queryBuilder().where(
                UnlockLogBeanDao.Properties.Upload.eq(unlockState)).build().list();
        if (unlockLogBeans != null) {
            return unlockLogBeans;
        }
        return null;
    }

    /**
     * 通过开门记录索引查询数据
     *
     * @param context  上下文
     * @param unLockId 开门记录Id
     * @return 符合条件的数据库数据集
     */
    public static UnlockLogBean queryDataByUnLockId(Context context, int unLockId) {
        UnlockLogBeanDao dao = DBManager.getDaoSession(context).getUnlockLogBeanDao();
        UnlockLogBean unlockLogBean = dao.queryBuilder().where(
                UnlockLogBeanDao.Properties.Id.eq(unLockId)).build().unique();
        if (unlockLogBean != null) {
            return unlockLogBean;
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

    /**
     * 通过时间查询所有数据
     *
     * @param context 上下文
     * @return 查询到数据库的所有数据
     */
    public static List<UnlockLogBean> queryByTime(Context context, int beginTime, int endTime) {
        UnlockLogBeanDao dao = DBManager.getDaoSession(context).getUnlockLogBeanDao();
        List<UnlockLogBean> unlockLogBeans = dao.queryBuilder().where(
                UnlockLogBeanDao.Properties.UnlockTime.between(beginTime, endTime)).build().list();
        if (unlockLogBeans != null) {
            return unlockLogBeans;
        }
        return null;
    }

    public static List<UnlockLogBean> queryDataByConditions(Context context, int beginTime,
                                                            int endTime, String number,
                                                            String roomNum, int type, int upLoad) {
        UnlockLogBeanDao dao = DBManager.getDaoSession(context).getUnlockLogBeanDao();
        QueryBuilder<UnlockLogBean> qb = dao.queryBuilder();

        if (beginTime != 0 && endTime != 0) {
            qb.where(UnlockLogBeanDao.Properties.UnlockTime.between(beginTime, endTime));
        }
        if (!TextUtils.isEmpty(number)) {
            qb.where(UnlockLogBeanDao.Properties.CardOrPhoneNum.eq(number));
        }
        if (!TextUtils.isEmpty(roomNum)) {
            qb.where(UnlockLogBeanDao.Properties.RoomNum.eq(roomNum));
        }
        if (type != 0) {
            qb.where(UnlockLogBeanDao.Properties.UnlockType.eq(type));
        }
        if (upLoad < 2) {
            qb.where(UnlockLogBeanDao.Properties.Upload.eq(upLoad));
        }

        List<UnlockLogBean> unlockLogBeans = qb.build().list();
        if (unlockLogBeans != null) {
            return unlockLogBeans;
        }
        return null;
    }
}
