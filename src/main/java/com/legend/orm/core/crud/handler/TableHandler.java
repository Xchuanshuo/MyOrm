package com.legend.orm.core.crud.handler;

import com.legend.orm.core.Executor;
import com.legend.orm.core.crud.LegendDB;
import com.legend.orm.core.interfaces.IEntity;

/**
 * @author Legend
 * @data by on 18-12-21.
 * @description
 */
public class TableHandler {

    private Executor executor = Executor.getInstance();
    private LegendDB legendDB;

    public void create(Class<? extends IEntity> clazz, String suffix) {
        executor.create(clazz, suffix);
    }

    public void drop(Class<? extends IEntity> clazz, String suffix) {
        executor.drop(clazz, suffix);
    }

    public void truncate(Class<? extends IEntity> clazz, String suffix) {
        executor.truncate(clazz, suffix);
    }
}
