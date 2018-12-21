package com.legend.orm.core.crud.handler;

import com.legend.orm.core.Executor;
import com.legend.orm.core.crud.LegendDB;
import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.model.Holder;
import com.legend.orm.core.utils.MetaUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Legend
 * @data by on 18-12-21.
 * @description
 */
public class InsertHandler {

    private Executor executor = Executor.getInstance();
    private LegendDB legendDB;

    public InsertHandler(LegendDB db) {
        this.legendDB = db;
    }

    public <T extends IEntity> boolean insert(T t) {
        Holder<Integer> lastInsertId = null;
        Field lastInsertField = null;
        Map<String, Object> values = MetaUtils.insertDispose(t, lastInsertId, lastInsertField);
        boolean res = executor.insert(legendDB, t, values, lastInsertId);
        if (res && lastInsertId != null && lastInsertId.value != null) {
            try {
                lastInsertField.set(t, lastInsertId.value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return res;
    }
}
