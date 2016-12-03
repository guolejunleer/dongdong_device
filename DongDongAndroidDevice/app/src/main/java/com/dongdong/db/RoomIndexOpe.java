package com.dongdong.db;

import android.content.Context;

import com.dongdong.db.entry.RoomIndexBean;
import com.dongdong.db.gen.RoomIndexBeanDao;
import com.dongdong.utils.DDLog;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * 操作平台门禁卡房号对应卡号索引的执行者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class RoomIndexOpe {

    /**
     * 添加一条数据至数据库
     *
     * @param context       上下文
     * @param roomIndexBean 数据
     */
    public static void insertData(Context context, RoomIndexBean roomIndexBean) {
        DBManager.getDaoSession(context).getRoomIndexBeanDao().insert(roomIndexBean);
    }

    /**
     * 将数据实体通过事务添加至数据库
     *
     * @param context 上下文
     * @param list    数据
     */
    public static void insertData(Context context, List<RoomIndexBean> list) {
        if (null == list || list.size() <= 0) {
            return;
        }
        DBManager.getDaoSession(context).getRoomIndexBeanDao().insertInTx(list);
    }

    /**
     * 添加数据至数据库，如果存在，将原来的数据覆盖
     *
     * @param context       上下文
     * @param roomIndexBean 数据
     */
    public static void saveData(Context context, RoomIndexBean roomIndexBean) {
        DBManager.getDaoSession(context).getRoomIndexBeanDao().save(roomIndexBean);
    }

    public static boolean updateRoomIndexByBean(Context context, RoomIndexBean roomIndexBean) {
        DBManager.getDaoSession(context).getRoomIndexBeanDao().update(roomIndexBean);
        return false;
    }

    /**
     * 通过Id删除卡
     *
     * @param context         上下文
     * @param roomIndexBeanId 数据ID
     */
    public static void deleteDataById(Context context, long roomIndexBeanId) {
        DBManager.getDaoSession(context).getRoomIndexBeanDao().deleteByKey(roomIndexBeanId);
    }

    /**
     * 通过房号Id删除房号和CardIndex的信息
     *
     * @param context 上下文
     * @param roomId  房号Id
     */
    public static void deleteDataByCardRoomId(Context context, int roomId) {
        RoomIndexBeanDao dao = DBManager.getDaoSession(context).getRoomIndexBeanDao();
//        RoomIndexBean roomIndexBean = dao.queryBuilder().where(
//                RoomIndexBeanDao.Properties.RoomId.eq(roomId)).build().unique();查询一张，多张会报错
//        if (roomIndexBean != null) {
//            dao.deleteByKey(roomIndexBean.getId());
//        }
        List<RoomIndexBean> beans = dao.queryBuilder().where(
                RoomIndexBeanDao.Properties.RoomId.eq(roomId)).build().list();
        if (beans != null) {
            int size = beans.size();
            for (int i = 0; i < size; i++) {
                dao.deleteByKey(beans.get(i).getId());
            }
        }
    }

    /**
     * 删除所有房号和CardIndex的信息
     *
     * @param context 上下文
     */
    public static void deleteAllData(Context context) {
        DBManager.getDaoSession(context).getRoomIndexBeanDao().deleteAll();
    }

    /**
     * 通过卡号查询卡
     *
     * @param context 上下文
     * @param roomId  房号Id
     * @return
     */
    public static RoomIndexBean queryDataByRoomId(Context context, int roomId) {
        RoomIndexBeanDao dao = DBManager.getDaoSession(context).getRoomIndexBeanDao();
        RoomIndexBean roomIndexBean = dao.queryBuilder().where(
                RoomIndexBeanDao.Properties.RoomId.eq(roomId)).build().unique();
        if (roomIndexBean != null) {
            return roomIndexBean;
        }
        return null;
    }

    /**
     * 通过卡号查询卡
     *
     * @param context 上下文
     * @param roomId  房号Id
     * @return
     */
    public static   List<RoomIndexBean> queryDataListByRoomId(Context context, int roomId) {
        RoomIndexBeanDao dao = DBManager.getDaoSession(context).getRoomIndexBeanDao();
        List<RoomIndexBean> roomIndexBean = dao.queryBuilder().where(
                RoomIndexBeanDao.Properties.RoomId.eq(roomId)).build().list();
        if (roomIndexBean != null) {
            return roomIndexBean;
        }
        return null;
    }

    /**
     * 查询所有数据
     *
     * @param context 上下文
     * @return 数据库所有数据
     */
    public static List<RoomIndexBean> queryAll(Context context) {
        QueryBuilder<RoomIndexBean> builder = DBManager.getDaoSession(context).getRoomIndexBeanDao().
                queryBuilder();
        return builder.build().list();
    }

}
