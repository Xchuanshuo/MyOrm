package com.legend.orm.demo;

import com.legend.orm.core.GridDB;

/**
 * @author Legend
 * @data by on 18-12-14.
 * @description
 */
public class GridDemoDB extends GridDB<DemoLegendDB> {

    /**
     * 传进来多个DB对象
     * @param dbs
     */
    public GridDemoDB(DemoLegendDB[] dbs) {
        super(dbs);
        this.registerGridables();
    }

    /**
     * 注册实体类的分库策略
     */
    @Override
    public void registerGridables() {
        this.gridWith(BookShelf.class, new BookShelf.GridStrategy());
    }
}
