package com.legend.orm.core.crud.handler;

import com.legend.orm.core.Executor;
import com.legend.orm.core.crud.LegendBase;
import com.legend.orm.core.crud.LegendDB;
import com.legend.orm.core.interfaces.IEntity;

import java.util.Map;

/**
 * @author Legend
 * @data by on 18-12-21.
 * @description
 */
public class UpdateHandler {

    private Executor executor = Executor.getInstance();
    private LegendDB legendDB;

    public UpdateHandler(LegendDB legendDB) {
        this.legendDB = legendDB;
    }

    // 带where条件多条更新
    public int update(Class<? extends IEntity> clazz, Map<String, Object> values
            , LegendBase.Filterable filter, Object...filterValues) {
        return executor.update(legendDB, clazz, values, filter, filterValues);
    }

    public int delete(Class<? extends IEntity> clazz, Object...ids) {
        return executor.delete(legendDB, clazz, ids);
    }
}
