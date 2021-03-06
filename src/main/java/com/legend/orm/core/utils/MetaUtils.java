package com.legend.orm.core.utils;

import com.legend.orm.core.annotation.Column;
import com.legend.orm.core.annotation.Table;
import com.legend.orm.core.exception.LegendException;
import com.legend.orm.core.factory.MetaFactory;
import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.model.ColumnInfo;
import com.legend.orm.core.model.Holder;
import com.legend.orm.core.model.Index;
import com.legend.orm.core.model.Meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Legend
 * @data by on 18-12-16.
 * @description
 */
public class MetaUtils {

    public static Meta meta(Class<? extends IEntity> clazz) {
        Map<Class<? extends IEntity>, Meta> metas =
                MetaFactory.getInstance().metas();
        Meta meta = metas.get(clazz);
        if (meta != null) return meta;
        IEntity entity = empty(clazz);
        meta = new Meta();
        meta.options = entity.options();
        meta.indices = entity.indices();
        if (meta.indices.primary() == null) {
            throw new LegendException("entity class should provide primary index");
        }
        fillMeta(clazz, meta);
        for (String name: meta.indices.primary().columns()) {
            ColumnInfo columnInfo = meta.columns.get(name);
            columnInfo.primary(true);
        }
        for (Index index: meta.indices.indices()) {
            String[] fields = new String[index.columns().length];
            int i = 0;
            for (String column: index.columns()) {
                fields[i++] = meta.columns.get(column).field().getName();
            }
            index.fields(fields);
        }
        metas.put(clazz, meta);
        return meta;
    }

    private static void fillMeta(Class<? extends IEntity> clazz, Meta meta) {
        for (Field field: clazz.getDeclaredFields()) {
            for (Annotation annotation: field.getAnnotations()) {
                if (annotation instanceof Column) {
                    if (field.getType().isPrimitive()) {
                        throw new LegendException("columnInfo must not be primitive type");
                    }
                    Column columnInfoDef = (Column) annotation;
                    ColumnInfo columnInfo = new ColumnInfo(columnInfoDef.name(), columnInfoDef.type(), columnInfoDef.autoIncrement(),
                            columnInfoDef.nullable(), columnInfoDef.defaultValue());
                    columnInfo.field(field);
                    field.setAccessible(true);
                    meta.column(columnInfo);
                    break;
                }
            }
        }
        for (Annotation annotation: clazz.getDeclaredAnnotations()) {
            if (annotation instanceof Table) {
                meta.name = ((Table) annotation).value();
                break;
            }
        }
        if (meta.name == null || meta.name.length()==0) {
            throw new LegendException("entity class should provide table name from to db");
        }
        if (meta.columns.isEmpty()) {
            throw new LegendException("entity class should provide at least one column");
        }
    }

    public static  <T extends IEntity> T empty(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new LegendException("entity class should provide default construct!");
        }
    }

    public static <T extends IEntity> T empty(Class<T> clazz, Object...ids) {
        T empty = empty(clazz);
        Meta meta = meta(clazz);
        int i = 0;
        for (String name: meta.indices.primary().columns()) {
            ColumnInfo columnInfo = meta.columns.get(name);
            try {
                columnInfo.field().set(empty, ids[i]);
            } catch (Exception e) {
                throw new LegendException("access field error!", e);
            }
            i++;
        }
        return empty;
    }


    public static <T extends IEntity> Object[] ids(T t) {
        Meta meta = MetaUtils.meta(t.getClass());
        Object[] ids = new Object[meta.indices.primary().columns().length];
        int i = 0;
        for (String name: meta.indices.primary().columns()) {
            Field field = meta.columns.get(name).field();
            try {
                ids[i++] = field.get(t);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return ids;
    }

    public static <T extends IEntity> Map<String, Object> values(T t) {
        Meta meta = MetaUtils.meta(t.getClass());
        Map<String, Object> values = new HashMap<>();
        meta.columns.forEach((name, columnInfo) -> {
            Field field = columnInfo.field();
            try {
                values.put(field.getName(), field.get(t));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return values;
    }

    public static <T extends IEntity> Map<String, Object> insertDispose(T t, Holder lastInsertId, Field lastInsertField) {
        Map<String, Object> values = new LinkedHashMap<>();
        Meta meta = MetaUtils.meta(t.getClass());
        for (Map.Entry<String, ColumnInfo> entry: meta.columns.entrySet()) {
            ColumnInfo columnInfo = entry.getValue();
            try {
                Field field = columnInfo.field();
                Object o = field.get(t);
                if (columnInfo.autoIncrement() && o==null) {
                    lastInsertId = new Holder<>();
                    lastInsertField = columnInfo.field();
                }
                values.put(field.getName(), o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return values;
    }

    public static Map<String, Object> updateDispose(Class<? extends IEntity> clazz, Map<String, Object> values) {
        Map<String, Object> valueModify = new HashMap<>(values);
        Meta meta = MetaUtils.meta(clazz);
        // 移除主键列
        for (String name: meta.indices.primary().fields()) {
            valueModify.remove(name);
        }
        return valueModify;
    }
}
