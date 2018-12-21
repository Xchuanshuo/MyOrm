package com.legend.orm.core.crud.handler;

import com.legend.orm.core.Executor;
import com.legend.orm.core.crud.LegendBase;
import com.legend.orm.core.crud.LegendDB;
import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.interfaces.IQueryOp;
import com.legend.orm.core.model.SelectParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Legend
 * @data by on 18-12-19.
 * @description
 */
public class QueryHandler {

    private Executor executor = Executor.getInstance();
    private LegendDB legendDB;
    List<LegendBase.OrderBy> orderByList;
    List<String> groupByList;
    LegendBase.Filterable having;
    int offset = 0, limit = 0;

    public QueryHandler(LegendDB legendDB) {
        this.legendDB = legendDB;
    }

    public <T extends IEntity> T get(Class<T> clazz, Object...ids) {
        return executor.get(clazz, ids);
    }

    public QueryHandler orderBy(String name, String direction) {
        if (orderByList == null) {
            orderByList = new ArrayList<>();
        }
        orderByList.add(new LegendBase.OrderBy(name, direction));
        return this;
    }

    public QueryHandler orderBy(String name) {
        return orderBy(name, null);
    }

    public QueryHandler groupBy(String...fs) {
        if (groupByList == null) {
            groupByList = new ArrayList<>();
        }
        groupByList.addAll(Arrays.asList(fs));
        return this;
    }

    public QueryHandler having(LegendBase.Filterable having) {
        this.having = having;
        return this;
    }

    public QueryHandler offset(int offset) {
        this.offset = offset;
        return this;
    }

    public QueryHandler limit(int limit) {
        this.limit = limit;
        return this;
    }

    public <T extends IEntity> List<T> find(Class<T> clazz) {
        return this.find(clazz, null, new Object[]{});
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, LegendBase.Filterable filter, Object...values) {
        return this.find(clazz, null, filter, values);
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, String suffix,
                                            LegendBase.Filterable filter, Object...values) {
        return this.find(clazz, buildParam(suffix, filter, values));
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, SelectParam param) {
        return executor.find(clazz, param);
    }

    public long count(Class<? extends IEntity> clazz) {
        return this.count(clazz, null);
    }

    public long count(Class<? extends IEntity> clazz, LegendBase.Filterable filter, Object...values) {
        return this.count(clazz, null, filter, values);
    }

    public long count(Class<? extends IEntity> clazz, String suffix,
                      LegendBase.Filterable filter, Object...values) {
        return this.count(clazz, buildParam(suffix, filter, values));
    }

    public long count(Class<? extends IEntity> clazz, SelectParam param) {
        return executor.count(legendDB, clazz, param);
    }

    public void any(Class<? extends IEntity> clazz, LegendBase legendBase, IQueryOp op, Object...values) {
        executor.any(clazz, legendDB, legendBase, op, values);
    }

    private SelectParam buildParam(String suffix, LegendBase.Filterable filter, Object...values) {
        SelectParam selectParam = SelectParam.builder().offset(offset).limit(limit)
                .filter(filter).suffix(suffix)
                .valueList(Arrays.asList(values))
                .having(having).build();
        if (orderByList != null) {
            selectParam.setOrderByList(new ArrayList<>(orderByList));
            orderByList.clear();
        }
        if (groupByList != null) {
            selectParam.setGroupByList(new ArrayList<>(groupByList));
            groupByList.clear();
        }
        return selectParam;
    }
}
