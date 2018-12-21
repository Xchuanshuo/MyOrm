package com.legend.orm.core.crud;

import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.interfaces.IGridable;
import com.legend.orm.core.listener.IEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Legend
 * @data by on 18-12-14.
 * @description
 */
public abstract class GridDB<D extends LegendDB> {

    private D[] dbs;
    private Map<Class<? extends IEntity>, IGridable<? extends IEntity>> gridables = new HashMap<>();

    public GridDB(D[] dbs) {
        this.dbs = dbs;
    }

    public int size() {
        return dbs.length;
    }

    public <T extends IEntity> GridDB<D> gridWith(Class<T> clazz, IGridable<T> gridable) {
        this.gridables.put(clazz, gridable);
        return this;
    }

    public D selectIndex(int idx) {
        return dbs[idx];
    }

    public <T extends IEntity> D select(T t) {
        IGridable gridable = gridables.get(t.getClass());
        int idx = gridable.select(dbs.length, t);
        return dbs[idx];
    }

    public <T extends IEntity> D select(Class<T> clazz, Object... params) {
        IGridable gridable = gridables.get(clazz);
        int idx = gridable.select(dbs.length, params);
        return dbs[idx];
    }

    public GridDB<D> on(IEventListener listener) {
        for (D db: dbs) {
            db.listenerHandler.on(listener);
        }
        return this;
    }

    public GridDB<D> off(IEventListener listener) {
        for (D db: dbs) {
            db.listenerHandler.off(listener);
        }
        return this;
    }

    public GridDB<D> once(IEventListener listener) {
        for (D db: dbs) {
            db.listenerHandler.scope(listener);
        }
        return this;
    }

    public void create(Class<? extends IEntity> clazz, String suffix) {
        for (LegendDB db: dbs) {
            db.create(clazz, suffix);
        }
    }

    public void drop(Class<? extends IEntity> clazz, String suffix) {
        for (D db: dbs) {
            db.create(clazz, suffix);
        }
    }

    public void truncate(Class<? extends IEntity> clazz, String suffix) {
        for (LegendDB db: dbs) {
            db.truncate(clazz, suffix);
        }
    }

    public <T extends IEntity> long count(Class<T> clazz, int dbIndex, String suffix) {
        LegendDB db = selectIndex(dbIndex);
        return db.count(clazz, suffix);
    }

    public <T extends IEntity> T get(Class<T> clazz, Object...ids) {
        LegendDB db = select(clazz, ids);
        return db.get(clazz, ids);
    }

    public <T extends IEntity> boolean insert(T t) {
        LegendDB db = select(t);
        return db.insert(t);
    }

    public <T extends IEntity> int update(T t) {
        LegendDB db = select(t);
        return db.update(t);
    }

    public <T extends IEntity> int delete(T t) {
        LegendDB db = select(t);
        return db.delete(t);
    }

    public abstract void registerGridables();
}
