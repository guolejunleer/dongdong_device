package com.dongdong.db.gen;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.dongdong.db.entry.BulletinBean;
import com.dongdong.db.entry.CardBean;
import com.dongdong.db.entry.RoomCardBean;
import com.dongdong.db.entry.RoomIndexBean;
import com.dongdong.db.entry.UnlockLogBean;

import com.dongdong.db.gen.BulletinBeanDao;
import com.dongdong.db.gen.CardBeanDao;
import com.dongdong.db.gen.RoomCardBeanDao;
import com.dongdong.db.gen.RoomIndexBeanDao;
import com.dongdong.db.gen.UnlockLogBeanDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig bulletinBeanDaoConfig;
    private final DaoConfig cardBeanDaoConfig;
    private final DaoConfig roomCardBeanDaoConfig;
    private final DaoConfig roomIndexBeanDaoConfig;
    private final DaoConfig unlockLogBeanDaoConfig;

    private final BulletinBeanDao bulletinBeanDao;
    private final CardBeanDao cardBeanDao;
    private final RoomCardBeanDao roomCardBeanDao;
    private final RoomIndexBeanDao roomIndexBeanDao;
    private final UnlockLogBeanDao unlockLogBeanDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        bulletinBeanDaoConfig = daoConfigMap.get(BulletinBeanDao.class).clone();
        bulletinBeanDaoConfig.initIdentityScope(type);

        cardBeanDaoConfig = daoConfigMap.get(CardBeanDao.class).clone();
        cardBeanDaoConfig.initIdentityScope(type);

        roomCardBeanDaoConfig = daoConfigMap.get(RoomCardBeanDao.class).clone();
        roomCardBeanDaoConfig.initIdentityScope(type);

        roomIndexBeanDaoConfig = daoConfigMap.get(RoomIndexBeanDao.class).clone();
        roomIndexBeanDaoConfig.initIdentityScope(type);

        unlockLogBeanDaoConfig = daoConfigMap.get(UnlockLogBeanDao.class).clone();
        unlockLogBeanDaoConfig.initIdentityScope(type);

        bulletinBeanDao = new BulletinBeanDao(bulletinBeanDaoConfig, this);
        cardBeanDao = new CardBeanDao(cardBeanDaoConfig, this);
        roomCardBeanDao = new RoomCardBeanDao(roomCardBeanDaoConfig, this);
        roomIndexBeanDao = new RoomIndexBeanDao(roomIndexBeanDaoConfig, this);
        unlockLogBeanDao = new UnlockLogBeanDao(unlockLogBeanDaoConfig, this);

        registerDao(BulletinBean.class, bulletinBeanDao);
        registerDao(CardBean.class, cardBeanDao);
        registerDao(RoomCardBean.class, roomCardBeanDao);
        registerDao(RoomIndexBean.class, roomIndexBeanDao);
        registerDao(UnlockLogBean.class, unlockLogBeanDao);
    }
    
    public void clear() {
        bulletinBeanDaoConfig.clearIdentityScope();
        cardBeanDaoConfig.clearIdentityScope();
        roomCardBeanDaoConfig.clearIdentityScope();
        roomIndexBeanDaoConfig.clearIdentityScope();
        unlockLogBeanDaoConfig.clearIdentityScope();
    }

    public BulletinBeanDao getBulletinBeanDao() {
        return bulletinBeanDao;
    }

    public CardBeanDao getCardBeanDao() {
        return cardBeanDao;
    }

    public RoomCardBeanDao getRoomCardBeanDao() {
        return roomCardBeanDao;
    }

    public RoomIndexBeanDao getRoomIndexBeanDao() {
        return roomIndexBeanDao;
    }

    public UnlockLogBeanDao getUnlockLogBeanDao() {
        return unlockLogBeanDao;
    }

}
