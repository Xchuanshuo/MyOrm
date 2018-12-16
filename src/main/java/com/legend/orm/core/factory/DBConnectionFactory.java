package com.legend.orm.core.factory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Legend
 * @data by on 18-12-16.
 * @description
 */
public class DBConnectionFactory {

    private volatile static DBConnectionFactory factory;
    private DataSource dataSource;

    private DBConnectionFactory() {}

    public static DBConnectionFactory getInstance() {
        if (factory == null) {
            synchronized(MetaFactory.class) {
                factory = new DBConnectionFactory();
            }
        }
        return factory;
    }

    public void setDatasource(DataSource datasource) {
        this.dataSource = datasource;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
