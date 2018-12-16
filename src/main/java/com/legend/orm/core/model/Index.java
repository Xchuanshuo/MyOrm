package com.legend.orm.core.model;

import java.util.StringJoiner;

/**
 * @author Legend
 * @data by on 18-12-10.
 * @description
 */
public class Index {

    private String[] columns;
    private String[] fields;
    private boolean primaryKey;
    private boolean unique;

    public Index(boolean primaryKey, boolean unique, String...columns) {
        this.primaryKey = primaryKey;
        this.unique = unique;
        this.columns = columns;
    }

    public Index fields(String[] fields) {
        this.fields = fields;
        return this;
    }

    public String s() {
        StringJoiner joiner = new StringJoiner(", ");
        for (String column: columns) {
            joiner.add(String.format("`%s`", column));
        }
        if (primaryKey) {
            return String.format("primary key(%s)", joiner.toString());
        }
        if (unique) {
            return String.format("unique key(%s)", joiner.toString());
        }
        return String.format("key(%s)", joiner.toString());
    }


    public String[] columns() {
        return columns;
    }

    public String[] fields() {
        return fields;
    }

    public boolean primaryKey() {
        return primaryKey;
    }

    public boolean unique() {
        return unique;
    }
}
