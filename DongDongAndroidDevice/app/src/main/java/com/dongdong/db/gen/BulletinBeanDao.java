package com.dongdong.db.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dongdong.db.entry.BulletinBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "BULLETIN_BEAN".
*/
public class BulletinBeanDao extends AbstractDao<BulletinBean, Long> {

    public static final String TABLENAME = "BULLETIN_BEAN";

    /**
     * Properties of entity BulletinBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Notice = new Property(1, String.class, "notice", false, "NOTICE");
        public final static Property Created = new Property(2, String.class, "created", false, "CREATED");
        public final static Property Title = new Property(3, String.class, "title", false, "TITLE");
        public final static Property Noticeid = new Property(4, String.class, "noticeid", false, "NOTICEID");
    }


    public BulletinBeanDao(DaoConfig config) {
        super(config);
    }
    
    public BulletinBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"BULLETIN_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"NOTICE\" TEXT," + // 1: notice
                "\"CREATED\" TEXT," + // 2: created
                "\"TITLE\" TEXT," + // 3: title
                "\"NOTICEID\" TEXT);"); // 4: noticeid
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BULLETIN_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, BulletinBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String notice = entity.getNotice();
        if (notice != null) {
            stmt.bindString(2, notice);
        }
 
        String created = entity.getCreated();
        if (created != null) {
            stmt.bindString(3, created);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(4, title);
        }
 
        String noticeid = entity.getNoticeid();
        if (noticeid != null) {
            stmt.bindString(5, noticeid);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, BulletinBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String notice = entity.getNotice();
        if (notice != null) {
            stmt.bindString(2, notice);
        }
 
        String created = entity.getCreated();
        if (created != null) {
            stmt.bindString(3, created);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(4, title);
        }
 
        String noticeid = entity.getNoticeid();
        if (noticeid != null) {
            stmt.bindString(5, noticeid);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public BulletinBean readEntity(Cursor cursor, int offset) {
        BulletinBean entity = new BulletinBean();
        readEntity(cursor, entity, offset);
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, BulletinBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setNotice(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setCreated(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTitle(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setNoticeid(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(BulletinBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(BulletinBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(BulletinBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
