package com.dongdong.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.dongdong.db.gen.DaoMaster;
import com.dongdong.db.gen.DaoSession;

/**
 * GreenDAO的管理者DBManager
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class DBManager {
    // 是否加密
    public static final boolean ENCRYPTED = true;

    private static final String DB_NAME = "dongdong.db";
    private static DBManager mDbManager;
    private static DaoMaster.DevOpenHelper mDevOpenHelper;
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;

    private Context mContext;

    private DBManager(Context context) {
        this.mContext = context;
        // 初始化数据库信息
        mDevOpenHelper = new DaoMaster.DevOpenHelper(context, DB_NAME);
        getDaoMaster(context);
        getDaoSession(context);
    }

    public static DBManager getInstance(Context context) {
        if (null == mDbManager) {
            synchronized (DBManager.class) {
                if (null == mDbManager) {
                    mDbManager = new DBManager(context);
                }
            }
        }
        return mDbManager;
    }

    /**
     * 获取可读数据库
     *
     * @param context 上下文
     * @return 可读数据库引用
     */
    public static SQLiteDatabase getReadableDatabase(Context context) {
        if (null == mDevOpenHelper) {
            getInstance(context);
        }
        return mDevOpenHelper.getReadableDatabase();
    }

    /**
     * 获取可写数据库
     *
     * @param context 上下文
     * @return 可写数据库引用
     */
    public static SQLiteDatabase getWritableDatabase(Context context) {
        if (null == mDevOpenHelper) {
            getInstance(context);
        }
        return mDevOpenHelper.getWritableDatabase();
    }

    /**
     * 获取DaoMaster
     *
     * @param context 上下文
     * @return DaoMaster
     */
    public static DaoMaster getDaoMaster(Context context) {
        if (null == mDaoMaster) {
            synchronized (DBManager.class) {
                if (null == mDaoMaster) {
                    mDaoMaster = new DaoMaster(getWritableDatabase(context));
                }
            }
        }
        return mDaoMaster;
    }

    /**
     * 获取DaoSession
     *
     * @param context 上下文
     * @return DaoSession
     */
    public static DaoSession getDaoSession(Context context) {
        if (null == mDaoSession) {
            synchronized (DBManager.class) {
                if (null == mDaoSession)
                    mDaoSession = getDaoMaster(context).newSession();
            }
        }
        return mDaoSession;
    }
}

