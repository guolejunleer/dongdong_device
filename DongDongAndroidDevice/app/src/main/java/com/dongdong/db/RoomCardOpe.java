package com.dongdong.db;

import android.content.Context;

import com.dongdong.base.BaseApplication;
import com.dongdong.db.entry.RoomCardBean;
import com.dongdong.db.gen.RoomCardBeanDao;
import com.dongdong.socket.normal.APlatData;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * 操作平台门禁卡房号对应卡号的执行者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class RoomCardOpe {
    /**
     * 添加一条数据至数据库
     *
     * @param context      上下文
     * @param roomCardBean 数据
     */
    public static void insertData(Context context, RoomCardBean roomCardBean) {
        DBManager.getDaoSession(context).getRoomCardBeanDao().insert(roomCardBean);
    }

    /**
     * 将数据实体通过事务添加至数据库
     *
     * @param context 上下文
     * @param list    数据
     */
    public static void insertData(Context context, List<RoomCardBean> list) {
        if (null == list || list.size() <= 0) {
            return;
        }
        DBManager.getDaoSession(context).getRoomCardBeanDao().insertInTx(list);
    }

    /**
     * 添加数据至数据库，如果存在，将原来的数据覆盖
     *
     * @param context      上下文
     * @param roomCardBean 数据
     */
    public static void saveData(Context context, RoomCardBean roomCardBean) {
        DBManager.getDaoSession(context).getRoomCardBeanDao().save(roomCardBean);
    }

    /**
     * 通过Id删除卡
     *
     * @param context        上下文
     * @param roomCardBeanId 数据ID
     */
    public static void deleteDataByRoomCardBeanId(Context context, long roomCardBeanId) {
        DBManager.getDaoSession(context).getRoomCardBeanDao().deleteByKey(roomCardBeanId);
    }

    /**
     * 通过卡号删除卡
     *
     * @param context 上下文
     * @param roomId  房号Id
     */
    public static void deleteDataByRoomId(Context context, int roomId) {
        RoomCardBeanDao dao = DBManager.getDaoSession(context).getRoomCardBeanDao();
//        RoomCardBean roomCardBeanId = dao.queryBuilder().where(
//                RoomCardBeanDao.Properties.RoomId.eq(roomId)).build().unique();
//        if (roomCardBeanId != null) {
//            dao.deleteByKey(roomCardBeanId.getId());
//        }

        List<RoomCardBean> beens = dao.queryBuilder().where(
                RoomCardBeanDao.Properties.RoomId.eq(roomId)).list();
        if (beens != null) {
            int count = beens.size();
            for (int i = 0; i < count; i++) {
                dao.deleteByKey(beens.get(i).getId());
            }
        }
    }

    public static void deleteDataByBean(Context context, RoomCardBean roomCardBean) {
        RoomCardBeanDao dao = DBManager.getDaoSession(context).getRoomCardBeanDao();
        List<RoomCardBean> beens = dao.queryBuilder().where(
                RoomCardBeanDao.Properties.RoomId.eq(roomCardBean.getRoomId()),
                RoomCardBeanDao.Properties.RoomId.eq(roomCardBean.getCardNum())).list();
        APlatData.debugLog("deleteDataByBean beens:"
                + beens.size() + ",beens:" + beens);
        if (beens != null) {
            int count = beens.size();
            for (int i = 0; i < count; i++) {
                dao.deleteByKey(beens.get(i).getId());
            }
        }
    }

    /**
     * 通过卡号查询卡
     *
     * @param context 上下文
     * @param cardNum 卡号
     * @return
     */
    public static List<RoomCardBean> queryDataByCardNum(Context context, String cardNum) {
        RoomCardBeanDao dao = DBManager.getDaoSession(context).getRoomCardBeanDao();
        List<RoomCardBean> roomCardBeanId = dao.queryBuilder().where(
                RoomCardBeanDao.Properties.CardNum.eq(cardNum)).build().list();

        if (roomCardBeanId != null && !roomCardBeanId.isEmpty()) {
            return roomCardBeanId;
        }
        return null;
    }

    /**
     * 通过房号Id查询卡
     *
     * @param context 上下文
     * @param roomId  房号Id
     * @return
     */
    public static RoomCardBean queryDataByRoomId(Context context, int roomId) {
        RoomCardBeanDao dao = DBManager.getDaoSession(context).getRoomCardBeanDao();
        RoomCardBean roomCardBeanId = dao.queryBuilder().where(
                RoomCardBeanDao.Properties.RoomId.eq(roomId)).build().unique();

        if (roomCardBeanId != null) {
            return roomCardBeanId;
        }
        return null;
    }

    /**
     * 通过房号Id查询卡
     *
     * @param context 上下文
     * @param roomId  房号Id
     * @return
     */
    public static List<RoomCardBean> queryDataListByRoomId(Context context, int roomId) {
        RoomCardBeanDao dao = DBManager.getDaoSession(context).getRoomCardBeanDao();
        List<RoomCardBean> list = dao.queryBuilder().where(
                RoomCardBeanDao.Properties.RoomId.eq(roomId)).build().list();
        if (list != null) {
            return list;
        }
        return null;
    }

    /**
     * 查询所有数据
     *
     * @param context 上下文
     * @return 数据库所有数据
     */
    public static List<RoomCardBean> queryAll(Context context) {
        QueryBuilder<RoomCardBean> builder = DBManager.getDaoSession(context).getRoomCardBeanDao().
                queryBuilder();
        return builder.build().list();
    }

    public static void deleteAllData(BaseApplication context) {
        DBManager.getDaoSession(context).getRoomCardBeanDao().deleteAll();
    }
}
