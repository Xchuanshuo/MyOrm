package com.legend.orm.core.model;

import com.legend.orm.core.LegendBase;

import java.util.List;

/**
 * @author Legend
 * @data by on 18-12-17.
 * @description
 */
public class SelectParam {

    private List<LegendBase.OrderBy> orderByList;
    private List<String> groupByList;
    private int offset = 0, limit = 0;

    public SelectParam() {}

    public SelectParam(List<LegendBase.OrderBy> orderByList, List<String> groupByList, int offset, int limit) {
        this.orderByList = orderByList;
        this.groupByList = groupByList;
        this.offset = offset;
        this.limit = limit;
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
}
