package com.dongdong.db.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dongdong.db.entry.RoomCardBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ROOM_CARD_BEAN".
*/
public class RoomCardBeanDao extends AbstractDao<RoomCardBean, Long> {

    public static final String TABLENAME = "ROOM_CARD_BEAN";

    /**
     * Properties of entity RoomCardBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property RoomId = new Property(1, int.class, "roomId", false, "ROOM_ID");
        public final static Property CardNum = new Property(2, String.class, "cardNum", false, "CARD_NUM");
    }


    public RoomCardBeanDao(DaoConfig config) {
        super(config);
    }
    
    public RoomCardBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ROOM_CARD_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"ROOM_ID\" INTEGER NOT NULL ," + // 1: roomId
                "\"CARD_NUM\" TEXT);"); // 2: cardNum
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ROOM_CARD_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, RoomCardBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getRoomId());
 
        String cardNum = entity.getCardNum();
        if (cardNum != null) {
            stmt.bindString(3, cardNum);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, RoomCardBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getRoomId());
 
        String cardNum = entity.getCardNum();
        if (cardNum != null) {
            stmt.bindString(3, cardNum);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public RoomCardBean readEntity(Cursor cursor, int offset) {
        RoomCardBean entity = new RoomCardBean();
        readEntity(cursor, entity, offset);
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, RoomCardBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setRoomId(cursor.getInt(offset + 1));
        entity.setCardNum(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(RoomCardBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(RoomCardBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(RoomCardBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
