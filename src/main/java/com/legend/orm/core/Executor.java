package com.legend.orm.core;

import com.legend.orm.core.exception.LegendException;
import com.legend.orm.core.factory.DBConnectionFactory;
import com.legend.orm.core.factory.SQLBuilderFactory;
import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.interfaces.IQueryOp;
import com.legend.orm.core.listener.ListenerHandler;
import com.legend.orm.core.model.ColumnInfo;
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

    public void create(Connection conn, Class<? extends IEntity> clazz,
                       String suffix) {
        operator.withinPrepare(conn, sqlFactory.buildCreateSQL(clazz, suffix), PreparedStatement::execute);
    }

    public void drop(Connection conn, Class<? extends IEntity> clazz, String suffix) {
        operator.withinPrepare(conn, sqlFactory.buildDropSQL(clazz, suffix), PreparedStatement::execute);
    }

    public <T extends IEntity> T get(Connection conn, Class<T> clazz, Object...ids) {
        LegendDB.Holder<T> holder = new LegendDB.Holder<>();
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

    public void truncate(Connection conn, Class<? extends IEntity> clazz, String suffix) {
        operator.withinPrepare(conn, sqlFactory.buildTruncateSQL(clazz, suffix), PreparedStatement::execute);
    }
    public  <T extends IEntity> T translate(ResultSet resultSet, Class<T> clazz) throws SQLException {
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

    public <T extends IEntity> List<T> find(Connection conn, Class<T> clazz,
                                           SelectParam param) {
        LegendDB.Holder<List<T>> holder = new LegendDB.Holder<>();
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

    public int update(LegendDB db, Class<? extends IEntity> clazz,
                      Map<String, Object> values, LegendBase.Filterable filter, Object...objects) {
        Connection conn = connectionFactory.getConnection();
        LegendDB.Holder<Integer> holder = new LegendDB.Holder<>();
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
        Exception error = this.executeUpdate(holder, legendBase, hybridValues);
        long duration = (System.nanoTime() - start) / 1000;
        listenerHandler.invokeListeners(Context.after(db, conn, clazz, legendBase, hybridValues, error, duration));
        return holder.value;
    }

    public long count(LegendDB db, Class<? extends IEntity> clazz,
                     SelectParam param) {
        Connection conn = connectionFactory.getConnection();
        LegendDB.Holder<Long> holder = new LegendDB.Holder<>();
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
            , LegendDB.Holder<Integer> lastInsertId) {
        LegendDB.Holder<Boolean> holder = new LegendDB.Holder<>();
        operator.withinTx(conn -> {
            holder.value = insert(db, t, values);
            if (lastInsertId != null) {
                LegendBase legendBase = LegendBase.select().field("last_insert_id()");
                this.any(db, t.getClass(), legendBase, stmt -> {
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
        Exception error = this.executeUpdate(null, legendBase, paramArray);
        long duration = (System.nanoTime() - start) / 1000;
        evt = Context.after(db, connectionFactory.getConnection(), t.getClass(), legendBase, paramArray, error, duration);
        listenerHandler.invokeListeners(evt);
        return error == null;
    }

    public int delete(LegendDB db, Class<? extends IEntity> clazz, Object...ids) {
        Connection conn = connectionFactory.getConnection();
        LegendDB.Holder<Integer> holder = new LegendDB.Holder<>();
        LegendBase legendBase = sqlFactory.buildDeleteObject(clazz, ids);
        Context evt = Context.before(db, conn, clazz, legendBase, ids);
        if (!listenerHandler.invokeListeners(evt)) {
            return -1;
        }
        long start = System.nanoTime();
        Exception error = this.executeUpdate(holder, legendBase, ids);
        long duration = (System.nanoTime() - start) / 1000;
        listenerHandler.invokeListeners(Context.after(db, conn
                , clazz, legendBase, ids, error, duration));
        return holder.value;
    }

    public void any(LegendDB db, Class<? extends IEntity> clazz,
                    LegendBase legendBase, IQueryOp op, Object...values) {
        Connection conn = connectionFactory.getConnection();
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

    public Exception executeUpdate(LegendDB.Holder<Integer> holder, LegendBase legendBase, Object[] hybridValues) {
        try {
            String sql = legendBase.sql();
            System.out.println(legendBase.sql());
            operator.withinPrepare(connectionFactory.getConnection(),sql, stmt -> {
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
