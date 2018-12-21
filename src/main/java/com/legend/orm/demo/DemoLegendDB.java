package com.legend.orm.demo;

import com.legend.orm.core.Operator;
import com.legend.orm.core.crud.LegendDB;
import com.legend.orm.core.factory.DBConnectionFactory;
import com.legend.orm.core.listener.ListenerHandler;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;


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
        setOperator(new Operator(factory));
    }

}
