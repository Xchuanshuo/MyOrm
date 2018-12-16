package com.legend.orm.core.model;

import com.legend.orm.core.exception.LegendException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Legend
 * @data by on 18-12-10.
 * @description
 */
public class Meta {

    public String name;
    public Map<String, ColumnInfo> columns = new LinkedHashMap<>();
    public Map<String, ColumnInfo> fields = new LinkedHashMap<>();
    public TableOptions options;
    public TableIndices indices;

    public Meta column(ColumnInfo columnInfo) {
        if (columns.get(columnInfo.name()) != null){
            throw new LegendException("duplicated columnInfo name "+ columnInfo.name());
        }
        columns.put(columnInfo.name(), columnInfo);
        fields.put(columnInfo.field().getName(), columnInfo);
        return this;
    }
}
