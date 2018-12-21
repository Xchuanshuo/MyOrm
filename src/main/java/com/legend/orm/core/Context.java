package com.legend.orm.core;

import com.legend.orm.core.crud.LegendBase;
import com.legend.orm.core.crud.LegendDB;
import com.legend.orm.core.interfaces.IEntity;

import java.sql.Connection;

/**
 * @author Legend
 * @data by on 18-12-11.
 * @description
 */
public class Context {

    private LegendDB db;
    private Connection connection;
    private Class<? extends IEntity> clazz;
    private LegendBase legendBase;
    private Object[] values;
    private boolean before;
    private Exception error;
    private long duration;

    private final static Object[] EMPTY_VALUES = new Object[]{};

    public static Context before(LegendDB db, Connection connection,
                                 Class<? extends IEntity> clazz,
                                 LegendBase legendBase, Object[] values) {
        Context context = new Context();
        context.db = db;
        context.connection = connection;
        context.clazz = clazz;
        context.legendBase = legendBase;
        context.values = values;
        context.before = true;
        return context;
    }

    public static Context after(LegendDB db, Connection connection, Class<? extends IEntity> clazz,
                                LegendBase legendBase, Object[] values, Exception error, long duration) {
        Context context = new Context();
        context.db = db;
        context.connection = connection;
        context.clazz = clazz;
        context.legendBase = legendBase;
        context.values = values;
        context.before = false;
        context.error = error;
        context.duration = duration;
        return context;
    }

    public Class<? extends IEntity> clazz() {
        return clazz;
    }

    public LegendBase q() {
        return legendBase;
    }

    public LegendDB db() {
        return db;
    }

    public Object[] values() {
        return values != null? values: EMPTY_VALUES;
    }

    public boolean before() {
        return before;
    }

    public boolean after() {
        return !before;
    }

    public Connection conn() {
        return connection;
    }

    public Exception error() {
        return error;
    }

    public Context error(Exception error) {
        this.error = error;
        return this;
    }

    public long duration() {
        return duration;
    }
}
