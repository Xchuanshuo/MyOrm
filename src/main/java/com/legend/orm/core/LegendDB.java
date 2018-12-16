package com.legend.orm.core;

import com.legend.orm.core.exception.LegendException;
import com.legend.orm.core.factory.DBConnectionFactory;
import com.legend.orm.core.interfaces.*;
import com.legend.orm.core.listener.ListenerHandler;
import com.legend.orm.core.model.ColumnInfo;
import com.legend.orm.core.model.Meta;
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

    private Executor executor;
    private List<LegendBase.OrderBy> orderByList;
    private List<String> groupByList;
    private int offset = 0, limit = 0;
    ListenerHandler listenerHandler;
    private String name;

    public LegendDB(String name) {
        this(name, new ListenerHandler());
    }

    public LegendDB(String name, ListenerHandler listenerHandler) {
        this.name = name;
        this.listenerHandler = listenerHandler;
    }

    public void setExecutor(Executor executor) {
        this.executor = new Executor(DBConnectionFactory.getInstance());
    }

    public String name() {
        return name;
    }

    public ListenerHandler getListenerHandler() {
        return listenerHandler;
    }

    static class Holder<T> {
        T value;
    }

    protected abstract Connection conn();

    public void create(Class<? extends IEntity> clazz) {
       create(clazz, null);
    }

    public void create(Class<? extends IEntity> clazz, String suffix) {
        executor.withinTx(conn -> create(conn, clazz, suffix));
    }

    public void create(Connection conn, Class<? extends IEntity> clazz,
                       String suffix) {
        executor.withinPrepare(conn, createSQL(clazz, suffix), PreparedStatement::execute);
    }


    private String createSQL(Class<? extends IEntity> clazz, String suffix) {
        Meta meta = MetaUtils.meta(clazz);
        LegendBase legendBase = LegendBase.create().table(meta.name, suffix);
        meta.columns.forEach((name, columnInfo) -> legendBase.column(columnInfo.name(), columnInfo.type(), columnInfo.autoIncrement()
                , columnInfo.nullable(), columnInfo.defaultValue()));
        meta.indices.indices().forEach(index -> legendBase.index(index.primaryKey(), index.unique(), index.columns()));
        meta.options.options().forEach(option -> legendBase.option(option.key(), option.value()));
        return legendBase.sql();
    }

    public void drop(Class<? extends IEntity> clazz) {
        drop(clazz, null);
    }

    public void drop(Class<? extends IEntity> clazz, String suffix) {
        executor.withinTx(conn -> drop(conn, clazz, suffix));
    }

    public void drop(Connection conn, Class<? extends IEntity> clazz, String suffix) {
        executor.withinPrepare(conn, dropSQL(clazz, suffix), PreparedStatement::execute);
    }

    public String dropSQL(Class<? extends IEntity> clazz, String suffix) {
        Meta meta = MetaUtils.meta(clazz);
        LegendBase legendBase = LegendBase.drop().table(meta.name, suffix);
        return legendBase.sql();
    }

    public void truncate(Class<? extends IEntity> clazz) {
        truncate(clazz, null);
    }

    public void truncate(Class<? extends IEntity> clazz, String suffix) {
        executor.withinTx(conn -> truncate(conn, clazz, suffix));
    }

    public void truncate(Connection conn, Class<? extends IEntity> clazz, String suffix) {
        executor.withinPrepare(conn, truncateSQL(clazz, suffix), PreparedStatement::execute);
    }

    public String truncateSQL(Class<? extends IEntity> clazz, String suffix) {
        Meta meta = MetaUtils.meta(clazz);
        LegendBase legendBase = LegendBase.truncate().table(meta.name, suffix);
        return legendBase.sql();
    }

    private <T extends IEntity> T translate(ResultSet resultSet, Class<T> clazz) throws SQLException {
        Meta meta = MetaUtils.meta(clazz);
        ResultSetMetaData rsd = resultSet.getMetaData();
        T result = MetaUtils.empty(clazz);
        try {
            for (int i=0;i<rsd.getColumnCount();i++) {
                String name = rsd.getColumnName(i+1);
                ColumnInfo columnInfo = meta.columns.get(name.toLowerCase());
                if (columnInfo != null) {
                    columnInfo.field().setAccessible(true);
                    columnInfo.field().set(result, resultSet.getObject(i+1));
                }
            }

        } catch (Exception e) {
            throw new LegendException("entity class should provide default construct");
        }
        return result;
    }

    public <T extends IEntity> T get(Class<T> clazz,Object...ids) {
        Holder<T> holder = new Holder<>();
        executor.withinTx(conn -> holder.value = get(conn, clazz, ids), false);
        return holder.value;
    }

    public <T extends IEntity> T get(Connection conn, Class<T> clazz, Object...ids) {
        Holder<T> holder = new Holder<>();
        executor.withinQuery(conn, selectSQL(clazz, ids), stmt -> {
            for (int i=0;i<ids.length;i++) {
                stmt.setObject(i+1, ids[i]);
            }
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return null;
            }
            holder.value = translate(result, clazz);
            return result;
        });
        return holder.value;
    }

    private String selectSQL(Class<? extends IEntity> clazz, Object...ids) {
        Meta meta = MetaUtils.meta(clazz);
        String[] columns = meta.indices.primary().columns();
        if (columns.length != ids.length) {
            throw new LegendException("ids length must match with primary columns ");
        }
        LegendBase.Filterable[] filters = new LegendBase.Filterable[ids.length];
        for (int i=0;i<ids.length;i++) {
            filters[i] = LegendBase.eq_(columns[i]);
        }
        IEntity empty = MetaUtils.empty(clazz, ids);
        LegendBase legendBase = LegendBase.select().field("*").table(meta.name, empty.suffix())
                .where(LegendBase.and(filters));
        return legendBase.sql();
    }

    public <T extends IEntity> List<T> find(Class<T> clazz) {
        return this.find(clazz, null, null, 0, 0, new Object[]{});
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, LegendBase.Filterable filter, Object...values) {
        return this.find(clazz,null, filter, 0, 0, values);
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, String suffix, Object...values) {
        return this.find(clazz, suffix, null, 0, 0, values);
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, String suffix,
                                            LegendBase.Filterable filter, Object...values) {
        return this.find(clazz, suffix, filter, 0, 0, values);
    }

    public <T extends IEntity> List<T> find(Class<T> clazz, String suffix, LegendBase.Filterable filter,
                                            int offset, int limit, Object...values) {
        Holder<List<T>> holder = new Holder<>();
        executor.withinTx(conn -> holder.value=find(conn, clazz, suffix, filter, offset, limit, values), false);
        return holder.value;
    }

    public <T extends IEntity> List<T> find(Connection conn, Class<T> clazz, String suffix,
                                            LegendBase.Filterable filter, Object...values) {
        return this.find(conn, clazz, suffix, filter, 0, 0, values);
    }

    public <T extends IEntity> List<T> find(Connection conn, Class<T> clazz,
                                            String suffix, LegendBase.Filterable filter,
                                            int offset, int limit, Object...values) {
        Holder<List<T>> holder = new Holder<>();
        executor.withinQuery(conn, findSQL(clazz, suffix, filter), stmt -> {
            int i = 1;
            for (;i<=values.length;i++) {
                stmt.setObject(i, values[i-1]);
            }
            if (offset > 0) {
                stmt.setObject(i++, offset);
            }
            if (limit > 0) {
                stmt.setObject(i++, limit);
            }
            List<T> result = new ArrayList<>();
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                result.add(translate(resultSet, clazz));
            }
            holder.value = result;
            return resultSet;
        });
        return holder.value;
    }

    private String findSQL(Class<? extends IEntity> clazz, String suffix,
                           LegendBase.Filterable filter) {
        Meta meta = MetaUtils.meta(clazz);
        LegendBase legendBase = LegendBase.select().field("*").table(meta.name, suffix);
        if (filter != null) {
            legendBase.where(filter);
        }
        if (orderByList!=null && orderByList.size()>0) {
            orderByList.forEach(legendBase::orderBy);
        }
        if (groupByList!=null && groupByList.size()>0) {
            groupByList.forEach(legendBase::groupBy);
        }
        if (offset > 0) {
            legendBase.offset(offset);
        }
        if (limit > 0) {
            legendBase.limit(limit);
        }
        return legendBase.sql();
    }

    public LegendDB orderBy(String name, String direction) {
        if (orderByList == null) {
            orderByList = new ArrayList<>();
        }
        orderByList.add(new LegendBase.OrderBy(name, direction));
        return this;
    }

    public LegendDB orderBy(String name) {
        if (orderByList == null) {
            orderByList = new ArrayList<>();
        }
        orderByList.add(new LegendBase.OrderBy(name));
        return this;
    }

    public LegendDB groupBy(String...fs) {
        if (groupByList == null) {
            groupByList = new ArrayList<>();
        }
        groupByList.addAll(Arrays.asList(fs));
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
        executor.withinTx(conn -> holder.value = count(conn, clazz, suffix, filter, values), false);
        return holder.value;
    }

    public long count(Connection conn, Class<? extends IEntity> clazz,
                      String suffix, LegendBase.Filterable filter, Object...values) {
        Holder<Long> holder = new Holder<>();
        LegendBase legendBase = countSQL(clazz, suffix, filter);
        Context evt = Context.before(this, conn, clazz, legendBase, values);
        if (!listenerHandler.invokeListeners(evt)) {
            return -1;
        }
        long start = System.nanoTime();
        Exception error = null;
        try {
            executor.withinQuery(conn, legendBase.sql(), stmt -> {
                int i = 1;
                for (;i<=values.length;i++) {
                    stmt.setObject(i, values[i-1]);
                }
                ResultSet resultSet = stmt.executeQuery();
                resultSet.next();
                holder.value = resultSet.getLong(1);
                return resultSet;
            });
        } catch (RuntimeException e) {
            error = e;
        }
        long duration = (System.nanoTime() - start) / 1000;
        evt = Context.after(this, conn, clazz, legendBase, values, error, duration);
        listenerHandler.invokeListeners(evt);
        return holder.value;
    }

    private LegendBase countSQL(Class<? extends IEntity> clazz, String suffix, LegendBase.Filterable filter) {
        Meta meta = MetaUtils.meta(clazz);
        LegendBase legendBase = LegendBase.select().field("count(*)").table(meta.name, suffix);
        if (filter != null) {
            legendBase.where(filter);
        }
        return legendBase;
    }

    public void any(Class<? extends IEntity> clazz, LegendBase legendBase, IQueryOp op, Object...values) {
        executor.withinTx(conn -> any(conn, clazz, legendBase, op, values), false);
    }

    public void any(Connection conn, Class<? extends IEntity> clazz,
                    LegendBase legendBase, IQueryOp op, Object...values) {
        Context evt = Context.before(this, conn, clazz, legendBase, values);
        if (!listenerHandler.invokeListeners(evt)) {
            return;
        }
        long start = System.nanoTime();
        Exception error = null;
        try {
            executor.withinQuery(conn, legendBase.sql(), op);
        } catch (RuntimeException e) {
            error = e;
        }
        long duration = (System.nanoTime() - start) / 1000;
        listenerHandler.invokeListeners(Context.after(this, conn, clazz, legendBase, values, error, duration));
    }

    private <T extends IEntity> Object[] ids(T t) {
        Meta meta = MetaUtils.meta(t.getClass());
        Object[] ids = new Object[meta.indices.primary().columns().length];
        int i = 0;
        for (String name: meta.indices.primary().columns()) {
            Field field = meta.columns.get(name).field();
            try {
                ids[i++] = field.get(t);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return ids;
    }

    private <T extends IEntity> Map<String, Object> values(T t) {
        Meta meta = MetaUtils.meta(t.getClass());
        Map<String, Object> values = new HashMap<>();
        meta.columns.forEach((name, columnInfo) -> {
            Field field = columnInfo.field();
            try {
                values.put(field.getName(), field.get(t));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return values;
    }

    public <T extends IEntity> int delete(T t) {
        return delete(t.getClass(), ids(t));
    }

    public int delete(Class<? extends IEntity> clazz, Object...ids) {
        Holder<Integer> holder = new Holder<>();
        executor.withinTx(conn -> holder.value = delete(conn, clazz, ids));
        return holder.value;
    }

    public int delete(Connection conn, Class<? extends IEntity> clazz, Object...ids) {
        Holder<Integer> holder = new Holder<>();
        LegendBase legendBase = deleteSQL(clazz, ids);
        Context evt = Context.before(this, conn, clazz, legendBase, ids);
        if (!listenerHandler.invokeListeners(evt)) {
            return -1;
        }
        long start = System.nanoTime();
        Exception error = executeUpdate(conn, holder, legendBase, ids);
        long duration = (System.nanoTime() - start) / 1000;
        listenerHandler.invokeListeners(Context.after(this, conn, clazz, legendBase, ids, error, duration));
        return holder.value;
    }

    private LegendBase deleteSQL(Class<? extends IEntity> clazz, Object...ids) {
        Meta meta = MetaUtils.meta(clazz);
        String[] columns = meta.indices.primary().columns();
        if (columns.length != ids.length) {
            throw new LegendException("ids length must match with primary columns");
        }
        LegendBase.Filterable[] filters = new LegendBase.Filterable[columns.length];
        for (int i=0;i<ids.length;i++) {
            filters[i] = LegendBase.eq_(columns[i]);
        }
        IEntity empty = MetaUtils.empty(clazz, ids);
        LegendBase legendBase = LegendBase.delete().table(meta.name, empty.suffix()).where(LegendBase.and(filters));
        return legendBase;
    }

    public <T extends IEntity> int update(T t) {
        return update(t.getClass(), values(t), ids(t));
    }

    public int update(Class<? extends IEntity> clazz, Map<String, Object> values, Object...ids) {
        Holder<Integer> holder = new Holder<>();
        Map<String, Object> valueModify = new HashMap<>(values);
        Meta meta = MetaUtils.meta(clazz);
        // 移除主键列
        for (String name: meta.indices.primary().fields()) {
            valueModify.remove(name);
        }
        executor.withinTx(conn -> holder.value = update(conn, clazz, valueModify, ids));
        return holder.value;
    }

    public int update(Connection conn, Class<? extends IEntity> clazz,
                      Map<String, Object> values, Object...ids) {
        Holder<Integer> holder = new Holder<>();
        LegendBase legendBase = updateSQL(clazz, values, ids);
        Object[] hybridValues = new Object[values.size() + ids.length];
        int i = 0;
        for (Object value: values.values()) {
            hybridValues[i++] = value;
        }
        for (Object value: ids) {
            hybridValues[i++] = value;
        }
        Context evt = Context.before(this, conn, clazz, legendBase, hybridValues);
        if (!listenerHandler.invokeListeners(evt)) {
            return -1;
        }
        long start = System.nanoTime();
        Exception error = executeUpdate(conn, holder, legendBase, hybridValues);
        long duration = (System.nanoTime() - start) / 1000;
        listenerHandler.invokeListeners(Context.after(this, conn, clazz, legendBase, hybridValues, error, duration));
        return holder.value;
    }

    private Exception executeUpdate(Connection conn, Holder<Integer> holder, LegendBase legendBase, Object[] hybridValues) {
        try {
            String sql = legendBase.sql();
            System.out.println(legendBase.sql());
            executor.withinPrepare(conn, sql, stmt -> {
                for (int k=0;k<hybridValues.length;k++) {
                    stmt.setObject(k+1, hybridValues[k]);
                }
                int val = stmt.executeUpdate();
                if (holder != null) {
                    holder.value = val;
                }
            });
        } catch (RuntimeException e) {
            return e;
        }
        return null;
    }

    private LegendBase updateSQL(Class<? extends IEntity> clazz,
                                 Map<String, Object> values, Object...ids) {
        Meta meta = MetaUtils.meta(clazz);
        String[] columns = meta.indices.primary().columns();
        if (columns.length != ids.length) {
            throw new LegendException("ids length must match with primary columns");
        }
        LegendBase.Filterable[] filters = new LegendBase.Filterable[ids.length];
        for (int i=0;i<ids.length;i++) {
            filters[i] = LegendBase.eq_(columns[i]);
        }
        IEntity empty = MetaUtils.empty(clazz, ids);
        LegendBase legendBase = LegendBase.update().table(meta.name, empty.suffix()).where(LegendBase.and(filters));
        values.forEach((name, value) -> {
            ColumnInfo columnInfo = meta.fields.get(name);
            if (columnInfo ==null || columnInfo.primary()) {
                return;
            }
            legendBase.with_(columnInfo.name());
        });
        return legendBase;
    }

    public <T extends IEntity> boolean insert(T t) {
        Map<String, Object> values = new LinkedHashMap<>();
        Meta meta = MetaUtils.meta(t.getClass());
        Holder<Integer> lastInsertId = null;
        Field lastInsertField = null;
        for (Map.Entry<String, ColumnInfo> entry: meta.columns.entrySet()) {
            ColumnInfo columnInfo = entry.getValue();
            try {
                Field field = columnInfo.field();
                Object o = field.get(t);
                if (columnInfo.autoIncrement() && o==null) {
                    lastInsertId = new Holder<>();
                    lastInsertField = columnInfo.field();
                }
                values.put(field.getName(), o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        boolean res = insert(t, values, lastInsertId);
        if (res && lastInsertId!=null && lastInsertId.value != null) {
            try {
                lastInsertField.set(t, lastInsertId.value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public <T extends IEntity> boolean insert(T t, Map<String, Object> values
            , Holder<Integer> lastInsertId) {
        Holder<Boolean> holder = new Holder<>();
        executor.withinTx(conn -> {
            holder.value = insert(conn, t, values);
            if (lastInsertId != null) {
                LegendBase legendBase = LegendBase.select().field("last_insert_id()");
                this.any(conn, t.getClass(), legendBase, stmt -> {
                    ResultSet resultSet = stmt.executeQuery();
                    if (resultSet.next()) {
                        lastInsertId.value = resultSet.getInt(1);
                    }
                    return resultSet;
                });
            }
        });
        return holder.value;
    }

    public <T extends IEntity> boolean insert(Connection conn, T t, Map<String, Object> values) {
        Meta meta = MetaUtils.meta(t.getClass());
        LegendBase legendBase = insertSQL(t, values);
        List<Object> paramList = new ArrayList<>();
        for (Map.Entry<String, ColumnInfo> entry: meta.columns.entrySet()) {
            ColumnInfo columnInfo = entry.getValue();
            Object value = values.get(columnInfo.field().getName());
            if (value != null) {
                paramList.add(value);
            }
        }
        Object[] paramArray = paramList.toArray();
        Context evt = Context.before(this, conn, t.getClass(), legendBase, paramArray);
        if (!listenerHandler.invokeListeners(evt)) {
            return false;
        }
        long start = System.nanoTime();
        Exception error = executeUpdate(conn, null, legendBase, paramArray);
        long duration = (System.nanoTime() - start) / 1000;
        evt = Context.after(this, conn, t.getClass(), legendBase, paramArray, error, duration);
        listenerHandler.invokeListeners(evt);
        return error == null;
    }

    private <T extends IEntity> LegendBase insertSQL(T t, Map<String, Object> values) {
        Meta meta = MetaUtils.meta(t.getClass());
        LegendBase legendBase = LegendBase.insert().table(meta.name, t.suffix());
        for (Map.Entry<String, ColumnInfo> entry: meta.columns.entrySet()) {
            ColumnInfo columnInfo = entry.getValue();
            Object value = values.get(columnInfo.field().getName());
            if (value == null) {
                if (columnInfo.autoIncrement()) continue;
                if (columnInfo.nullable()) continue;
                if (!columnInfo.nullable() && columnInfo.defaultValue() != null
                        && !columnInfo.defaultValue().isEmpty()) {
                    continue;
                }
            }
            legendBase.with_(entry.getKey());
        }
        return legendBase;
    }

}
