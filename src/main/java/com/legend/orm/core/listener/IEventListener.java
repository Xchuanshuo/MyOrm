package com.legend.orm.core.listener;

import com.legend.orm.core.Context;

@FunctionalInterface
public interface IEventListener {
    boolean on(Context ctx);
}
