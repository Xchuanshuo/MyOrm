package com.legend.orm.core.listener;

import com.legend.orm.core.Context;
import com.legend.orm.core.interfaces.IScopeHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Legend
 * @data by on 18-12-16.
 * @description
 */
public class ListenerHandler {

    private List<IEventListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private ThreadLocal<IEventListener> localListener = new ThreadLocal<>();

    public ListenerHandler on(IEventListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public ListenerHandler off(IEventListener listener) {
        this.listeners.remove(listener);
        return this;
    }

    public IScopeHandler scope(IEventListener listener) {
        return runnable -> {
            localListener.set(listener);
            try {
                runnable.run();
            } finally {
                localListener.remove();
            }
        };
    }

    public boolean invokeListeners(Context event) {
        if (localListener.get() != null) {
            if (!localListener.get().on(event)) {
                return false;
            }
        }
        for (IEventListener listener: listeners) {
            if (!listener.on(event)) {
                return false;
            }
        }
        return true;
    }

}
