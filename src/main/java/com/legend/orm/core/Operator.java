package com.legend.orm.core;

import com.legend.orm.core.exception.LegendException;
import com.legend.orm.core.factory.DBConnectionFactory;
import com.legend.orm.core.interfaces.IPrepareOp;
import com.legend.orm.core.interfaces.IQueryOp;
import com.legend.orm.core.interfaces.ITransactionOp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Legend
 * @data by on 18-12-16.
 * @description
 */
public class Operator {

    private volatile static Operator operator;
    private static DBConnectionFactory factory;

    public static Operator getInstance() {
        if (operator == null) {
            synchronized(Operator.class) {
                if (operator == null) {
                    operator = new Operator(factory);
                }
            }
        }
        return operator;
    }

    public Operator(DBConnectionFactory factory) {
        Operator.factory = factory;
    }

    public void withinTx(ITransactionOp op) {
        withinTx(op, true);
    }

    public void withinTx(ITransactionOp op, boolean tx) {
        Connection connection = factory.getConnection();
        try {
            op.execute(connection);
            if (tx) {
                connection.commit();
            }
        } catch (SQLException e) {
            if (tx) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {

                }
            }
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void withinPrepare(Connection conn, String sql, IPrepareOp op) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            op.prepare(stmt);
        } catch (SQLException e) {
            throw new LegendException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    public void withinQuery(Connection conn, String sql, IQueryOp op) {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            System.out.println(sql);
            stmt = conn.prepareStatement(sql);
            result = op.query(stmt);
        } catch (SQLException e) {
            throw new LegendException(e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {

                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
