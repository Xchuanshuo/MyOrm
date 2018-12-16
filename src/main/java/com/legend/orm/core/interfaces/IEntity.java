package com.legend.orm.core.interfaces;

import com.legend.orm.core.utils.Utils;
import com.legend.orm.core.model.TableIndices;
import com.legend.orm.core.model.TableOptions;

/**
 * @author Legend
 * @data by on 18-12-10.
 * @description 所有的实体类必须实现该接口
 */
public interface IEntity {

    /**
     * 表名
     * @return
     */
    default String table() { return null; }

    /**
     * 分表必须覆盖此方法
     * @return 分表后缀名
     */
    default String suffix() {
        return null;
    }

    default String tableWithSuffix() {
        return tableWith(suffix());
    }

    default String tableWith(String suffix) {
        return Utils.tableWithSuffix(table(), suffix);
    }

    /**
     * 定义表的物理结构
     * @return
     */
    TableOptions options();

    /**
     * 定义表的主键和索引等信息
     * @return
     */
    TableIndices indices();
}
