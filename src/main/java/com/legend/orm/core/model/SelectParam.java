package com.legend.orm.core.model;

import com.legend.orm.core.LegendBase;

import java.util.List;

/**
 * @author Legend
 * @data by on 18-12-17.
 * @description
 */
public class SelectParam {

    // 要查询的字段集合
    private List<String> fieldList;
    private List<LegendBase.OrderBy> orderByList;
    private List<String> groupByList;
    private int offset = 0, limit = 0;
    private LegendBase.Filterable filter;
    private List<Object> valueList;
    private String suffix;
    private LegendBase.Filterable having;

    public SelectParam() {}

    private SelectParam(List<String> fieldList, List<LegendBase.OrderBy> orderByList,
                        List<String> groupByList, int offset, int limit,
                        LegendBase.Filterable filter, List<Object> valueList, String suffix, LegendBase.Filterable having) {
        this.fieldList = fieldList;
        this.orderByList = orderByList;
        this.groupByList = groupByList;
        this.offset = offset;
        this.limit = limit;
        this.filter = filter;
        this.valueList = valueList;
        this.suffix = suffix;
    }

    public List<LegendBase.OrderBy> getOrderByList() {
        return orderByList;
    }

    public void setOrderByList(List<LegendBase.OrderBy> orderByList) {
        this.orderByList = orderByList;
    }

    public List<String> getGroupByList() {
        return groupByList;
    }

    public void setGroupByList(List<String> groupByList) {
        this.groupByList = groupByList;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public LegendBase.Filterable getFilter() {
        return filter;
    }

    public void setFilter(LegendBase.Filterable filter) {
        this.filter = filter;
    }

    public List<Object> getValueList() {
        return valueList;
    }

    public void setValueList(List<Object> valueList) {
        this.valueList = valueList;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public List<String> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<String> fieldList) {
        this.fieldList = fieldList;
    }

    public LegendBase.Filterable getHaving() {
        return having;
    }

    public void setHaving(LegendBase.Filterable having) {
        this.having = having;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<String> fieldList;
        private List<LegendBase.OrderBy> orderByList;
        private List<String> groupByList;
        private LegendBase.Filterable having;
        private int offset = 0, limit = 0;
        private LegendBase.Filterable filter;
        private List<Object> valueList;
        private String suffix;

        private Builder() {}

        public Builder fieldList(List<String> fieldList) {
            this.fieldList = fieldList;
            return this;
        }

        public Builder orderByList(List<LegendBase.OrderBy> orderByList) {
            this.orderByList = orderByList;
            return this;
        }

        public Builder groupByList(List<String> groupByList) {
            this.groupByList = groupByList;
            return this;
        }

        public Builder having(LegendBase.Filterable having) {
            this.having = having;
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder filter(LegendBase.Filterable filter) {
            this.filter = filter;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder valueList(List<Object> valueList) {
            this.valueList = valueList;
            return this;
        }

        public SelectParam build() {
            return new SelectParam(fieldList, orderByList, groupByList
                    , offset, limit, filter, valueList, suffix, having);
        }
    }
}
