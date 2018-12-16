package com.legend.orm.demo;

import com.legend.orm.core.Executor;
import com.legend.orm.core.LegendDB;
import com.legend.orm.core.factory.DBConnectionFactory;
import com.legend.orm.core.listener.ListenerHandler;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;

import java.sql.Connection;


/**
 * @author Legend
 * @data by on 18-12-13.
 * @description
 */
public class DemoLegendDB extends LegendDB {

    private DBConnectionFactory factory;

    public DemoLegendDB(String name, String uri) {
        this(name, new ListenerHandler(), uri);
    }

    public DemoLegendDB(String name, ListenerHandler handler, String uri) {
        super(name, handler);
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setUrl(uri);
        factory = DBConnectionFactory.getInstance();
        factory.setDatasource(dataSource);
        setExecutor(new Executor(factory));
    }

    @Override
    protected Connection conn() {
        return factory.getConnection();
    }
}