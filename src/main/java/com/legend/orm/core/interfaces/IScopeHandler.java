package com.legend.orm.core.interfaces;

@FunctionalInterface
public interface IScopeHandler {
    void execute(Runnable runnable);
}