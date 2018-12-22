package com.legend.orm.core.crud;

import com.legend.orm.core.Executor;
import com.legend.orm.core.Operator;
import com.legend.orm.core.crud.handler.InsertHandler;
import com.legend.orm.core.crud.handler.QueryHandler;
import com.legend.orm.core.crud.handler.TableHandler;
import com.legend.orm.core.crud.handler.UpdateHandler;
import com.legend.orm.core.interfaces.*;
import com.legend.orm.core.listener.ListenerHandler;
import com.legend.orm.core.model.SelectParam;
import com.legend.orm.core.utils.MetaUtils;

import java.util.*;

/**
 * @author Legend
 * @data by on 18-12-11.
 * @description
 */
public abstract class LegendDB {

    private Operator operator;
    private Executor executor;
    ListenerHandler listenerHandler;
    private String name;

    public LegendDB(String name) {
        this(name, new ListenerHandler());
    }

    public LegendDB(String name, ListenerHandler listenerHandler) {
        this.name = name;
        this.listenerHandler = listenerHandler;
        this.executor = Executor.getInstance();
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String name() {
        return name;
    }

    public ListenerHandler getListenerHandler() {
        return listenerHandler;
    }

    public void create(Class<? extends IEntity> clazz) {
       this.create(clazz, null);
    }

    public void create(Class<? extends IEntity> clazz, String suffix) {
        TableHandler tableHandler = new TableHandler();
        tableHandler.create(clazz, suffix);
    }

    public void drop(Class<? extends IEntity> clazz) {
        this.drop(clazz, null);
    }

    public void drop(Class<? extends IEntity> clazz, String suffix) {
        TableHandler tableHandler = new TableHandler();
        tableHandler.drop(clazz, suffix);
    }

    public void truncate(Class<? extends IEntity> clazz) {
        this.truncate(clazz, null);
    }

    public void truncate(Class<? extends IEntity> clazz, String suffix) {
        TableHandler tableHandler = new TableHandler();
        tableHandler.truncate(clazz, suffix);
    }

    public <T extends IEntity> T get(Class<T> clazz, Object...ids) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.get(clazz, ids);
    }

    public <T extends IEntity> List<T> find(Class<T> clazz) {
        return this.find(clazz, null, new Object[]{});
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, String suffix) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.find(clazz, suffix, null);
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, LegendBase.Filterable filter, Object...values) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.find(clazz, null, filter, values);
    }

    public QueryHandler orderBy(String name, String direction) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.orderBy(name, direction);
    }

    public QueryHandler orderBy(String name) {
        return orderBy(name, null);
    }

    public QueryHandler groupBy(String...fs) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.groupBy(fs);
    }

    public QueryHandler having(LegendBase.Filterable having) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.having(having);
    }

    public QueryHandler offset(int offset) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.offset(offset);
    }

    public QueryHandler limit(int limit) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.limit(limit);
    }

    public long count(Class<? extends IEntity> clazz) {
        return this.count(clazz, "");
    }

    public long count(Class<? extends IEntity> clazz, LegendBase.Filterable filter, Object...values) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.count(clazz, null, filter, values);
    }

    public long count(Class<? extends IEntity> clazz, String suffix) {
        QueryHandler queryHandler = new QueryHandler(this);
        LegendBase.Filterable filter = null;
        return queryHandler.count(clazz, suffix, filter);
    }

    public long max(Class<? extends IEntity> clazz, String fieldStr) {
        return this.max(clazz, fieldStr, null);
    }

    public long max(Class<? extends IEntity> clazz, String fieldStr,
                    LegendBase.Filterable filter, Object...values) {
        return this.max(clazz, fieldStr, null, filter, values);
    }

    public long max(Class<? extends IEntity> clazz, String fieldStr, String suffix,
                    LegendBase.Filterable filter, Object...values) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.max(clazz, fieldStr, suffix, filter, values);
    }

    public long min(Class<? extends IEntity> clazz, String fieldStr) {
        return this.min(clazz, fieldStr, null);
    }

    public long min(Class<? extends IEntity> clazz, String fieldStr,
                    LegendBase.Filterable filter, Object...values) {
        return this.min(clazz, fieldStr, null, filter, values);
    }

    public long min(Class<? extends IEntity> clazz, String fieldStr, String suffix,
                    LegendBase.Filterable filter, Object...values) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.min(clazz, fieldStr, suffix, filter, values);
    }

    public long sum(Class<? extends IEntity> clazz, String fieldStr) {
        return this.sum(clazz, fieldStr, null);
    }

    public long sum(Class<? extends IEntity> clazz, String fieldStr,
                    LegendBase.Filterable filter, Object...values) {
        return this.sum(clazz, fieldStr, null, filter, values);
    }

    public long sum(Class<? extends IEntity> clazz, String fieldStr, String suffix,
                    LegendBase.Filterable filter, Object...values) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.sum(clazz, fieldStr, suffix, filter, values);
    }

    public long average(Class<? extends IEntity> clazz, String fieldStr) {
        return this.average(clazz, fieldStr, null);
    }

    public long average(Class<? extends IEntity> clazz, String fieldStr,
                    LegendBase.Filterable filter, Object...values) {
        return this.average(clazz, fieldStr, null, filter, values);
    }

    public long average(Class<? extends IEntity> clazz, String fieldStr, String suffix,
                    LegendBase.Filterable filter, Object...values) {
        QueryHandler queryHandler = new QueryHandler(this);
        return queryHandler.average(clazz, fieldStr, suffix, filter, values);
    }

    public void any(Class<? extends IEntity> clazz, LegendBase legendBase, IQueryOp op, Object...values) {
        QueryHandler queryHandler = new QueryHandler(this);
        queryHandler.any(clazz, legendBase, op, values);
    }

    public <T extends IEntity> int delete(T t) {
        return this.delete(t.getClass(), MetaUtils.ids(t));
    }

    public int delete(Class<? extends IEntity> clazz, Object...ids) {
        UpdateHandler updateHandler = new UpdateHandler(this);
        return updateHandler.delete(clazz, ids);
    }

    public <T extends IEntity> int update(T t) {
        return this.update(t.getClass(), MetaUtils.values(t), MetaUtils.ids(t));
    }

    // 带where条件多条更新
    public int update(Class<? extends IEntity> clazz, Map<String, Object> values
            , LegendBase.Filterable filter, Object...filterValues) {
        UpdateHandler updateHandler = new UpdateHandler(this);
        return updateHandler.update(clazz, values, filter, filterValues);
    }

    // 根据主键列id进行单条更新
    public int update(Class<? extends IEntity> clazz, Map<String, Object> values, Object...ids) {
        return this.update(clazz, values, null, ids);
    }

    public <T extends IEntity> boolean insert(T t) {
        InsertHandler insertHandler = new InsertHandler(this);
        return insertHandler.insert(t);
    }
}
