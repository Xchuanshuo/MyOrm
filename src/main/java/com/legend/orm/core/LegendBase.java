package com.legend.orm.core;

import com.legend.orm.core.model.ColumnInfo;
import com.legend.orm.core.model.Index;
import com.legend.orm.core.model.Option;
import com.legend.orm.core.utils.StringUtils;
import com.legend.orm.core.utils.Utils;

import java.util.*;

/**
 * @author Legend
 * @data by on 18-12-10.
 * @description
 */
public class LegendBase {

    static enum Type {
        Insert, Delete, Update, Select, Drop, Create, Truncate
    }

    private Type type;
    private List<String> fields = new ArrayList<>();
    private String table;
    private String suffix;
    private Filterable filter;
    private List<String> groupBys = new ArrayList<>();
    private Filterable having;
    private List<OrderBy> orderBys = new ArrayList<>();
    private int offset;
    private int limit;
    private boolean offset_;
    private boolean limit_;

    private List<Setter> setters = new ArrayList<>();

    private List<ColumnInfo> columnInfos = new ArrayList<>();
    private List<Index> indices = new ArrayList<>();
    private List<Option> options = new ArrayList<>();

    private static final PlaceHolder DEFAULT = new PlaceHolder();

    private LegendBase() {}

    public static LegendBase insert() {
        LegendBase legendBase = new LegendBase();
        legendBase.type = Type.Insert;
        return legendBase;
    }

    public static LegendBase delete() {
        LegendBase legendBase = new LegendBase();
        legendBase.type = Type.Delete;
        return legendBase;
    }

    public static LegendBase update() {
        LegendBase legendBase = new LegendBase();
        legendBase.type = Type.Update;
        return legendBase;
    }

    public static LegendBase select() {
        LegendBase legendBase = new LegendBase();
        legendBase.type = Type.Select;
        return legendBase;
    }

    public static LegendBase create() {
        LegendBase legendBase = new LegendBase();
        legendBase.type = Type.Create;
        return legendBase;
    }

    public static LegendBase drop() {
        LegendBase legendBase = new LegendBase();
        legendBase.type = Type.Drop;
        return legendBase;
    }

    public static LegendBase truncate() {
        LegendBase legendBase = new LegendBase();
        legendBase.type = Type.Truncate;
        return legendBase;
    }

    public String sql() {
        switch (type) {
            case Create:
                return createSQL();
            case Drop:
                return dropSQL();
            case Truncate:
                return truncateSQL();
            case Insert:
                return insertSQL();
            case Delete:
                return deleteSQL();
            case Update:
                return updateSQL();
            case Select:
                return selectSQL();
            default:
                return null;
        }
    }

    // select 需要查询的字段
    public LegendBase field(String...fs) {
        this.fields.addAll(Arrays.asList(fs));
        return this;
    }

    public LegendBase table(String table) {
        this.table = table;
        return this;
    }

    public LegendBase table(String table, String suffix) {
        this.table = table;
        this.suffix = suffix;
        return this;
    }

    public LegendBase suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public String table() {
        return table;
    }

    public String tableWithSuffix() {
        return Utils.tableWithSuffix(table, suffix);
    }

    public LegendBase groupBy(String...fs) {
        List<String> list = new ArrayList<>();
        for (String s: fs) {
            list.add(String.format("`%s`", s));
        }
        this.groupBys.addAll(list);
        return this;
    }

    public LegendBase where(Filterable filter) {
        this.filter = filter;
        return this;
    }

    public LegendBase having(Filterable filter) {
        this.having = filter;
        return this;
    }

    public LegendBase orderBy(OrderBy order) {
        this.orderBys.add(order);
        return this;
    }

    public LegendBase orderBy(String name) {
        this.orderBys.add(new OrderBy(name, null));
        return this;
    }

    public LegendBase orderBy(String name, String direction) {
        this.orderBys.add(new OrderBy(name, direction));
        return this;
    }

    public LegendBase offset(int offset) {
        this.offset = offset;
        return this;
    }

    public LegendBase offset_() {
        this.offset_ = true;
        return this;
    }

    public LegendBase limit(int limit) {
        this.limit = limit;
        return this;
    }

    public LegendBase limit_() {
        this.limit_ = true;
        return this;
    }

    public LegendBase with(String name, Object value) {
        this.setters.add(new Setter(name, primitive(value)));
        return this;
    }

    public LegendBase with_(String name) {
        this.setters.add(new Setter(name, DEFAULT));
        return this;
    }

    public static And and(Filterable...filters) {
        return new And(filters);
    }

    public static Or or(Filterable...filters) {
        return new Or(filters);
    }

    public static Comparator eq(String name, Object o) {
        return new Comparator(name, "=", primitive(o));
    }

    public static Comparator eq_(String name) {
        return new Comparator(name, "=", DEFAULT);
    }

    public static Comparator ne(String name, Object o) {
        return new Comparator(name, "!=", primitive(o));
    }

    public static Comparator ne_(String name) {
        return new Comparator(name, "!=", DEFAULT);
    }

    public static Comparator lt(String name, Object o) {
        return new Comparator(name,  "<", primitive(o));
    }

    public static Comparator lt_(String name) {
        return new Comparator(name, "<", DEFAULT);
    }

    public static Comparator le(String name, Object o) {
        return new Comparator(name, "<=", primitive(o));
    }

    public static Comparator le_(String name) {
        return new Comparator(name, "<=", DEFAULT);
    }

    public static Comparator gt(String name, Object o) {
        return new Comparator(name, ">", primitive(o));
    }

    public static Comparator gt_(String name) {
        return new Comparator(name, ">", DEFAULT);
    }

    public static Comparator ge(String name, Object o) {
        return new Comparator(name, ">=", primitive(o));
    }

    public static Comparator ge_(String name) {
        return new Comparator(name, ">=", DEFAULT);
    }

    public static Like search(String name, String target) {
        return new Like(name, target);
    }

    public static Filterable searchList(List<String> names, String target) {
        List<Filterable> list = new ArrayList<>();
        for (String name: names) {
            list.add(search(name, target));
        }
        return new Or(list);
    }

    public static Between between(String name, Object min, Object max) {
        return new Between(name, primitive(min), primitive(max));
    }

    public static Between between_(String name) {
        return new Between(name, DEFAULT, DEFAULT);
    }

    public static In in(String name, Object...os) {
        return new In(name, set(os));
    }

    public static In in(String name, int includes) {
        PrimitiveCollection sets = set();
        for (int i=0;i<includes;i++) {
            sets.values.add(DEFAULT);
        }
        return new In(name, sets);
    }

    public static In in(String name, Collection<Object> os) {
        return new In(name, set(os));
    }

    public static In in(String name, SubQuery subQuery) {
        return new In(name, subQuery);
    }

    public static NotIn notIn(String name, Object...os) {
        return new NotIn(name, set(os));
    }

    public static NotIn notIn_(String name, int includes) {
        PrimitiveCollection sets = set();
        for (int i=0;i<includes;i++) {
            sets.values.add(DEFAULT);
        }
        return new NotIn(name, sets);
    }

    public static NotIn notIn(String name, Collection<Object> os) {
        return new NotIn(name, set(os));
    }

    public static NotIn notIn(String name, SubQuery subQuery) {
        return new NotIn(name, subQuery);
    }

    public static Not not(Filterable filter) {
        return new Not(filter);
    }

    public static PlaceHolder placeHolder() {
        return new PlaceHolder();
    }

    public static Primitive primitive(Object o) {
        return new Primitive(o);
    }

    public static PrimitiveCollection set(Object...os) {
        List<Value> values = new ArrayList<>();
        for (Object o: os) {
            values.add(primitive(o));
        }
        return new PrimitiveCollection(values);
    }

    public static PrimitiveCollection set(Collection<Object> os) {
        List<Value> values = new ArrayList<>();
        for (Object o: os) {
            values.add(primitive(o));
        }
        return new PrimitiveCollection(values);
    }

    public static SubQuery subQuery(LegendBase legendBase) {
        return new SubQuery(legendBase);
    }

    @Override
    public String toString() {
        if (this.type == Type.Insert) {
            return this.insertSQL();
        } else if (this.type == Type.Delete) {
            return this.deleteSQL();
        } else if (this.type == Type.Update) {
            return this.updateSQL();
        } else if (this.type == Type.Select) {
            return this.selectSQL();
        } else if (this.type == Type.Create) {
            return this.createSQL();
        } else if (this.type == Type.Drop) {
            return this.dropSQL();
        } else if (this.type == Type.Truncate) {
            return this.truncateSQL();
        }
        return "SQL not supported yet";
    }

    private String selectSQL() {
        StringBuilder builder = new StringBuilder();
        builder.append("select ");
        for (int i=0;i<fields.size();i++) {
            builder.append(fields.get(i));
            if (i < fields.size()-1) {
                builder.append(",");
            }
        }
        if (this.table != null) {
            builder.append(" from ");
            builder.append(String.format("`%s`" ,tableWithSuffix()));
        }
        if (this.filter != null) {
            builder.append(" where ");
            builder.append(this.filter.s(0));
        }
        if (!this.groupBys.isEmpty()) {
            builder.append(" group by ");
            StringJoiner joiner = new StringJoiner(", ");
            for (String groupBy: this.groupBys) {
                joiner.add(groupBy);
            }
            builder.append(joiner.toString());
        }
        if (this.having != null) {
            builder.append(" having ");
            builder.append(having.s(0));
        }
        if (!this.orderBys.isEmpty()) {
            builder.append(" order by ");
            StringJoiner joiner = new StringJoiner(", ");
            for (OrderBy orderBy: this.orderBys) {
                joiner.add(orderBy.s());
            }
            builder.append(joiner.toString());
        }
        if (this.offset_) {
            builder.append(" limit ?,");
        } else if (this.offset > 0) {
            builder.append(" limit ");
            builder.append(offset).append(",");
        }

        if (builder.indexOf("limit") != -1) {
            if (this.limit_) {
                builder.append("?");
            } else if (this.limit > 0) {
                builder.append(limit);
            }
        } else {
            if (this.limit_) {
                builder.append(" limit 0,?");
            } else if (this.limit > 0) {
                builder.append(" limit 0,");
                builder.append(limit);
            }
        }
        return builder.toString();
    }

    private String updateSQL() {
        StringBuilder builder = new StringBuilder();
        builder.append("update ");
        builder.append(String.format("`%s`", tableWithSuffix()));
        builder.append(" set ");
        StringJoiner joiner = new StringJoiner(", ");
        for (Setter setter: setters) {
            joiner.add(setter.s());
        }
        builder.append(joiner.toString());
        if (this.filter != null) {
            builder.append(" where ");
            builder.append(this.filter.s(0));
        }
        return builder.toString();
    }

    private String deleteSQL() {
        StringBuilder builder = new StringBuilder();
        if (this.filter == null) {
            builder.append("delete * from ");
            builder.append(String.format("`%s`", tableWithSuffix()));
        } else {
            builder.append("delete from ");
            builder.append(String.format("`%s`", tableWithSuffix()));
            builder.append(" where ");
            builder.append(this.filter.s(0));
        }
        return builder.toString();
    }

    private String insertSQL() {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ");
        builder.append(String.format("`%s`", tableWithSuffix()));
        builder.append("(");
        StringJoiner joiner = new StringJoiner(", ");
        for (Setter setter: setters) {
            joiner.add(setter.name);
        }
        builder.append(joiner.toString());
        builder.append(") values(");
        joiner = new StringJoiner(", ");
        for (Setter setter: setters) {
            joiner.add(setter.value.value());
        }
        builder.append(joiner.toString());
        builder.append(")");
        return builder.toString();
    }

    public String truncateSQL() {
        return String.format("truncate table `%s`", tableWithSuffix());
    }

    public String dropSQL() {
        return String.format("drop table if exists `%s`", tableWithSuffix());
    }

    private String createSQL() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("create table if not exists `%s` (", tableWithSuffix()));
        StringJoiner joiner = new StringJoiner(", ");
        for (ColumnInfo columnInfo : columnInfos) {
            joiner.add(columnInfo.s());
        }
        for (Index index: indices) {
            joiner.add(index.s());
        }
        builder.append(joiner.toString());
        builder.append(")");
        if (!this.options.isEmpty()) {
            builder.append(" ");
            joiner = new StringJoiner(" ");
            for (Option option: options) {
                joiner.add(option.s());
            }
            builder.append(joiner.toString());
        }
        return builder.toString();
    }

    @FunctionalInterface
    public static interface Value {
        String value();
    }

    static public class OrderBy {
        private String name;
        private String direction;

        public OrderBy(String name, String direction) {
            this.name = name;
            if (direction == null) {
                direction = "asc";
            }
            this.direction = direction.toLowerCase();
        }

        public String s() {
            if (direction == null) {
                return name;
            }
            return String.format("`%s` %s", name, direction);
        }
    }

    static class Setter {
        private String name;
        private Value value;

        public Setter(String name, Value value) {
            this.name = String.format("`%s`", name);
            this.value = value;
        }

        public String s() {
            return String.format("%s = %s", name, value.value());
        }
    }

    @FunctionalInterface
    public static interface Filterable {
        String s(int depth);
    }

    static class And implements Filterable {

        private List<Filterable> filters = new ArrayList<>();

        public And(Filterable...filters) {
            this.filters.addAll(Arrays.asList(filters));
        }

        public And then(Filterable...filters) {
            this.filters.addAll(Arrays.asList(filters));
            return this;
        }

        @Override
        public String s(int depth) {
            StringJoiner joiner = new StringJoiner(" and ");
            for (Filterable filter: filters) {
                joiner.add(filter.s(depth+1));
            }
            if (filters.size()==1 || depth==0) {
                return joiner.toString();
            }
            return String.format("(%s)", joiner.toString());
        }
    }

    static class Or implements Filterable {

        private List<Filterable> filters = new ArrayList<>();

        public Or(List<Filterable> filters) {
            this.filters = filters;
        }

        public Or(Filterable...filters) {
            this.filters.addAll(Arrays.asList(filters));
        }

        public Or then(Filterable...filters) {
            this.filters.addAll(Arrays.asList(filters));
            return this;
        }

        @Override
        public String s(int depth) {
            StringJoiner joiner = new StringJoiner(" or ");
            for (Filterable filter: filters) {
                joiner.add(filter.s(depth+1));
            }
            if (filters.size()==1 || depth==0) {
                return joiner.toString();
            }
            return String.format("(%s)", joiner.toString());
        }
    }

    static class Comparator implements Filterable {

        private String field;
        private String op;
        private Value value;

        public Comparator(String field, String op, Value value) {
            this.field = field;
            this.op = op;
            this.value = value;
        }

        @Override
        public String s(int depth) {
            return String.format("`%s` %s %s", field, op, value.value());
        }
    }

    static class Like implements Filterable {

        private String field;
        private String value;

        public Like(String field, String value) {
            this.field = field;
            this.value = value;
        }

        @Override
        public String s(int depth) {
            String str = String.format("`%s` like ", field);
            if (StringUtils.validate(value)) {
                return str+"'%"+value+"%'";
            } else {
                return "null";
            }
        }
    }

    static class Between implements Filterable {

        private String name;
        private Value min, max;

        public Between(String name, Value min, Value max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        @Override
        public String s(int depth) {
            return String.format("`%s` between %s and %s", name, min.value(), max.value());
        }
    }

    static class In implements Filterable {
        private String name;
        private Value value;

        public In(String name, Value value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String s(int depth) {
            return String.format(" `%s` in %s", name, value.value());
        }
    }

    static class NotIn implements Filterable {

        private String name;
        private Value value;

        public NotIn(String name, Value value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String s(int depth) {
            return String.format(" `%s` in %s", name, value.value());
        }
    }

    static class Not implements Filterable {

        private Filterable filter;

        public Not(Filterable filter) {
            this.filter = filter;
        }

        @Override
        public String s(int depth) {
            return String.format("not %s", filter.s(depth+1));
        }
    }

    static class PlaceHolder implements Value {
        @Override
        public String value() {
            return "?";
        }
    }

    static class Primitive implements Value {

        private Object v;

        public Primitive(Object v) {
            this.v = v;
        }

        @Override
        public String value() {
            if (v == null) {
                return "null";
            }
            if (v instanceof String) {
                if (StringUtils.validate(v.toString())) {
                    return String.format("'%s'", v);
                } else {
                    return "null";
                }
            }
            return v.toString();
        }
    }

    static class PrimitiveCollection implements Value {

        private List<Value> values;

        public PrimitiveCollection(List<Value> values) {
            this.values = values;
        }

        public PrimitiveCollection(Value...values) {
            this.values = Arrays.asList(values);
        }

        @Override
        public String value() {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            StringJoiner joiner = new StringJoiner(", ");
            for (Value value: values) {
                joiner.add(value.value());
            }
            builder.append(joiner.toString());
            builder.append(")");
            return builder.toString();
        }
    }

    static class SubQuery implements Value {

        private LegendBase legendBase;

        public SubQuery(LegendBase legendBase) {
            this.legendBase = legendBase;
        }

        @Override
        public String value() {
            return String.format("(%s)", legendBase);
        }
    }

    public LegendBase option(String key, String value) {
        this.options.add(new Option(key, value));
        return this;
    }

    public LegendBase index(String...columns){
        return index(false, false, columns);
    }

    public LegendBase uniqueIndex(String...columns) {
        return index(false, true, columns);
    }

    public LegendBase primaryIndex(String...columns) {
        return index(true, false, columns);
    }

    public LegendBase index(boolean primaryKey, boolean unique, String...columns) {
        this.indices.add(new Index(primaryKey, unique, columns));
        return this;
    }

    public LegendBase column(String name, String type) {
        return column(name, type, false, true, null);
    }

    public LegendBase column(String name, String type, boolean autoIncrement
            , boolean nullable, String defaultValue) {
        this.columnInfos.add(new ColumnInfo(name, type, autoIncrement, nullable, defaultValue));
        return this;
    }
}
