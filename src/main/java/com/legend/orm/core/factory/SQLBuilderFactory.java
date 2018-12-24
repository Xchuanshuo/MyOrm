package com.legend.orm.core.factory;

import com.legend.orm.core.crud.LegendBase;
import com.legend.orm.core.exception.LegendException;
import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.model.ColumnInfo;
import com.legend.orm.core.model.Meta;
import com.legend.orm.core.model.SelectParam;
import com.legend.orm.core.utils.MetaUtils;

import java.util.Collections;
import java.util.Map;

/**
 * @author Legend
 * @data by on 18-12-17.
 * @description SQL语句构建工厂
 */
public class SQLBuilderFactory {

    private volatile static SQLBuilderFactory factory;

    private SQLBuilderFactory() {}

    public static SQLBuilderFactory getInstance() {
        if (factory == null) {
            synchronized(SQLBuilderFactory.class) {
                if (factory == null) {
                    factory = new SQLBuilderFactory();
                }
            }
        }
        return factory;
    }

    public LegendBase buildCountObject(Class<? extends IEntity> clazz, SelectParam param) {
        param.setFieldList(Collections.singletonList("count(*)"));
        return buildFindObject(clazz, param);
    }

    public LegendBase buildAverageObject(Class<? extends IEntity> clazz, SelectParam param) {
        polymericFunctionDispose(param, "avg");
        return buildFindObject(clazz, param);
    }

    public LegendBase buildSumObject(Class<? extends IEntity> clazz, SelectParam param) {
        polymericFunctionDispose(param, "sum");
        return buildFindObject(clazz, param);
    }

    public LegendBase buildMinObject(Class<? extends IEntity> clazz, SelectParam param) {
        polymericFunctionDispose(param, "min");
        return buildFindObject(clazz, param);
    }

    public LegendBase buildMaxObject(Class<? extends IEntity> clazz, SelectParam param) {
        polymericFunctionDispose(param, "max");
        return buildFindObject(clazz, param);
    }

    // 聚合函数预处理
    private void polymericFunctionDispose(SelectParam param, String funcStr) {
        if (param.getFieldList().isEmpty()) {
            throw new LegendException("The function max() must exist a column of table!");
        }
        String str = param.getFieldList().get(0);
        param.setFieldList(Collections.singletonList(funcStr+"("+str+")"));
    }

    public String buildSelectOneSQL(Class<? extends IEntity> clazz, Object...ids) {
        Meta meta = MetaUtils.meta(clazz);
        String[] columns = meta.indices.primary().columns();
        if (columns.length != ids.length) {
            throw new LegendException("ids length must match with primary columns ");
        }
        LegendBase.Filterable[] filters = new LegendBase.Filterable[ids.length];
        for (int i=0;i<ids.length;i++) {
            filters[i] = LegendBase.eq_(columns[i]);
        }
        IEntity empty = MetaUtils.empty(clazz, ids);
        LegendBase legendBase = LegendBase.select().field("*").table(meta.name, empty.suffix())
                .where(LegendBase.and(filters));
        return legendBase.sql();
    }

    public String buildDropSQL(Class<? extends IEntity> clazz, String suffix) {
        Meta meta = MetaUtils.meta(clazz);
        LegendBase legendBase = LegendBase.drop().table(meta.name, suffix);
        return legendBase.sql();
    }

    public String buildTruncateSQL(Class<? extends IEntity> clazz, String suffix) {
        Meta meta = MetaUtils.meta(clazz);
        LegendBase legendBase = LegendBase.truncate().table(meta.name, suffix);
        return legendBase.sql();
    }

    public String buildCreateSQL(Class<? extends IEntity> clazz, String suffix) {
        Meta meta = MetaUtils.meta(clazz);
        LegendBase legendBase = LegendBase.create().table(meta.name, suffix);
        meta.columns.forEach((name, columnInfo) -> legendBase.column(columnInfo.name(), columnInfo.type(), columnInfo.autoIncrement()
                , columnInfo.nullable(), columnInfo.defaultValue()));
        meta.indices.indices().forEach(index -> legendBase.index(index.primaryKey(), index.unique(), index.columns()));
        meta.options.options().forEach(option -> legendBase.option(option.key(), option.value()));
        return legendBase.sql();
    }

    public String buildFindSQL(Class<? extends IEntity> clazz, SelectParam param) {
        return buildFindObject(clazz, param).sql();
    }

    public LegendBase buildFindObject(Class<? extends IEntity> clazz, SelectParam param) {
        Meta meta = MetaUtils.meta(clazz);
        LegendBase legendBase = LegendBase.select();
        if (param.getFieldList()!=null
                && param.getFieldList().size() > 0) {
            for (String fieldStr: param.getFieldList()) {
                legendBase.field(fieldStr);
            }
        } else {
            legendBase.field("*");
        }
        legendBase.table(meta.name, param.getSuffix());
        if (param.getFilter() != null) {
            legendBase.where(param.getFilter());
        }
        if (param.getOrderByList()!=null && param.getOrderByList().size()>0) {
            param.getOrderByList().forEach(legendBase::orderBy);
        }
        if (param.getGroupByList()!=null && param.getGroupByList().size()>0) {
            param.getGroupByList().forEach(legendBase::groupBy);
        }
        if (param.getOffset() > 0) {
            legendBase.offset_();
        }
        if (param.getLimit() > 0) {
            legendBase.limit_();
        }
        if (param.getHaving() != null) {
            legendBase.having(param.getHaving());
        }
        return legendBase;
    }

    public <T extends IEntity> LegendBase buildInsertObject(T t, Map<String, Object> values) {
        Meta meta = MetaUtils.meta(t.getClass());
        LegendBase legendBase = LegendBase.insert().table(meta.name, t.suffix());
        for (Map.Entry<String, ColumnInfo> entry: meta.columns.entrySet()) {
            ColumnInfo columnInfo = entry.getValue();
            Object value = values.get(columnInfo.field().getName());
            if (value == null) {
                if (columnInfo.autoIncrement()) continue;
                if (columnInfo.nullable()) continue;
                if (!columnInfo.nullable() && columnInfo.defaultValue() != null
                        && !columnInfo.defaultValue().isEmpty()) {
                    continue;
                }
            }
            legendBase.with_(entry.getKey());
        }
        return legendBase;
    }

    public LegendBase buildUpdateObject(Class<? extends IEntity> clazz, Map<String, Object> values,
            LegendBase.Filterable filter, Object...objects) {
        Meta meta = MetaUtils.meta(clazz);
        IEntity empty = MetaUtils.empty(clazz);
        LegendBase legendBase = null;
        if (filter != null) {
            legendBase = LegendBase.update().table(meta.name, empty.suffix())
                    .where(filter);
        } else {
            legendBase = updateForIds(empty, meta, objects);
        }
        LegendBase finalLegendBase = legendBase;
        values.forEach((name, value) -> {
            ColumnInfo columnInfo = meta.fields.get(name);
            if (columnInfo == null || columnInfo.primary()) {
                return;
            }
            finalLegendBase.with_(columnInfo.name());
        });
        return legendBase;
    }

    private LegendBase updateForIds(IEntity empty, Meta meta, Object...ids) {
        String[] columns = meta.indices.primary().columns();
        if (columns.length != ids.length) {
            throw new LegendException("ids length must match with primary columns");
        }
        LegendBase.Filterable[] filters = new LegendBase.Filterable[ids.length];
        for (int i=0;i<ids.length;i++) {
            filters[i] = LegendBase.eq_(columns[i]);
        }
        LegendBase legendBase = LegendBase.update().table(meta.name, empty.suffix()).where(LegendBase.and(filters));
        return legendBase;
    }

    public LegendBase buildDeleteObject(Class<? extends IEntity> clazz, Object...ids) {
        Meta meta = MetaUtils.meta(clazz);
        String[] columns = meta.indices.primary().columns();
        if (columns.length != ids.length) {
            throw new LegendException("ids length must match with primary columns");
        }
        LegendBase.Filterable[] filters = new LegendBase.Filterable[columns.length];
        for (int i=0;i<ids.length;i++) {
            filters[i] = LegendBase.eq_(columns[i]);
        }
        IEntity empty = MetaUtils.empty(clazz, ids);
        LegendBase legendBase = LegendBase.delete().table(meta.name, empty.suffix()).where(LegendBase.and(filters));
        return legendBase;
    }
}
