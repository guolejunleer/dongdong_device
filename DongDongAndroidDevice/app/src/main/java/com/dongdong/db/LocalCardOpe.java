package com.dongdong.db;

import android.content.Context;

import com.dongdong.db.entry.CardBean;
import com.dongdong.db.gen.CardBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * 操作门禁卡的执行者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class LocalCardOpe {

    /**
     * 添加一条数据至数据库
     *
     * @param context  上下文
     * @param cardBean 数据
     */
    public static void insertData(Context context, CardBean cardBean) {
        DBManager.getDaoSession(context).getCardBeanDao().insert(cardBean);
    }

    /**
     * 将数据实体通过事务添加至数据库
     *
     * @param context 上下文
     * @param list    数据
     */
    public static void insertData(Context context, List<CardBean> list) {
        if (null == list || list.size() <= 0) {
            return;
        }
        DBManager.getDaoSession(context).getCardBeanDao().insertInTx(list);
    }

    /**
     * 添加数据至数据库，如果存在，将原来的数据覆盖
     *
     * @param context  上下文
     * @param cardBean 数据
     */
    public static void saveData(Context context, CardBean cardBean) {
        DBManager.getDaoSession(context).getCardBeanDao().save(cardBean);
    }

    /**
     * 通过Id删除卡
     *
     * @param context    上下文
     * @param cardBeanId 数据ID
     */
    public static void deleteDataById(Context context, long cardBeanId) {
        DBManager.getDaoSession(context).getCardBeanDao().deleteByKey(cardBeanId);
    }

    /**
     * 通过卡号删除卡
     *
     * @param context 上下文
     * @param cardNum 卡号
     */
    public static void deleteDataByCardNum(Context context, String cardNum) {
        CardBeanDao dao = DBManager.getDaoSession(context).getCardBeanDao();
        CardBean cardBean = dao.queryBuilder().where(CardBeanDao.Properties.CardNum.eq(cardNum)).build().unique();
        if (cardBean != null) {
            dao.deleteByKey(cardBean.getId());
        }
    }

    /**
     * 通过卡号查询卡
     *
     * @param context 上下文
     * @param cardNum 卡号
     * @return
     */
    public static CardBean queryDataByCardNum(Context context, String cardNum) {
        CardBeanDao dao = DBManager.getDaoSession(context).getCardBeanDao();
        CardBean cardBean = dao.queryBuilder().where(CardBeanDao.Properties.CardNum.eq(cardNum)).build().unique();
        if (cardBean != null) {
            return cardBean;
        }
        return null;
    }


    /**
     * 通过卡号查询卡
     *
     * @param context 上下文
     * @param cardNum 卡号
     * @return
     */
    public static List<CardBean> queryDatasByCardNum(Context context, String cardNum) {
        CardBeanDao dao = DBManager.getDaoSession(context).getCardBeanDao();
        List<CardBean> cardBean = dao.queryBuilder().where(CardBeanDao.Properties.CardNum.eq(cardNum)).build().list();
        if (cardBean != null && !cardBean.isEmpty()) {
            return cardBean;
        }
        return null;
    }

    /**
     * 查询所有数据
     *
     * @param context 上下文
     * @return 数据库所有数据
     */
    public static List<CardBean> queryAll(Context context) {
        QueryBuilder<CardBean> builder = DBManager.getDaoSession(context).getCardBeanDao().
                queryBuilder();
        return builder.build().list();
    }
}
