package com.legend.orm.core.interfaces;

/**
 * @author Legend
 * @data by on 18-12-10.
 * @description
 */
public interface IGridable<T extends IEntity> {

    /**
     * 根据实体对象选择分库索引
     * @param dbs
     * @param t
     * @return
     */
    int select(int dbs, T t);

    /**
     * 根据特点参数选择分库索引
     * @param dbs
     * @param params
     * @return
     */
    int select(int dbs, Object...params);
}
