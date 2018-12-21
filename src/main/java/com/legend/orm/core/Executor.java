package com.legend.orm.core;

import com.legend.orm.core.crud.LegendBase;
import com.legend.orm.core.crud.LegendDB;
import com.legend.orm.core.exception.LegendException;
import com.legend.orm.core.factory.DBConnectionFactory;
import com.legend.orm.core.factory.SQLBuilderFactory;
import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.interfaces.IQueryOp;
import com.legend.orm.core.listener.ListenerHandler;
import com.legend.orm.core.model.ColumnInfo;
import com.legend.orm.core.model.Holder;
import com.legend.orm.core.model.Meta;
import com.legend.orm.core.model.SelectParam;
import com.legend.orm.core.utils.MetaUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Legend
 * @data by on 18-12-18.
 * @description
 */
public class Executor {

    private volatile static Executor executor;
    private SQLBuilderFactory sqlFactory = SQLBuilderFactory.getInstance();
    private DBConnectionFactory connectionFactory = DBConnectionFactory.getInstance();
    private Operator operator = Operator.getInstance();
    private ListenerHandler listenerHandler = new ListenerHandler();

    private Executor() {}

    public static Executor getInstance() {
        if (executor == null) {
            synchronized(Executor.class) {
                if (executor == null) {
                    executor = new Executor();
                }
            }
        }
        return executor;
    }

    public void create(Class<? extends IEntity> clazz, String suffix) {
        operator.withinTx(conn -> executor.create(conn, clazz, suffix));
    }

    private void create(Connection conn, Class<? extends IEntity> clazz,
                       String suffix) {
        operator.withinPrepare(conn, sqlFactory.buildCreateSQL(clazz, suffix), PreparedStatement::execute);
    }

    public void drop(Class<? extends IEntity> clazz, String suffix) {
        operator.withinTx(conn -> executor.drop(conn, clazz, suffix));
    }

    private void drop(Connection conn, Class<? extends IEntity> clazz, String suffix) {
        operator.withinPrepare(conn, sqlFactory.buildDropSQL(clazz, suffix), PreparedStatement::execute);
    }

    public void truncate(Class<? extends IEntity> clazz, String suffix) {
        operator.withinTx(conn -> executor.truncate(conn, clazz, suffix));
    }

    private void truncate(Connection conn, Class<? extends IEntity> clazz, String suffix) {
        operator.withinPrepare(conn, sqlFactory.buildTruncateSQL(clazz, suffix), PreparedStatement::execute);
    }

    public <T extends IEntity> T get(Class<T> clazz, Object...ids) {
        Holder<T> holder = new Holder<>();
        operator.withinTx(conn -> holder.value = executor.get(conn, clazz, ids), false);
        return holder.value;
    }

    private  <T extends IEntity> T get(Connection conn, Class<T> clazz, Object...ids) {
        Holder<T> holder = new Holder<>();
        operator.withinQuery(conn, sqlFactory.buildSelectOneSQL(clazz, ids), stmt -> {
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

    public <T extends IEntity> T translate(ResultSet resultSet, Class<T> clazz) throws SQLException {
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

    public <T extends IEntity> List<T> find(Class<T> clazz, SelectParam param) {
        Connection conn = connectionFactory.getConnection();
        Holder<List<T>> holder = new Holder<>();
        operator.withinQuery(conn, sqlFactory.buildFindSQL(clazz, param), stmt -> {
            int i = 1;
            for (;i<=param.getValueList().size();i++) {
                stmt.setObject(i, param.getValueList().get(i-1));
            }
            if (param.getOffset() > 0) {
                stmt.setObject(i++, param.getOffset());
            }
            if (param.getLimit() > 0) {
                stmt.setObject(i, param.getLimit());
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

    // 带where条件多条更新
    public int update(LegendDB db, Class<? extends IEntity> clazz, Map<String, Object> values
            , LegendBase.Filterable filter, Object...filterValues) {
        Holder<Integer> holder = new Holder<>();
        Map<String, Object> valueModify = MetaUtils.updateDispose(clazz, values);
        operator.withinTx(conn -> holder.value = this.update(conn, db, clazz, valueModify,
                filter, filterValues));
        return holder.value;
    }

    private int update(Connection conn, LegendDB db, Class<? extends IEntity> clazz,
                      Map<String, Object> values, LegendBase.Filterable filter, Object...objects) {
        Holder<Integer> holder = new Holder<>();
        LegendBase legendBase = sqlFactory.buildUpdateObject(clazz, values, filter, objects);
        Object[] hybridValues = new Object[values.size() + objects.length];
        int i = 0;
        for (Object value: values.values()) {
            hybridValues[i++] = value;
        }
        for (Object value: objects) {
            hybridValues[i++] = value;
        }
        Context evt = Context.before(db, conn, clazz, legendBase, hybridValues);
        if (!listenerHandler.invokeListeners(evt)) {
            return -1;
        }
        long start = System.nanoTime();
        Exception error = this.executeUpdate(conn, holder, legendBase, hybridValues);
        long duration = (System.nanoTime() - start) / 1000;
        listenerHandler.invokeListeners(Context.after(db, conn, clazz, legendBase, hybridValues, error, duration));
        return holder.value;
    }

    public long count(LegendDB db, Class<? extends IEntity> clazz,
                     SelectParam param) {
        Connection conn = connectionFactory.getConnection();
        Holder<Long> holder = new Holder<>();
        LegendBase legendBase = sqlFactory.buildCountSQL(clazz, param);
        Context evt = Context.before(db, conn, clazz, legendBase, param.getValueList().toArray());
        if (!listenerHandler.invokeListeners(evt)) {
            return -1;
        }
        List<Object> valueList = param.getValueList();
        long start = System.nanoTime();
        Exception error = null;
        try {
            operator.withinQuery(conn, legendBase.sql(), stmt -> {
                int i = 1;
                for (;i<=param.getValueList().size();i++) {
                    stmt.setObject(i,valueList.get(i-1));
                }
                if (param.getOffset() > 0) {
                    stmt.setObject(i++, param.getOffset());
                }
                if (param.getLimit() > 0) {
                    stmt.setObject(i, param.getLimit());
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
        evt = Context.after(db, conn, clazz, legendBase, valueList.toArray(), error, duration);
        listenerHandler.invokeListeners(evt);
        return holder.value;
    }

    public <T extends IEntity> boolean insert(LegendDB db, T t, Map<String, Object> values
            , Holder<Integer> lastInsertId) {
        Holder<Boolean> holder = new Holder<>();
        operator.withinTx(conn -> {
            holder.value = insert(db, t, values);
            if (lastInsertId != null) {
                LegendBase legendBase = LegendBase.select().field("last_insert_id()");
                this.any(conn, db, t.getClass(), legendBase, stmt -> {
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

    public <T extends IEntity> boolean insert(LegendDB db, T t, Map<String, Object> values) {
        Meta meta = MetaUtils.meta(t.getClass());
        LegendBase legendBase = sqlFactory.buildInsertObject(t, values);
        List<Object> paramList = new ArrayList<>();
        for (Map.Entry<String, ColumnInfo> entry: meta.columns.entrySet()) {
            ColumnInfo columnInfo = entry.getValue();
            Object value = values.get(columnInfo.field().getName());
            if (value != null) {
                paramList.add(value);
            }
        }
        Object[] paramArray = paramList.toArray();
        Context evt = Context.before(db, connectionFactory.getConnection(), t.getClass(), legendBase, paramArray);
        if (!listenerHandler.invokeListeners(evt)) {
            return false;
        }
        long start = System.nanoTime();
        Exception error = this.executeUpdate(connectionFactory.getConnection(),
                null, legendBase, paramArray);
        long duration = (System.nanoTime() - start) / 1000;
        evt = Context.after(db, connectionFactory.getConnection(), t.getClass(), legendBase, paramArray, error, duration);
        listenerHandler.invokeListeners(evt);
        return error == null;
    }

    public int delete(LegendDB db, Class<? extends IEntity> clazz, Object...ids) {
        Holder<Integer> holder = new Holder<>();
        operator.withinTx(conn -> holder.value = executor.delete(conn, db, clazz, ids));
        return holder.value;
    }

    private int delete(Connection conn, LegendDB db, Class<? extends IEntity> clazz, Object...ids) {
        Holder<Integer> holder = new Holder<>();
        LegendBase legendBase = sqlFactory.buildDeleteObject(clazz, ids);
        Context evt = Context.before(db, conn, clazz, legendBase, ids);
        if (!listenerHandler.invokeListeners(evt)) {
            return -1;
        }
        long start = System.nanoTime();
        Exception error = this.executeUpdate(conn, holder, legendBase, ids);
        long duration = (System.nanoTime() - start) / 1000;
        listenerHandler.invokeListeners(Context.after(db, conn
                , clazz, legendBase, ids, error, duration));
        return holder.value;
    }

    public void any(Class<? extends IEntity> clazz, LegendDB db,
                    LegendBase legendBase, IQueryOp op, Object...values) {
        operator.withinTx(conn -> this.any(conn, db, clazz, legendBase, op, values), false);
    }

    private void any(Connection conn, LegendDB db, Class<? extends IEntity> clazz,
                    LegendBase legendBase, IQueryOp op, Object...values) {
        Context evt = Context.before(db, conn, clazz, legendBase, values);
        if (!listenerHandler.invokeListeners(evt)) {
            return;
        }
        long start = System.nanoTime();
        Exception error = null;
        try {
            operator.withinQuery(conn, legendBase.sql(), op);
        } catch (RuntimeException e) {
            error = e;
        }
        long duration = (System.nanoTime() - start) / 1000;
        listenerHandler.invokeListeners(Context.after(db, conn, clazz, legendBase, values, error, duration));
    }

    public Exception executeUpdate(Connection conn, Holder<Integer> holder, LegendBase legendBase, Object[] hybridValues) {
        try {
            String sql = legendBase.sql();
            System.out.println(legendBase.sql());
            operator.withinPrepare(conn, sql, stmt -> {
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
}
