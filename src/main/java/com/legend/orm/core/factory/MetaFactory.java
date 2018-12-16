package com.legend.orm.core.factory;

import com.legend.orm.core.interfaces.IEntity;
import com.legend.orm.core.model.Meta;
import com.legend.orm.demo.Member;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Legend
 * @data by on 18-12-16.
 * @description
 */
public class MetaFactory {

    private static Map<Class<? extends IEntity>, Meta>
            metas = new ConcurrentHashMap<>();

    private volatile static MetaFactory factory;

    private MetaFactory() {}

    public static MetaFactory getInstance() {
       if (factory == null) {
           synchronized(MetaFactory.class) {
               factory = new MetaFactory();
           }
       }
        return factory;
    }

    public Map<Class<? extends IEntity>, Meta> metas() {
        return metas;
    }
}
