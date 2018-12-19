package com.legend.orm.core;

import com.legend.orm.core.interfaces.*;
import com.legend.orm.core.listener.ListenerHandler;
import com.legend.orm.core.model.SelectParam;
import com.legend.orm.core.utils.MetaUtils;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * @author Legend
 * @data by on 18-12-11.
 * @description
 */
public abstract class LegendDB {

    private Operator operator;
    private Executor executor;
    private List<LegendBase.OrderBy> orderByList;
    private List<String> groupByList;
    private LegendBase.Filterable having;
    private int offset = 0, limit = 0;
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

    static public class Holder<T> {
        T value;
    }

    public void create(Class<? extends IEntity> clazz) {
       create(clazz, null);
    }

    public void create(Class<? extends IEntity> clazz, String suffix) {
        operator.withinTx(conn -> executor.create(conn, clazz, suffix));
    }

    public void drop(Class<? extends IEntity> clazz) {
        drop(clazz, null);
    }

    public void drop(Class<? extends IEntity> clazz, String suffix) {
        operator.withinTx(conn -> executor.drop(conn, clazz, suffix));
    }

    public void truncate(Class<? extends IEntity> clazz) {
        truncate(clazz, null);
    }

    public void truncate(Class<? extends IEntity> clazz, String suffix) {
        operator.withinTx(conn -> executor.truncate(conn, clazz, suffix));
    }

    public <T extends IEntity> T get(Class<T> clazz,Object...ids) {
        Holder<T> holder = new Holder<>();
        operator.withinTx(conn -> holder.value = executor.get(conn, clazz, ids), false);
        return holder.value;
    }

    public <T extends IEntity> List<T> find(Class<T> clazz) {
        return this.find(clazz, null, new Object[]{});
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, String suffix) {
        return this.find(clazz, suffix, null);
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, LegendBase.Filterable filter, Object...values) {
        return this.find(clazz, null, filter, values);
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, String suffix,
                                            LegendBase.Filterable filter, Object...values) {
        Holder<List<T>> holder = new Holder<>();
        operator.withinTx(conn -> holder.value=find(conn, clazz, suffix, filter, values), false);
        return holder.value;
    }

    public <T extends IEntity>  List<T> find(Class<T> clazz, SelectParam param) {
        Holder<List<T>> holder = new Holder<>();
        operator.withinTx(conn -> holder.value = executor.find(conn, clazz, param));
        return holder.value;
    }

    public <T extends IEntity>  List<T> find(Connection conn, Class<T> clazz, String suffix,
                                             LegendBase.Filterable filter,
                                             Object...values) {
        return executor.find(conn, clazz, buildParam(suffix, filter, values));
    }

    public LegendDB orderBy(String name, String direction) {
        if (orderByList == null) {
            orderByList = new ArrayList<>();
        }
        orderByList.add(new LegendBase.OrderBy(name, direction));
        return this;
    }

    public LegendDB orderBy(String name) {
        return orderBy(name, null);
    }

    public LegendDB groupBy(String...fs) {
        if (groupByList == null) {
            groupByList = new ArrayList<>();
        }
        groupByList.addAll(Arrays.asList(fs));
        return this;
    }

    public LegendDB having(LegendBase.Filterable having) {
        this.having = having;
        return this;
    }

    public LegendDB offset(int offset) {
        this.offset = offset;
        return this;
    }

    public LegendDB limit(int limit) {
        this.limit = limit;
        return this;
    }

    public long count(Class<? extends IEntity> clazz) {
        String suffix = null;
        LegendBase.Filterable filter = null;
        return count(clazz, suffix, filter);
    }

    public long count(Class<? extends IEntity> clazz, LegendBase.Filterable filter, Object...values) {
        return count(clazz, null, filter, values);
    }

    public long count(Class<? extends IEntity> clazz, String suffix) {
        return count(clazz, suffix, null);
    }

    public long count(Class<? extends IEntity> clazz, String suffix,
                      LegendBase.Filterable filter, Object...values) {
        Holder<Long> holder = new Holder<>();
        operator.withinTx(conn -> holder.value = executor.count(this, clazz
                , buildParam(suffix, filter, values)), false);
        return holder.value;
    }

    public void any(Class<? extends IEntity> clazz, LegendBase legendBase, IQueryOp op, Object...values) {
        operator.withinTx(conn -> executor.any(this, clazz, legendBase, op, values), false);
    }

    public <T extends IEntity> int delete(T t) {
        return delete(t.getClass(), MetaUtils.ids(t));
    }

    public int delete(Class<? extends IEntity> clazz, Object...ids) {
        Holder<Integer> holder = new Holder<>();
        operator.withinTx(conn -> holder.value = executor.delete(this, clazz, ids));
        return holder.value;
    }

    public <T extends IEntity> int update(T t) {
        return update(t.getClass(), MetaUtils.values(t), MetaUtils.ids(t));
    }

    // 带where条件多条更新
    public int update(Class<? extends IEntity> clazz, Map<String, Object> values
            , LegendBase.Filterable filter, Object...filterValues) {
        LegendDB.Holder<Integer> holder = new LegendDB.Holder<>();
        Map<String, Object> valueModify = MetaUtils.updateDispose(clazz, values);
        operator.withinTx(conn -> holder.value = executor.update(this, clazz, valueModify,
                filter, filterValues));
        return holder.value;
    }

    // 根据主键列id进行单条更新
    public int update(Class<? extends IEntity> clazz, Map<String, Object> values, Object...ids) {
        return update(clazz, values, null, ids);
    }

    public <T extends IEntity> boolean insert(T t) {
        Holder<Integer> lastInsertId = null;
        Field lastInsertField = null;
        Map<String, Object> values = MetaUtils.insertDispose(t, lastInsertId, lastInsertField);
        boolean res = executor.insert(this, t, values, lastInsertId);
        if (res && lastInsertId!=null && lastInsertId.value != null) {
            try {
                lastInsertField.set(t, lastInsertId.value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return res;
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
