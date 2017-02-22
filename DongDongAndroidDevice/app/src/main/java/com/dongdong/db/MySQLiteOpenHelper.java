package com.dongdong.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.dongdong.db.gen.BulletinBeanDao;
import com.dongdong.db.gen.CardBeanDao;
import com.dongdong.db.gen.DaoMaster;
import com.dongdong.db.gen.RoomCardBeanDao;
import com.dongdong.db.gen.RoomIndexBeanDao;
import com.dongdong.db.gen.UnlockLogBeanDao;
import com.github.yuweiguocn.library.greendao.MigrationHelper;

/**
 * Created by Growth on 2016/3/3.
 */
public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, BulletinBeanDao.class, CardBeanDao.class,
                RoomCardBeanDao.class, RoomIndexBeanDao.class, UnlockLogBeanDao.class);
    }
}
